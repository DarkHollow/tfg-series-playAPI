package models.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.TvShow;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TvdbService {

  private final WSClient ws;
  private final ExternalUtils externalUtils;
  private final TvdbConnection tvdbConnection;

  private static char SEPARATOR = File.separatorChar;

  @Inject
  public TvdbService(WSClient ws, ExternalUtils externalUtils, TvdbConnection tvdbConnection) {
    this.ws = ws;
    this.externalUtils = externalUtils;
    this.tvdbConnection = tvdbConnection;
  }

  // peticion a TVDB
  private JsonNode tvdbGetRequest(String query, String language)
          throws InterruptedException, ExecutionException, TimeoutException {
    JsonNode result;
    int MAXTRIES = 2; // maximos intentos de refrescar token
    int count = 0;

    // encodear como URI
    query = query.replaceAll(" ", "%20");

    while (true) {
      try {
        CompletionStage<JsonNode> stage = ws.url(query)
                .setHeader("Authorization", "Bearer " + tvdbConnection.getToken())
                .setHeader("Accept-Language", language)
                .get()
                .thenApply(WSResponse::asJson);

        result = stage.toCompletableFuture().get(10, TimeUnit.SECONDS);

        if (result != null && !(++count == MAXTRIES)) {
          if (result.has("data")) {
            break;
          } else if (result.has("Error") && Objects.equals(result.get("Error").asText(), "Not authorized")) {
            // no autorizado en tvdb
            Logger.info("Petición TVDB - Sin autorizacion, refrescando token...");
            tvdbConnection.refreshToken();
          } else if (result.has("Error") && result.get("Error").asText().contains("No results")) {
            // error: query sin resultados
            if (language.equals("es")) {
              Logger.info("Petición TVDB - sin resultados - probando en inglés...");
              return tvdbGetRequest(query, "en");
            } else {
              Logger.info("Petición TVDB - sin resultados, no podemos obtener datos de la siguiente query:\nQuery: " + query);
            }
          } else {
            Logger.error(result.toString());
            Logger.debug("Petición TVDB - error que desconocemos");
          }
        } else {
          Logger.info("Petición TVDB - respuesta es null o llevamos 2 intentos, no podemos obtener datos de la " +
                  "siguiente query:\nQuery: " + query);
          break;
        }
      } catch (Exception ex) {
        Logger.error("Petición TVDB - " + ex.getClass());
        throw ex;
      }
    }

    return result;
  }

  // obtener imagen por tvdbId y tipo
  public String getImage(TvShow tvShow, String type) {
    String imageQuery = "https://api.thetvdb.com/series/" + tvShow.tvdbId.toString() + "/images/query?keyType=" + type;

    try {
      JsonNode images = tvdbGetRequest(imageQuery, "es").withArray("data");
      String fileName = null;

      if (images.size() > 1) {
        // seleccionamos la imagen mas votada
        Double bestRating = -1.0;
        Integer moreRatings = -1;

        for (JsonNode image: images) {
          Double imageRating = image.get("ratingsInfo").get("average").asDouble();
          if (imageRating > bestRating) {
            fileName = externalUtils.nullableString(image.get("fileName").asText());
            bestRating = imageRating;
          } else if (imageRating.equals(bestRating)) {
            // si tienen la misma puntuacion, cogemos la que mas votaciones lleve
            Integer imageRatings = image.get("ratingsInfo").get("count").asInt();
            if (imageRatings > moreRatings) {
              fileName = externalUtils.nullableString(image.get("fileName").asText());
              moreRatings = imageRatings;
            }
          }
        }
      } else if (images.size() == 1) {
        // solo hay una imagen, la cogemos
        fileName = externalUtils.nullableString(images.get(0).get("fileName").asText());
      } else {
        // no se han encontrado imagenes
        return null;
      }

      if (fileName != null) {
        // descargar imagen
        URL downloadURL = new URL("https://thetvdb.com/banners/" + fileName);
        // generamos nombre a guardar a partir de la primera letra del tipo con la mitad del hashCode en positivo
        String saveName = type.substring(0, 1) + externalUtils.positiveHalfHashCode(fileName.hashCode());
        // sacamos la extensión del fichero de imagen
        String format = fileName.substring(fileName.lastIndexOf('.') + 1);
        // generamos la ruta donde se guardará la imagen
        String folderPath = "." + SEPARATOR + "public" + SEPARATOR + "images" + SEPARATOR + "series" + SEPARATOR + tvShow.id.toString();
        // ruta absoluta
        String path = folderPath + SEPARATOR + saveName + "." + format;
        // descargamos imagen
        String resultPath = externalUtils.downloadImage(downloadURL, format, path);
        if (resultPath != null) {
          Logger.info(tvShow.name + " - " + type + " descargado");
          // borrar imagen antigua
          externalUtils.deleteOldImages(folderPath, type.substring(0, 1), saveName + "." + format);
          return resultPath;
        }
      } else {
        // no hay imagenes ?
        return null;
      }
    } catch (Exception ex) {
      Logger.error(tvShow.name + " - error descargando " + type);
      Logger.debug(ex.getClass().toString());
    }
    return null;
  }

  // obtener banner - función específica por características especiales de banner...
  public String getBanner(TvShow tvShow) {
    try {
      TvShow newTvShow = getTvShowTVDB(tvShow.tvdbId);
      String newBanner = externalUtils.nullableString(newTvShow.banner);

      if (newBanner != null) {
        URL downloadURL = new URL("https://thetvdb.com/banners/" + newBanner);
        // generamos nombre a guardar a partir de la primera letra del tipo con la mitad del hashCode en positivo
        String saveName = "b" + externalUtils.positiveHalfHashCode(newBanner.hashCode());
        // sacamos la extensión del archivo
        String format = newBanner.substring(newBanner.lastIndexOf('.') + 1);
        // generamos la ruta donde se guardará la imagen
        String folderPath = "." + SEPARATOR + "public" + SEPARATOR + "images" + SEPARATOR + "series" + SEPARATOR + tvShow.id.toString();
        // ruta absoluta
        String path = folderPath + SEPARATOR + saveName + "." + format;
        // descargamos la imagen
        String resultPath = externalUtils.downloadImage(downloadURL, format, path);
        if (resultPath != null) {
          Logger.info(tvShow.name + " - banner descargado");
          externalUtils.deleteOldImages(folderPath, "b", saveName + "." + format);
          return resultPath;
        }
      } else {
        Logger.info(tvShow.name + " - no tiene banner");
      }
    } catch (MalformedURLException ex) {
      Logger.error(tvShow.name + " Error formato URL de banner, no descargado");
    } catch (Exception ex) {
      Logger.error(tvShow.name + " Error descargando banner: " + ex.getMessage());
    }
    return null;
  }

  // buscar por tvdbId (solo un resultado posible)
  public TvShow findOnTvdbByTvdbId(Integer tvdbId) throws InterruptedException, ExecutionException, TimeoutException {
    TvShow tvShow = null;
    JsonNode respuesta;
    String query = "https://api.thetvdb.com/series/" + tvdbId.toString();

    respuesta = tvdbGetRequest(query, "es");

    // comprobamos si ha encontrado el tv show en Tvdb
    if (respuesta != null && respuesta.has("data")) {
      JsonNode jsonTvShow = respuesta.with("data");
      Logger.debug("TvShow encontrado en TvdbConnection: " + jsonTvShow.get("seriesName").asText());
      // inicializamos el tv show
      tvShow = new TvShow();
      tvShow.tvdbId = jsonTvShow.get("id").asInt();
      tvShow.imdbId = externalUtils.nullableString(jsonTvShow.get("imdbId").asText());
      tvShow.name = externalUtils.nullableString(jsonTvShow.get("seriesName").asText());
      tvShow.banner = externalUtils.nullableString(jsonTvShow.get("banner").asText());
      tvShow.local = false;
      JsonNode fecha = jsonTvShow.get("firstAired");
      tvShow.firstAired = externalUtils.parseDate(fecha);
    } else {
      Logger.info("TheTVDB - TV Show no encontrada en TVDB");
    }
    return tvShow;
  }

  // buscar por campo
  public List<TvShow> findOnTVDBby(String field, String value)
          throws InterruptedException, ExecutionException, TimeoutException {
    // buscamos el TV Show en tvdbConnection
    JsonNode respuesta;
    List<TvShow> tvShows = new ArrayList<>();
    String query = "https://api.thetvdb.com/search/series?" + field + "=" + value;

    respuesta = tvdbGetRequest(query, "es");

    // recorremos tv shows encontrados en TVDB
    if (respuesta != null) {
      if (respuesta.has("data")) {
        for (JsonNode jsonTvShow : respuesta.withArray("data")) {
          // obtenemos datos del TV Show
          TvShow nuevaTvShow = new TvShow();
          nuevaTvShow.tvdbId = Integer.parseInt(jsonTvShow.get("id").asText());
          nuevaTvShow.name = externalUtils.nullableString(jsonTvShow.get("seriesName").asText());
          nuevaTvShow.banner = externalUtils.nullableString(jsonTvShow.get("banner").asText());
          nuevaTvShow.local = false;  // iniciamos por defecto a false

          JsonNode fecha = jsonTvShow.get("firstAired");
          nuevaTvShow.firstAired = externalUtils.parseDate(fecha);

          // finalmente la añadimos a la lista
          tvShows.add(nuevaTvShow);
        }
      } else if (respuesta.has("error")) {
        Logger.info("TVDB Service findOnTVDBby: " + respuesta.get("error").toString());
      }
    } else {
      Logger.error("No se ha podido hacer petición a TVDB: TVDB caída, falta de conexión, timeout de petición...");
    }

    return tvShows;
  }

  public TvShow getTvShowTVDB(Integer tvdbId) throws InterruptedException, ExecutionException, TimeoutException {
    TvShow tvShow = null;
    JsonNode respuesta;
    String query = "https://api.thetvdb.com/series/" + tvdbId.toString();

    respuesta = tvdbGetRequest(query, "es");

    // comprobamos si ha encontrado el tv show en Tvdb
    if (respuesta != null && respuesta.has("data")) {
      JsonNode jsonTvShow = respuesta.with("data");
      Logger.info("TheTVDB - TV Show encontrada: " + jsonTvShow.get("seriesName").asText());

      // inicializamos el tv show
      tvShow =  new TvShow();
      tvShow.tvdbId = jsonTvShow.get("id").asInt();
      tvShow.imdbId = externalUtils.nullableString(jsonTvShow.get("imdbId").asText());
      tvShow.name = externalUtils.nullableString(jsonTvShow.get("seriesName").asText());
      tvShow.banner = externalUtils.nullableString(jsonTvShow.get("banner").asText());
      tvShow.network = externalUtils.nullableString(jsonTvShow.get("network").asText());
      tvShow.overview = externalUtils.nullableString(jsonTvShow.get("overview").asText());
      tvShow.rating = externalUtils.nullableString(jsonTvShow.get("rating").asText());
      tvShow.runtime = externalUtils.nullableString(jsonTvShow.get("runtime").asText());
      tvShow.local = false;
      JsonNode fecha = jsonTvShow.get("firstAired");
      tvShow.firstAired = externalUtils.parseDate(fecha);

      // generos
      for (JsonNode genre : jsonTvShow.get("genre")) {
        tvShow.genre.add(genre.toString());
      }

      // status
      if (jsonTvShow.get("status").asText().equals(TvShow.Status.Continuing.toString())) {
        tvShow.status = TvShow.Status.Continuing;
      } else if (jsonTvShow.get("status").asText().equals(TvShow.Status.Ended.toString())) {
        tvShow.status = TvShow.Status.Ended;
      }

    } else {
      Logger.info("TvShow no encontrada en TVDB");
    }

    return tvShow;
  }

}
