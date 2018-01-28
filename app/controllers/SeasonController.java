package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Episode;
import models.EpisodeSeen;
import models.Season;
import models.TvShow;
import models.service.EpisodeSeenService;
import models.service.SeasonService;
import models.service.TvShowService;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Roles;
import utils.json.JsonViews;

import static play.mvc.Results.*;

public class SeasonController {
  private final SeasonService seasonService;
  private final TvShowService tvShowService;
  private final EpisodeSeenService episodeSeenService;
  private final utils.json.Utils jsonUtils;
  private final Roles roles;

  @Inject
  public SeasonController(SeasonService seasonService, TvShowService tvShowService,
                          EpisodeSeenService episodeSeenService, utils.json.Utils jsonUtils, Roles roles) {
    this.seasonService = seasonService;
    this.tvShowService = tvShowService;
    this.episodeSeenService = episodeSeenService;
    this.jsonUtils = jsonUtils;
    this.roles = roles;
  }

  // devolver todas las temporadas de una serie (información reducida)
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result allTvShowSeasons(Integer tvShowId) {
    TvShow tvShow = tvShowService.find(tvShowId);
    if (tvShow == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    try {
      JsonNode jsonNode = jsonUtils.jsonParseObject(tvShow.seasons, JsonViews.FullTvShow.class);
      if (jsonNode == null) {
        ObjectNode result = Json.newObject();
        result.put("error", "Not found");
        return notFound(result);
      } else {
        ObjectNode objectNode = Json.newObject();
        objectNode.put("size", tvShow.seasons.size());
        // contar numero episodio por temporada
        jsonNode.forEach(season -> ((ObjectNode)season).put("episodeCount", seasonService.getSeasonByNumber(tvShow, season.get("seasonNumber").asInt()).episodes.size()));
        Boolean following = tvShowService.checkFollowTvShow(tvShow.id, roles.getUser(Http.Context.current()).id);
        objectNode.put("following", following);
        // si el usuario sigue la serie, comprobar cuántos episodio de cada temporada no ha visto
        if (following) {
          Integer totalEpisodes = 0;
          Integer totalSeenEpisodes = 0;
          for (JsonNode season : jsonNode) {
            Integer seasonEpisodesCount = 0;
            Integer seasonSeenEpisodesCount = 0;

            Season seasonObject = seasonService.getSeasonByNumber(tvShowService.find(tvShow.id), season.get("seasonNumber").asInt());
            if (seasonObject != null) {
              seasonEpisodesCount = seasonObject.episodes.size();
              seasonSeenEpisodesCount = episodeSeenService.getSeasonEpisodesSeen(seasonObject, roles.getUser(Http.Context.current()).id).size();
            }
            totalEpisodes += seasonEpisodesCount;
            totalSeenEpisodes += seasonSeenEpisodesCount;

            // mostrar en cada temporada numero de episodios vistos y no vistos
            ((ObjectNode)season).put("seenCount", seasonSeenEpisodesCount);
            ((ObjectNode)season).put("unseenCount", seasonEpisodesCount - seasonSeenEpisodesCount);
            // mostrar en la serie numero de episodio total, numero de episodios vistos total y numero de episodio no vistos total
            objectNode.put("episodeCount", totalEpisodes);
            objectNode.put("seenCount", totalSeenEpisodes);
            objectNode.put("unseenCount", totalEpisodes - totalSeenEpisodes);
          }

        }
        objectNode.set("seasons", jsonNode);
        return ok(objectNode);
      }
    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

  // devolver una temporada por tv show id y numero de temporada
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result seasonByTvShowIdAndSeasonNumber(Integer tvShowId, Integer seasonNumber) {
    TvShow tvShow = tvShowService.find(tvShowId);
    if (tvShow == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    try {
      JsonNode jsonNode = jsonUtils.jsonParseObject(seasonService.getSeasonByNumber(tvShow, seasonNumber), JsonViews.FullAll.class);
      if (jsonNode != null) {
        Boolean following = tvShowService.checkFollowTvShow(tvShow.id, roles.getUser(Http.Context.current()).id);
        ((ObjectNode)jsonNode).put("following", following);
        Integer episodesCount = jsonNode.withArray("episodes").size();
        ((ObjectNode)jsonNode).put("episodesCount", episodesCount);
        // si el usuario sigue la serie, comprobar cuántos episodio de cada temporada no ha visto
        if (following) {
          Integer seenEpisodesCount = 0;
          for (JsonNode episode : jsonNode.withArray("episodes")) {
            EpisodeSeen episodeSeen = episodeSeenService.findByEpisodeIdUserId(episode.get("id").asInt(), roles.getUser(Http.Context.current()).id);
            if (episodeSeen != null) {
              seenEpisodesCount++;
              ((ObjectNode)episode).put("seen", episodeSeen.date.toString());
            } else {
              String nullString = null;
              ((ObjectNode)episode).put("seen", nullString);
            }
          }
          ((ObjectNode)jsonNode).put("seenCount", seenEpisodesCount);
          ((ObjectNode)jsonNode).put("unseenCount", episodesCount - seenEpisodesCount);
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
