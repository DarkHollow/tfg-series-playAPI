package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import json.TvShowViews;
import models.TvShow;
import models.service.TvShowService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class TvShowController extends Controller {

  private final TvShowService tvShowService;

  @Inject
  public TvShowController(TvShowService tvShowService) {
    this.tvShowService = tvShowService;
  }

  // devolver todas los TV Shows (NOTE: futura paginacion?)
  // JSON View TvShowView.SearchTvShow: vista que solo incluye los campos
  // relevante de una búsqueda
  @Transactional(readOnly = true)
  public Result all() {
    List<TvShow> tvShows = tvShowService.all();

    // si la lista está vacía, not found
    if (tvShows.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = Json.parse(new ObjectMapper()
                                  .writerWithView(TvShowViews.SearchTvShow.class)
                                  .writeValueAsString(tvShows));
      return ok(jsonNode);

    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

  // devolver un TV Show por id
  @Transactional(readOnly = true)
  public Result tvShowById(Integer id) {
    TvShow tvShow = tvShowService.find(id);
    if (tvShow == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = Json.parse(new ObjectMapper()
                                  .writerWithView(TvShowViews.FullTvShow.class)
                                  .writeValueAsString(tvShow));
      return ok(jsonNode);

    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

  // devolver la busqueda de TV Shows LIKE
  @Transactional(readOnly = true)
  public Result searchTvShowNameLike(String query) {
    if (query.length() >= 3) {
      List<TvShow> tvShows = tvShowService.findBy("name", query, false);

      // si la lista está vacía, not found
      if (tvShows.isEmpty()) {
        ObjectNode result = Json.newObject();
        result.put("error", "Not found");
        return notFound(result);
      }

      // si la lista no está vacía, devolvemos datos
      try {
        JsonNode jsonNode = Json.parse(new ObjectMapper()
                                    .writerWithView(TvShowViews.SearchTvShow.class)
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
