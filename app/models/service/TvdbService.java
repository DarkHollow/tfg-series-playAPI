package models.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.TvShow;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utils.TVDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class TvdbService {

  private final WSClient ws;
  private final SimpleDateFormat df;
  private final TvShowService tvShowService;

  @Inject
  public TvdbService(WSClient ws, SimpleDateFormat df, TvShowService tvShowService) {
    this.ws = ws;
    this.df = df;
    this.tvShowService = tvShowService;
  }

  // buscar por tvdbId (solo un resultado posible)
  public TvShow findOnTvdbByTvdbId(Integer tvdbId) {
    TvShow tvShow = null;
    JsonNode respuesta = null;

    try {
      CompletionStage<JsonNode> stage = ws.url("https://api.thetvdb.com/series/" + tvdbId.toString())
              .setHeader("Authorization", "Bearer " + TVDB.getToken())
              .setHeader("Accept-Language", "es")
              .get()
              .thenApply(WSResponse::asJson);

      respuesta = stage.toCompletableFuture().get(5, TimeUnit.SECONDS);

      System.out.println(respuesta);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }

    // comprobamos si ha encontrado la tvShow en TVDB
    if (respuesta != null && respuesta.has("data")) {
      JsonNode jsonTvShow = respuesta.with("data");
      Logger.debug("TvShow encontrado en TVDB: " + jsonTvShow.get("seriesName").asText());

      // inicializamos la tvShow
      tvShow =  new TvShow();
      tvShow.tvdbId = Integer.parseInt(jsonTvShow.get("id").asText()); // id de tvdb
      tvShow.name = jsonTvShow.get("seriesName").asText();       // nombre de la tvShow
      tvShow.banner = jsonTvShow.get("banner").asText();               // banner de la tvShow
      tvShow.local = false;

      try {
        df.applyPattern("yyyy-MM-dd");
        tvShow.firstAired = df.parse(jsonTvShow.get("firstAired").asText()); // fecha estreno
      } catch (ParseException e) {
        Logger.debug("No se ha podido parsear la fecha de estreno");
      }

      // comprobamos si la tenemos en la base de datos
      if (tvShowService.findByTvdbId(tvdbId) != null) {
        Logger.debug("TvShow encontrada en local");
        tvShow.local = true;
      }
    } else {
      Logger.debug("TvShow no encontrada en TVDB");
    }

    return tvShow;
  }

  // buscar en tvdb y en local, marcar las que coinciden (multiples resultados posibles)
  // buscar por campo
  public List<TvShow> findOnTVDBby(String field, String value) {
    // buscamos el TV Show en tvdb
    JsonNode respuesta = null;
    List<TvShow> tvShows = new ArrayList<>();

    try {
      CompletionStage<JsonNode> stage = ws.url("https://api.thetvdb.com/search/series")
              .setHeader("Authorization", "Bearer " + TVDB.getToken())
              .setHeader("Accept-Language", "es")
              .setQueryParameter(field, value)
              .get()
              .thenApply(WSResponse::asJson);

      respuesta = stage.toCompletableFuture().get(5, TimeUnit.SECONDS);

      System.out.println("Hola");
      System.out.println(respuesta);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }

    // recorremos tvShows encontradas en TVDB
    if (respuesta != null && respuesta.has("data")) {
      for (JsonNode jsonTvShow : respuesta.withArray("data")) {
        // obtenemos datos del TV Show
        TvShow nuevaTvShow = new TvShow();
        nuevaTvShow.tvdbId = Integer.parseInt(jsonTvShow.get("id").asText()); // id de tvdb
        nuevaTvShow.name = jsonTvShow.get("seriesName").asText();   // nombre del TV Show
        nuevaTvShow.banner = jsonTvShow.get("banner").asText();     // banner del TV Show
        nuevaTvShow.local = false;                                  // iniciamos por defecto a false
        try {
          df.applyPattern("yyyy-MM-dd");
          nuevaTvShow.firstAired = df.parse(jsonTvShow.get("firstAired").asText()); // fecha estreno
        } catch (ParseException e) {
          Logger.debug("No se ha podido parsear la fecha de estreno");
        }

        // buscamos en local para indicar si la tenemos o no
        TvShow tvShowLocal = tvShowService.findByTvdbId(nuevaTvShow.tvdbId);
        if (tvShowLocal != null) {
          nuevaTvShow.local = true;
        }

        // finalmente la añadimos a la lista
        tvShows.add(nuevaTvShow);
      }
    }

    return tvShows;
  }

}
