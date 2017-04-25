package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import json.TvShowViews;
import models.TvShow;
import models.service.TvShowRequestService;
import models.service.TvShowService;
import models.service.UserService;
import models.service.tvdb.TvdbService;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class TvShowRequestController extends Controller {

  private final TvdbService tvdbService;
  private final TvShowService tvShowService;
  private final TvShowRequestService tvShowRequestService;
  private final UserService userService;
  private final FormFactory formFactory;

  @Inject
  public TvShowRequestController(TvdbService tvdbService, TvShowService tvShowService,
                                 TvShowRequestService tvShowRequestService, UserService userService,
                                 FormFactory formFactory) {
    this.tvdbService = tvdbService;
    this.tvShowService = tvShowService;
    this.tvShowRequestService = tvShowRequestService;
    this.userService = userService;
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

      // asignacion de campos TRANSIENT
      // si la lista no está vacía, comprobamos series en local
      for (TvShow tvShow : tvShows) {
        TvShow localTvShow = tvShowService.findByTvdbId(tvShow.tvdbId);
        if (localTvShow != null) {
          tvShow.id = localTvShow.id;
          tvShow.local = true;
        } else {
          // si no está en local, comprobamos si está solicitada
          tvShow.requested = !tvShowRequestService.findTvShowRequests(tvShow.tvdbId).isEmpty();
        }
      }

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
        // comprobamos que no esté en local
        if (tvShowService.findByTvdbId(tvShow.tvdbId) == null) {
          // comprobamos que exista el usuario
          if (userService.find(userId) != null) {
            // intentamos hacer la peticion
            if (tvShowRequestService.requestTvShow(tvdbId, userId)) {
              result.put("ok", "TV Show request done");
              return ok(result);
            } else {
              // tv show ya solicitado por este user?
              result.put("error", "This user requested this TV Show already");
              return badRequest(result);
            }
          } else {
            result.put("error", "This user doesn't exist");
            return badRequest(result);
          }
        } else {
          tvShow.local = true;
          result.put("error", "TV Show is on local already");
          return badRequest(result);
        }
      } else {
        // la tvShow no existe en TvdbConnection o ya la tenemos en local
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
