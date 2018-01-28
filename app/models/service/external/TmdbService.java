package models.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Episode;
import models.Season;
import models.TvShow;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TmdbService {

  private final WSClient ws;
  private final ExternalUtils externalUtils;
  private final TmdbConnection tmdbConnection;

  private static char SEPARATOR = File.separatorChar;

  @Inject
  public TmdbService(WSClient ws, ExternalUtils externalUtils, TmdbConnection tmdbConnection) {
    this.ws = ws;
    this.externalUtils = externalUtils;
    this.tmdbConnection = tmdbConnection;
  }

  // petición sola
  private WSResponse request(String finalQuery) throws InterruptedException, ExecutionException, TimeoutException {
    CompletionStage<WSResponse> stage = ws.url(finalQuery).get();
    return stage.toCompletableFuture().get(10, TimeUnit.SECONDS);
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
      Boolean finished = false;
      while (!finished) {
        response = request(finalQuery);

        switch (response.getStatus()) {
          case 200:
            result = response.asJson();
            finished = true;
            break;
          case 401:
            Logger.error("Petición TMDb - Sin autorización, comprobar API Key");
            finished = true;
            break;
          case 404:
            Logger.info("Petición TMDb - Recurso no encontrado");
            finished = true;
            break;
          case 429:
            Logger.info("Petición TMDb - Límite de peticiones alcanzado, esperando 5 segundos");
            TimeUnit.SECONDS.sleep(5);
            break;
          default:
            Logger.warn("Petición TMDb - Status no controlado");
            finished = true;
            Logger.warn("Status " + response.getStatus());
        }
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
    // comprobamos si ha encontrado el tv show en Tmdb
    if (respuesta != null && respuesta.withArray("tv_results").size() > 0) {
      JsonNode jsonTvShow = respuesta.withArray("tv_results").get(0);
      Logger.info("The Movie Database API - TV Show encontrada: " + jsonTvShow.get("name").asText());

      // inicializamos el tv show
      tvShow = new TvShow();
      tvShow.tvdbId = tvdbId;
      tvShow.tmdbId = jsonTvShow.get("id").asInt();
      tvShow.name = externalUtils.nullableString(jsonTvShow.get("name").asText());
      tvShow.overview = externalUtils.nullableString(jsonTvShow.get("overview").asText());
      tvShow.fanart = externalUtils.nullableString(jsonTvShow.get("backdrop_path").asText());
      tvShow.local = false;
      JsonNode fecha = jsonTvShow.get("first_air_date");
      tvShow.firstAired = externalUtils.parseDate(fecha);
    } else {
      Logger.info("TvShow no encontrada en The Movie Database API");
    }
    return tvShow;
  }

  // obtener un show en formato JSON en tmdb por tmdb id (incluye temporadas con poca información)
  public JsonNode getJsonTvShowByTmdbId(Integer tmdbId) throws InterruptedException, ExecutionException, TimeoutException {
    JsonNode respuesta;
    String query = "https://api.themoviedb.org/3/tv/" + tmdbId.toString();

    respuesta = getRequest(query, "es-ES");

    // comprobamos si ha encontrado el tv show en Tmdb
    if (respuesta != null && respuesta.get("status_code") == null) {
      Logger.info("The Movie Database API - Temporadas de " + respuesta.get("name").asText() + " encontradas");
      return respuesta;
    } else {
      Logger.info("TvShow no encontrada en The Movie Database API");
      return null;
    }
  }

  // obtiene temporada
  public Season getCompleteSeasonByTmdbIdAndSeasonNumber(Integer tmdbId, Integer seasonNumber) throws InterruptedException, ExecutionException, TimeoutException {
    JsonNode respuesta;
    Season season = null;
    String query = "https://api.themoviedb.org/3/tv/" + tmdbId.toString() + SEPARATOR + "season" + SEPARATOR + seasonNumber.toString();

    respuesta = getRequest(query, "es-ES");

    // comprobamos si ha encontrado la season en Tmdb
    if (respuesta != null && respuesta.get("status_code") == null) {
      Logger.info("The Movie Database API - Temporada " + seasonNumber + " encontrada");
      // temporada
      season = new Season();
      // temporada de capítulos especiales?
      String name = externalUtils.nullableString(respuesta.get("name").asText());
      if (name != null && name.equals("Specials")) {
        season.name = "Especiales";
      } else {
        season.name = name;
      }
      season.overview = externalUtils.nullableString(respuesta.get("overview").asText());
      season.poster = externalUtils.nullableString(respuesta.get("poster_path").asText());
      JsonNode fecha = respuesta.get("air_date");
      season.firstAired = externalUtils.parseDate(fecha);
    } else {
      Logger.info("The Movie Database API - Temporada " + seasonNumber + " no encontrada");
    }

    return season;
  }

  // obtiene episodios
  public List<Episode> getAllSeasonEpisodesByTmdbIdAndSeasonNumber(Integer tmdbId, Integer seasonNumber) throws InterruptedException, ExecutionException, TimeoutException {
    JsonNode respuesta;
    List<Episode> episodes = null;
    String query = "https://api.themoviedb.org/3/tv/" + tmdbId.toString() + SEPARATOR + "season" + SEPARATOR + seasonNumber.toString();

    respuesta = getRequest(query, "es-ES");

    // comprobamos si ha encontrado la season en Tmdb
    if (respuesta != null && respuesta.get("status_code") == null) {
      Logger.info("The Movie Database API - Temporada " + seasonNumber + " y sus episodios encontrada");
      // inicializamos lista
      episodes = new ArrayList<>();

      // recorremos episodios de la respuesta
      if (respuesta.withArray("episodes").size() > 0) {
        JsonNode jsonEpisodes = respuesta.withArray("episodes");
        List<Episode> finalEpisodes = episodes;
        jsonEpisodes.forEach(episodeJson -> {
          Episode episode = new Episode();
          episode.episodeNumber = episodeJson.get("episode_number").asInt();
          episode.name = externalUtils.nullableString(episodeJson.get("name").asText());
          episode.overview = externalUtils.nullableString(episodeJson.get("overview").asText());
          episode.screenshot = externalUtils.nullableString(episodeJson.get("still_path").asText());
          episode.firstAired = externalUtils.parseDate(episodeJson.get("air_date"));

          finalEpisodes.add(episode);
        });
      }
    } else {
      Logger.info("TMDb Service - Get all season episodes: Season no encontrada en The Movie Database API");
    }

    return episodes;
  }

}
