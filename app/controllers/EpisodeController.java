package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Season;
import models.TvShow;
import models.service.EpisodeService;
import models.service.SeasonService;
import models.service.TvShowService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Roles;
import utils.json.JsonViews;

import static play.mvc.Results.*;

public class EpisodeController {
  private final EpisodeService episodeService;
  private final SeasonService seasonService;
  private final TvShowService tvShowService;
  private final utils.json.Utils jsonUtils;

  @Inject
  public EpisodeController(EpisodeService episodeService, SeasonService seasonService, TvShowService tvShowService,
                           utils.json.Utils jsonUtils) {
    this.episodeService = episodeService;
    this.seasonService = seasonService;
    this.tvShowService = tvShowService;
    this.jsonUtils = jsonUtils;
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
        return ok(jsonNode);
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
