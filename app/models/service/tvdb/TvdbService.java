package models.service.tvdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.TvShow;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class TvdbService {

  private final WSClient ws;
  private final SimpleDateFormat df;
  private final TvdbConnection tvdbConnection;

  @Inject
  public TvdbService(WSClient ws, SimpleDateFormat df, TvdbConnection tvdbConnection) {
    this.ws = ws;
    this.df = df;
    this.tvdbConnection = tvdbConnection;
  }

  // peticion a TVDB
  private JsonNode tvdbGetRequest(String query, String language) {
    JsonNode result = null;
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
        Logger.error("Petición TVDB - " + ex.getMessage());
        break;
      }
    }

    return result;
  }

  // descargar una imagen desde una url a un directorio local definido por tvdbId y tipo
  private String downloadImage(URL url, String format, String path) {
    BufferedImage image;
    try {
      // descargamos imagen
      image = ImageIO.read(url);
      File imageFile = new File(path);
      // creamos carpetas
      imageFile.getParentFile().mkdirs();
      // guardamos imagen
      ImageIO.write(image, format, imageFile);
    } catch (Exception ex) {
      Logger.error("Download Image - error descargando imagen");
      return null;
    }
    return path;
  }

  // obtener imagen por tvdbId y tipo
  public String getImage(TvShow tvShow, String type) {
    Logger.info(tvShow.name + " - descargando " + type);
    String imageQuery = "https://api.thetvdb.com/series/" + tvShow.tvdbId.toString() + "/images/query?keyType=" + type;

    try {
      JsonNode images = tvdbGetRequest(imageQuery, "es").withArray("data");
      String fileName = null;

      if (images.size() > 1) {
        // seleccionamos la imagen mas votada
        Integer bestRating = -1;
        Integer moreRatings = -1;

        for (JsonNode image: images) {
          Integer imageRating = image.get("ratingsInfo").get("average").asInt();
          if (imageRating > bestRating) {
            fileName = image.get("fileName").asText();
            bestRating = imageRating;
          } else if (imageRating.equals(bestRating)) {
            // si tienen la misma puntuacion, cogemos la que mas votaciones lleve
            Integer imageRatings = image.get("ratingsInfo").get("count").asInt();
            if (imageRatings > moreRatings) {
              fileName = image.get("fileName").asText();
              moreRatings = imageRatings;
            }
          }
        }
      } else if (images.size() == 1) {
        // solo hay una imagen, la cogemos
        fileName = images.get(0).get("fileName").asText();
      } else {
        // no se han encontrado imagenes
        return null;
      }

      if (fileName != null) {
        // descargar imagen
        URL downloadURL = new URL("http://thetvdb.com/banners/" + fileName);
        String format = fileName.substring(fileName.lastIndexOf('.') + 1);
        String path = "./public/images/series/" + tvShow.id.toString() + "/" + type + "." + format;
        String resultPath = downloadImage(downloadURL, format, path);
        if (resultPath != null) {
          Logger.info(tvShow.name + " - " + type + " descargado");
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
    Logger.info(tvShow.name + " - descargando banner");
    try {
      if (!tvShow.banner.isEmpty()) {
        URL downloadURL = new URL("http://thetvdb.com/banners/" + tvShow.banner);
        String format = tvShow.banner.substring(tvShow.banner.lastIndexOf('.') + 1);
        String path = "./public/images/series/" + tvShow.id.toString() + "/banner." + format;
        String resultPath = downloadImage(downloadURL, format, path);
        if (resultPath != null) {
          Logger.info(tvShow.name + " - banner descargado");
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
  public TvShow findOnTvdbByTvdbId(Integer tvdbId) {
    TvShow tvShow = null;
    JsonNode respuesta;
    String query = "https://api.thetvdb.com/series/" + tvdbId.toString();

    respuesta = tvdbGetRequest(query, "es");

    // comprobamos si ha encontrado el tv show en Tvdb
    if (respuesta != null && respuesta.has("data")) {
      JsonNode jsonTvShow = respuesta.with("data");
      Logger.debug("TvShow encontrado en TvdbConnection: " + jsonTvShow.get("seriesName").asText());

      // inicializamos el tv show
      tvShow =  new TvShow();
      tvShow.tvdbId = jsonTvShow.get("id").asInt();                    // id de tvdbConnection
      tvShow.imdbId = jsonTvShow.get("imdbId").asText();
      tvShow.name = jsonTvShow.get("seriesName").asText();             // nombre de la tvShow
      tvShow.banner = jsonTvShow.get("banner").asText();               // banner de la tvShow
      tvShow.local = false;
      JsonNode fecha = jsonTvShow.get("firstAired");
      tvShow.firstAired = parseDate(fecha);

    } else {
      Logger.info("TvShow no encontrada en TVDB");
    }

    return tvShow;
  }

  // buscar por campo
  public List<TvShow> findOnTVDBby(String field, String value) {
    // buscamos el TV Show en tvdbConnection
    JsonNode respuesta;
    List<TvShow> tvShows = new ArrayList<>();
    String query = "https://api.thetvdb.com/search/series?" + field + "=" + value;

    respuesta = tvdbGetRequest(query, "es");

    // recorremos tv shows encontrados en TVDB
    if (respuesta != null && respuesta.has("data")) {
      for (JsonNode jsonTvShow : respuesta.withArray("data")) {
        // obtenemos datos del TV Show
        TvShow nuevaTvShow = new TvShow();
        nuevaTvShow.tvdbId = Integer.parseInt(jsonTvShow.get("id").asText()); // id de tvdbConnection
        nuevaTvShow.name = jsonTvShow.get("seriesName").asText();   // nombre del TV Show
        nuevaTvShow.banner = jsonTvShow.get("banner").asText();     // banner del TV Show
        nuevaTvShow.local = false;                                  // iniciamos por defecto a false

        JsonNode fecha = jsonTvShow.get("firstAired");
        nuevaTvShow.firstAired = parseDate(fecha);

        // finalmente la añadimos a la lista
        tvShows.add(nuevaTvShow);
      }
    }

    return tvShows;
  }

  public TvShow getTvShowTVDB(Integer tvdbId) {
    TvShow tvShow = null;
    JsonNode respuesta;
    String query = "https://api.thetvdb.com/series/" + tvdbId.toString();

    respuesta = tvdbGetRequest(query, "es");

    // comprobamos si ha encontrado el tv show en Tvdb
    if (respuesta != null && respuesta.has("data")) {
      JsonNode jsonTvShow = respuesta.with("data");
      Logger.info("TvShow encontrado en TvdbConnection: " + jsonTvShow.get("seriesName").asText());

      // inicializamos el tv show
      tvShow =  new TvShow();
      tvShow.tvdbId = jsonTvShow.get("id").asInt();                    // id de tvdbConnection
      tvShow.imdbId = jsonTvShow.get("imdbId").asText();
      tvShow.name = jsonTvShow.get("seriesName").asText();             // nombre de la tvShow
      tvShow.banner = jsonTvShow.get("banner").asText();               // banner de la tvShow
      tvShow.network = jsonTvShow.get("network").asText();
      tvShow.overview = jsonTvShow.get("overview").asText();
      tvShow.rating = jsonTvShow.get("rating").asText();
      tvShow.runtime = jsonTvShow.get("runtime").asText();
      tvShow.local = false;
      JsonNode fecha = jsonTvShow.get("firstAired");
      tvShow.firstAired = parseDate(fecha);

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

  // parsear fecha
  private Date parseDate(JsonNode jsonDate) {
    int i = 0, tries = 100;
    Date result = null;

    if (!jsonDate.isNull() && !jsonDate.asText().equals("")) {

      while (i < tries) {
        try {
          df.applyPattern("yyyy-MM-dd");
          result = df.parse(jsonDate.asText()); // fecha estreno
          break;
        } catch (Exception e) {
          i++;
          if (i != 100) {
            Logger.info("Reintentando parsear fecha de estreno");
          } else {
            Logger.error("No se ha podido parsear la fecha");
          }
        }
      }
    }

    return result;
  }

}
