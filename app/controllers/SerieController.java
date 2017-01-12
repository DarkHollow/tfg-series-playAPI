package controllers;

import play.mvc.*;
import play.libs.Json;
import play.db.jpa.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import json.SerieViews;
import models.Serie;
import models.service.SerieService;
import java.util.ArrayList;
import java.util.List;

public class SerieController extends Controller {

  // devolver todas las series (NOTE: futura paginacion?)
  // JSON View SeriesView.SearchSerie: vista que solo incluye los campos
  // relevante de una búsqueda
  @Transactional(readOnly = true)
  public Result all() {
    List<Serie> series = SerieService.all();

    // si la lista está vacía, not found
    if (series.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = Json.parse(new ObjectMapper()
                                  .writerWithView(SerieViews.SearchSerie.class)
                                  .writeValueAsString(series));
      return ok(jsonNode);

    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
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

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = Json.parse(new ObjectMapper()
                                  .writerWithView(SerieViews.FullSerie.class)
                                  .writeValueAsString(serie));
      return ok(jsonNode);

    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

  // devolver la busqueda de series LIKE
  @Transactional(readOnly = true)
  public Result searchSeriesNameLike(String query) {
    List<Serie> series = SerieService.findBy("seriesName", query, false);

    // si la lista está vacía, not found
    if (series.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = Json.parse(new ObjectMapper()
                                  .writerWithView(SerieViews.SearchSerie.class)
                                  .writeValueAsString(series));
      return ok(jsonNode);

    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

}
