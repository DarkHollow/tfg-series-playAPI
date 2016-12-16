package controllers;

import play.mvc.*;
import play.libs.Json;
import play.db.jpa.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Serie;
import models.service.SerieService;
import java.util.ArrayList;
import java.util.List;

public class SerieController extends Controller {

  // devolver todas las series (NOTE: futura paginacion?)
  @Transactional(readOnly = true)
  public Result all() {
    List<Serie> series = SerieService.all();
    if (series.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }
    return ok(Json.toJson(series));
  }

  // devolver una serie por id
  @Transactional(readOnly = true)
  public Result serieById(Integer id) {
    Serie serie = SerieService.find(id);
    if (serie == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }
    return ok(Json.toJson(serie));
  }

  // devolver la busqueda de series LIKE
  @Transactional(readOnly = true)
  public Result searchSeriesNameLike(String query) {
    List<Serie> series = SerieService.findBy("seriesName", query, false);
    if (series.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }
    return ok(Json.toJson(series));
  }

}
