package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import json.TvShowViews;
import models.TvShow;
import models.service.tvdb.TvdbService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class TvdbController extends Controller {

  private final TvdbService tvdbService;

  @Inject
  public TvdbController(TvdbService tvdbService) {
    this.tvdbService = tvdbService;
  }

  // buscar TV Show en TVDB por id
  @Transactional(readOnly = true)
  public Result tvShowById(Integer tvdbId) {

    TvShow tvShow = tvdbService.findOnTvdbByTvdbId(tvdbId);
    if (tvShow != null) {
      try {
        JsonNode jsonNode = Json.parse(new ObjectMapper()
                .writerWithView(TvShowViews.SearchTvShowTvdbId.class)
                .writeValueAsString(tvShow));
        // quitamos campos no relevantes
        ObjectNode object = (ObjectNode) jsonNode;
        object.remove("id");
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

  }
}
