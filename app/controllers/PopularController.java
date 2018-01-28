package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.EpisodeSeen;
import models.Popular;
import models.Season;
import models.TvShow;
import models.service.EpisodeSeenService;
import models.service.PopularService;
import models.service.SeasonService;
import models.service.TvShowService;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Roles;
import utils.json.JsonViews;

import java.util.ArrayList;
import java.util.List;

public class PopularController extends Controller {

  private final TvShowService tvShowService;
  private final PopularService popularService;
  private final SeasonService seasonService;
  private final EpisodeSeenService episodeSeenService;
  private final utils.json.Utils jsonUtils;
  private final Roles roles;

  @Inject
  public PopularController(TvShowService tvShowService, PopularService popularService, SeasonService seasonService,
                           EpisodeSeenService episodeSeenService, utils.json.Utils jsonUtils, Roles roles) {
    this.tvShowService = tvShowService;
    this.popularService = popularService;
    this.seasonService = seasonService;
    this.episodeSeenService = episodeSeenService;
    this.jsonUtils = jsonUtils;
    this.roles = roles;
  }

  // devolver las series más populares
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result getPopular(Integer querySize) {

    Integer size = 5;
    Integer minimum = 1;
    Integer maximum = 10;
    // comprobar si tiene tamaño en la query
    if (querySize != null && querySize > minimum && querySize <= maximum) {
      size = querySize;
    }

    List<Popular> populars = popularService.getPopular(size);
    // si la lista está vacía, not found
    if (populars.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    } else {
      // si la lista no está vacía, devolvemos datos
      try {
        List<TvShow> tvShows = new ArrayList<>();
        for (Popular popular: populars) {
          tvShows.add(popular.tvShow);
        }

        JsonNode jsonNode = jsonUtils.jsonParseObject(tvShows, JsonViews.FullTvShow.class);
        ObjectNode objectNode = Json.newObject();
        // añadimos popularidad y tendencia de cada serie
        jsonNode.forEach((tvShow) -> {
          Popular popular = tvShowService.find(tvShow.get("id").asInt()).popular;
          ((ObjectNode)tvShow).put("poster", popular.tvShow.poster);
          ((ObjectNode)tvShow).put("popularity", popular.getPopularity());
          ((ObjectNode)tvShow).put("trend", popular.getTrend());
          Boolean following = tvShowService.checkFollowTvShow(tvShow.get("id").asInt(), roles.getUser(Http.Context.current()).id);
          ((ObjectNode)tvShow).put("following", following);
          // si el usuario sigue la serie, comprobar cuántos episodio de cada temporada no ha visto
          if (following) {
            Integer totalEpisodes = 0;
            Integer totalSeenEpisodes = 0;
            for (JsonNode season : tvShow.withArray("seasons")) {

              Integer seasonEpisodesCount = 0;
              Integer seasonSeenEpisodesCount = 0;

              Season seasonObject = seasonService.getSeasonByNumber(tvShowService.find(tvShow.get("id").asInt()), season.get("seasonNumber").asInt());
              if (seasonObject != null) {
                seasonEpisodesCount = seasonObject.episodes.size();
                seasonSeenEpisodesCount = episodeSeenService.getSeasonEpisodesSeen(seasonObject, roles.getUser(Http.Context.current()).id).size();
              }
              totalEpisodes += seasonEpisodesCount;
              totalSeenEpisodes += seasonSeenEpisodesCount;
            }
            // mostrar en la serie numero de episodio total, numero de episodios vistos total y numero de episodio no vistos total
            ((ObjectNode) tvShow).put("episodeCount", totalEpisodes);
            ((ObjectNode) tvShow).put("seenCount", totalSeenEpisodes);
            ((ObjectNode) tvShow).put("unseenCount", totalEpisodes - totalSeenEpisodes);
          }
          // por ultimo, borrar los campos innecesarios que han sido necesarios poner para calcular...
          // TODO: mejorar todo esto !
          ((ObjectNode) tvShow).remove("tvdbId");
          ((ObjectNode) tvShow).remove("tmdbId");
          ((ObjectNode) tvShow).remove("overview");
          ((ObjectNode) tvShow).remove("banner");
          ((ObjectNode) tvShow).remove("fanart");
          ((ObjectNode) tvShow).remove("network");
          ((ObjectNode) tvShow).remove("runtime");
          ((ObjectNode) tvShow).remove("genre");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("status");
          ((ObjectNode) tvShow).remove("tvShowVotes");
          ((ObjectNode) tvShow).remove("seasons");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("rating");
        });
        // añadimos tamaño
        objectNode.put("size", tvShows.size());
        // añadimos series
        objectNode.set("tvShows", jsonNode);

        return ok(objectNode);
      } catch (Exception ex) {
        // si hubiese un error, devolver error interno
        Logger.debug(ex.getMessage());
        ObjectNode result = Json.newObject();
        result.put("error", "It can't be processed");
        return internalServerError(result);
      }
    }

  }

}
