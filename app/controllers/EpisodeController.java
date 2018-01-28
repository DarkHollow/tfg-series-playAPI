package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.EpisodeSeen;
import models.Season;
import models.TvShow;
import models.service.EpisodeSeenService;
import models.service.EpisodeService;
import models.service.SeasonService;
import models.service.TvShowService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Roles;
import utils.json.JsonViews;

import static play.mvc.Results.*;

public class EpisodeController {
  private final EpisodeService episodeService;
  private final SeasonService seasonService;
  private final TvShowService tvShowService;
  private final EpisodeSeenService episodeSeenService;
  private final utils.json.Utils jsonUtils;
  private final Roles roles;

  @Inject
  public EpisodeController(EpisodeService episodeService, SeasonService seasonService, TvShowService tvShowService,
                           EpisodeSeenService episodeSeenService, utils.json.Utils jsonUtils, Roles roles) {
    this.episodeService = episodeService;
    this.seasonService = seasonService;
    this.tvShowService = tvShowService;
    this.episodeSeenService = episodeSeenService;
    this.jsonUtils = jsonUtils;
    this.roles = roles;
  }

  // devolver todos los episodios de una temporada de una serie
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result allTvShowSeasonEpisodes(Integer tvShowId, Integer seasonNumber) {
    TvShow tvShow = tvShowService.find(tvShowId);
    if (tvShow == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    Season season = seasonService.getSeasonByNumber(tvShow, seasonNumber);
    if (season == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    try {
      JsonNode jsonNode = jsonUtils.jsonParseObject(season.episodes, JsonViews.FullAll.class);
      if (jsonNode == null) {
        ObjectNode result = Json.newObject();
        result.put("error", "Not found");
        return notFound(result);
      } else {
        ObjectNode objectNode = Json.newObject();
        Boolean following = tvShowService.checkFollowTvShow(tvShow.id, roles.getUser(Http.Context.current()).id);
        objectNode.put("following", following);
        Integer episodesCount = jsonNode.size();
        // si el usuario sigue la serie, comprobar cuántos episodio de cada temporada no ha visto
        if (following) {
          Integer seenEpisodesCount = 0;
          for (JsonNode episode : jsonNode) {
            EpisodeSeen episodeSeen = episodeSeenService.findByEpisodeIdUserId(episode.get("id").asInt(), roles.getUser(Http.Context.current()).id);
            if (episodeSeen != null) {
              seenEpisodesCount++;
              ((ObjectNode)episode).put("seen", episodeSeen.date.toString());
            } else {
              String nullString = null;
              ((ObjectNode)episode).put("seen", nullString);
            }
          }
          objectNode.put("seenCount", seenEpisodesCount);
          objectNode.put("unseenCount", episodesCount - seenEpisodesCount);
        }
        objectNode.put("size", episodesCount);
        objectNode.set("episodes", jsonNode);
        return ok(objectNode);
      }
    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

  // devolver un episodio de una temporada de una serie
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result seasonByTvShowIdAndSeasonNumber(Integer tvShowId, Integer seasonNumber, Integer episodeNumber) {
    TvShow tvShow = tvShowService.find(tvShowId);
    if (tvShow == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    Season season = seasonService.getSeasonByNumber(tvShow, seasonNumber);
    if (season == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    try {
      JsonNode jsonNode = jsonUtils.jsonParseObject(episodeService.getEpisodeByNumber(tvShow, seasonNumber, episodeNumber), JsonViews.FullAll.class);
      if (jsonNode != null) {

        Boolean following = tvShowService.checkFollowTvShow(tvShow.id, roles.getUser(Http.Context.current()).id);
        ((ObjectNode)jsonNode).put("following", following);
        // si el usuario sigue la serie, comprobar si lo ha visto o no
        if (following) {
          EpisodeSeen episodeSeen = episodeSeenService.findByEpisodeIdUserId(jsonNode.get("id").asInt(), roles.getUser(Http.Context.current()).id);
          if (episodeSeen != null) {
            ((ObjectNode)jsonNode).put("seen", episodeSeen.date.toString());
          } else {
            String nullString = null;
            ((ObjectNode)jsonNode).put("seen", nullString);
          }
        }

        return ok(jsonNode);
      } else {
        ObjectNode result = Json.newObject();
        result.put("error", "Not found");
        return notFound(result);
      }
    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

}
