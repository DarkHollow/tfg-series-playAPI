package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import utils.json.JsonViews;
import models.TvShow;
import models.service.external.TvdbService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Administrator;

public class TvdbController extends Controller {

  private final TvdbService tvdbService;
  private final utils.json.Utils jsonUtils;

  @Inject
  public TvdbController(TvdbService tvdbService, utils.json.Utils jsonUtils) {
    this.tvdbService = tvdbService;
    this.jsonUtils = jsonUtils;
  }

  // buscar TV Show en TVDB por id
  @Transactional(readOnly = true)
  @Security.Authenticated(Administrator.class)
  public Result tvShowById(Integer tvdbId) {
    try {
      TvShow tvShow = tvdbService.findOnTvdbByTvdbId(tvdbId);
      if (tvShow != null) {
        try {
          JsonNode jsonNode = jsonUtils.jsonParseObject(tvShow, JsonViews.SearchTvShowTvdbId.class);
          // quitamos campos no relevantes
          ObjectNode object = (ObjectNode) jsonNode;
          object.remove("id");
          object.remove("score");
          object.remove("voteCount");

          return ok(object);
        } catch (Exception ex) {
          // si hubiese un error, devolver error interno
          ObjectNode result = Json.newObject();
          result.put("error", "It can't be processed");
          return internalServerError(result);
        }
      } else {
        ObjectNode result = Json.newObject();
        result.put("error", "Not found");
        return notFound(result);
      }
    } catch (Exception ex) {
      ObjectNode result = Json.newObject();
      result.put("error", "cannot connect with external API");
      return status(504, result); // gateway timeout
    }
  }

}
