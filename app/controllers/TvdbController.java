package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import json.SerieViews;
import models.Serie;
import models.service.TvdbService;
import play.data.DynamicForm;
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

  // buscar series en TVDB y marcar las locales
  // devolver la busqueda de series LIKE
  @Transactional(readOnly = true)
  public Result searchSeriesTVDBbyName(String query) {
    if (query.length() >= 3) {
      List<Serie> series = tvdbService.findOnTVDBby("name", query);

      // si la lista está vacía, not found
      if (series.isEmpty()) {
        ObjectNode result = Json.newObject();
        result.put("error", "Not found");
        return notFound(result);
      }

      // si la lista no está vacía, devolvemos datos
      try {
        JsonNode jsonNode = Json.parse(new ObjectMapper()
                                    .writerWithView(SerieViews.SearchTVDB.class)
                                    .writeValueAsString(series));
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
