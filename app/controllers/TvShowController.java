package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import json.TvShowViews;
import models.TvShow;
import models.service.TvShowService;
import models.service.TvShowRequestService;
import models.service.TvdbService;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class TvShowController extends Controller {

  private final TvShowService tvShowService;
  private final TvdbService tvdbService;
  private final TvShowRequestService tvShowRequestService;
  private final FormFactory formFactory;

  @Inject
  public TvShowController(TvShowService tvShowService, TvdbService tvdbService, TvShowRequestService tvShowRequestService, FormFactory formFactory) {
    this.tvShowService = tvShowService;
    this.tvdbService = tvdbService;
    this.tvShowRequestService = tvShowRequestService;
    this.formFactory = formFactory;
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

  // Peticion POST TvShow nuevo
  // realizar la petición del TV Show
  @Transactional
  public Result requestTvShow() {
    ObjectNode result = Json.newObject();
    Integer tvdbId, userId;

    // obtenemos datos de la petición post
    DynamicForm requestForm = formFactory.form().bindFromRequest();

    try {
      tvdbId = Integer.valueOf(requestForm.get("tvdbId"));
      userId = Integer.valueOf(requestForm.get("userId"));
    } catch (Exception ex) {
      result.put("error", "tvdbId/userId null or not number");
      return badRequest(result);
    }

    // comprobamos que exista en tvdb y que no la tengamos en local
    if (tvdbId != null && userId != null) {
      TvShow tvShow = tvdbService.findOnTvdbByTvdbId(tvdbId);
      if (tvShow != null) {
        // comprobamos que esté en local
        if (!tvShow.local) {
          // intentamos hacer la peticion
          if (tvShowRequestService.requestTvShow(tvdbId, userId)) {
            result.put("ok", "TV Show request done");
            return ok(result);
          } else {
            // tvShow ya solicitada por este user?
            result.put("error", "This user requested this TV Show already");
            return badRequest(result);
          }
        } else {
          result.put("error", "TV Show is on local already");
          return badRequest(result);
        }

      } else {
        // la tvShow no existe en TVDB o ya la tenemos en local
        result.put("error", "TV Show doesn't exist on TVDB");
        return notFound(result);
      }
    } else {
      // peticion erronea ? tvdbId es null
      result.put("error", "tvdbId/userId can't be null");
      return badRequest(result);
    }
  }

}
