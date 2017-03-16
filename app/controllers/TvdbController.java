package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import json.TvShowViews;
import models.TvShow;
import models.service.TvdbService;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class TvdbController extends Controller {

  private final TvdbService tvdbService;
  private final FormFactory formFactory;

  @Inject
  public TvdbController(TvdbService tvdbService, FormFactory formFactory) {
    this.tvdbService = tvdbService;
    this.formFactory = formFactory;
  }

  // buscar TV Show en TVDB y marcar las locales
  // devolver la busqueda de TV Show LIKE
  @Transactional(readOnly = true)
  public Result searchTvShowTVDBbyName(String query) {
    if (query.length() >= 3) {
      List<TvShow> tvShows = tvdbService.findOnTVDBby("name", query);

      // si la lista está vacía, not found
      if (tvShows.isEmpty()) {
        ObjectNode result = Json.newObject();
        result.put("error", "Not found");
        return notFound(result);
      }

      // si la lista no está vacía, devolvemos datos
      try {
        JsonNode jsonNode = Json.parse(new ObjectMapper()
                                    .writerWithView(TvShowViews.SearchTVDB.class)
                                    .writeValueAsString(tvShows));
        return ok(jsonNode);

      } catch (Exception ex) {
        // si hubiese un error, devolver error interno
        ObjectNode result = Json.newObject();
        result.put("error", "It can't be processed");
        return internalServerError(result);
      }
    } else {
      ObjectNode result = Json.newObject();
      result.put("error", "Bad request");
      return badRequest(result);
    }
  }

}
