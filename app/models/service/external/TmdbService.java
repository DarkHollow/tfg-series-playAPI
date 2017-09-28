package models.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.TvShow;
import org.hibernate.context.TenantIdentifierMismatchException;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TmdbService {

  private final WSClient ws;
  private final SimpleDateFormat df;
  private final TmdbConnection tmdbConnection;

  private static char SEPARATOR = File.separatorChar;

  @Inject
  public TmdbService(WSClient ws, SimpleDateFormat df, TmdbConnection tmdbConnection) {
    this.ws = ws;
    this.df = df;
    this.tmdbConnection = tmdbConnection;
  }

  // petición a The Movie Database API
  private JsonNode getRequest(String query, String language)
          throws InterruptedException, ExecutionException, TimeoutException {
    JsonNode result = null;
    WSResponse response;

    // encodear como URI
    query = query.replaceAll(" ", "%20");

    // query + params
    String finalQuery = query;

    // añadir idioma
    if (finalQuery.contains("?")) {
      finalQuery += "&language=" + language;
    } else {
      finalQuery += "?language=" + language;
    }

    // añadir api key
    finalQuery += "&api_key=" + tmdbConnection.getApiKey();

    try {
      CompletionStage<WSResponse> stage = ws.url(finalQuery).get();
      response = stage.toCompletableFuture().get(10, TimeUnit.SECONDS);

      switch (response.getStatus()) {
        case 200:
          result = response.asJson();
        case 401:
          Logger.error("Petición TMDb - Sin autorización, comprobar API Key");
        case 404:
          Logger.info("Petición TMDb - Recurso no encontrado");
        default:
          Logger.warn("Petición TMDb - Status no controlado");
      }

    } catch (Exception ex) {
      Logger.error("Petición TMDb - " + ex.getClass());
      throw ex;
    }

    return result;
  }

  // buscar por tvdbId
  public TvShow findByTvdbId(Integer tvdbId) throws InterruptedException, ExecutionException, TimeoutException {
    TvShow tvShow = null;
    JsonNode respuesta;
    String query = "https://api.themoviedb.org/3/find/" + tvdbId.toString() + "?external_source=tvdb_id";

    respuesta = getRequest(query, "es-ES");

    // comprobamos si ha encontrado el tv show en Tvdb
    if (respuesta != null && respuesta.withArray("tv_results").size() > 0) {
      JsonNode jsonTvShow = respuesta.with("tv_results").get(0);
      Logger.info("TvShow encontrado en The Movie Database API: " + respuesta.get("name").asText());

      // inicializamos el tv show
      tvShow =  new TvShow();
      tvShow.tvdbId = tvdbId;
      tvShow.name = jsonTvShow.get("name").asText();
      tvShow.overview = jsonTvShow.get("overview").asText();
      tvShow.fanart = jsonTvShow.get("backdrop_path").asText();
      tvShow.local = false;
      JsonNode fecha = jsonTvShow.get("first_aire_date");
      tvShow.firstAired = parseDate(fecha);

    } else {
      Logger.info("TvShow no encontrada en The Movie Database API");
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
