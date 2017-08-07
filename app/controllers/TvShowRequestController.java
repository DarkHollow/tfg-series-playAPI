package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import json.TvShowViews;
import models.TvShow;
import models.TvShowRequest;
import models.service.TvShowRequestService;
import models.service.TvShowService;
import models.service.UserService;
import models.service.tvdb.TvdbService;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Auth;

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
        // tenemos la serie en local ?
        TvShow localTvShow = tvShowService.findByTvdbId(tvShow.tvdbId);
        if (localTvShow != null) {
          tvShow.id = localTvShow.id;
          tvShow.local = true;
        } else {
          tvShow.local = false;
        }

        // hay request ?
        TvShowRequest request = tvShowRequestService.findTvShowRequestByTvdbId(tvShow.tvdbId);
        if (request != null) {
          tvShow.requestStatus = request.status.toString();
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

  // Peticion PUT Request TV Show aceptada
  // aceptar request y obtener datos de TV Show
  @Transactional
  @Security.Authenticated(Auth.class)
  public Result acceptTvShowRequest() {
    ObjectNode result = Json.newObject();
    Integer requestId;

    // obtenemos datos de la petición post
    DynamicForm requestForm = formFactory.form().bindFromRequest();

    try {
      requestId = Integer.valueOf(requestForm.get("requestId"));
    } catch (Exception ex) {
      result.put("error", "requestId null or not number");
      return badRequest(result);
    }

    // obtenemos request
    if (requestId != null) {
      TvShowRequest request = tvShowRequestService.findById(requestId);

      if (request != null) {
        // comprobamos que la request no esté en otro estado que 'Requested' por asincronía
        if (request.status.equals(TvShowRequest.Status.Requested)) {
          // ponemos la request en proceso
          request.status = TvShowRequest.Status.Processing;

          Integer tvdbId = request.tvdbId;

          // comprobamos que no tenemos el tv show por cualquier casualidad del mundo en la bbdd
          if (tvShowService.findByTvdbId(tvdbId) == null) {
            // obtenemos tv show
            TvShow tvShow = tvdbService.getTvShowTVDB(tvdbId);
            if (tvShow != null) {

              // persistimos serie nueva y respuesta ok
              tvShow = tvShowService.create(tvShow);
              result.put("ok", "Serie persistida");

              // descargamos las imagenes del TV Show
              // obtenemos banner
              tvShow.banner = tvdbService.getBanner(tvShow);
              if (tvShow.banner != null) {
                tvShow.banner = tvShow.banner.replace("public", "assets");
                // banner obtenido
                result.put("banner", true);
              } else {
                // no se ha podido obtener el banner
                Logger.info(tvShow.name + " - no se ha podido obtener el banner");
                result.put("banner", false);
              }

              // obtenemos poster
              tvShow.poster = tvdbService.getImage(tvShow, "poster");
              if (tvShow.poster != null) {
                tvShow.poster = tvShow.poster.replace("public", "assets");
                // poster obtenido
                result.put("poster", true);
              } else {
                // no se ha podido obtener el poster
                Logger.info(tvShow.name + " - no se ha podido obtener el poster");
                result.put("poster", false);
              }

              // obtenemos fanart
              tvShow.fanart = tvdbService.getImage(tvShow, "fanart");
              if (tvShow.fanart != null) {
                tvShow.fanart = tvShow.fanart.replace("public", "assets");
                // fanart obtenido
                result.put("fanart", true);
              } else {
                // no se ha podido obtener el fanart
                Logger.info(tvShow.name + " - no se ha podido obtener el fanart");
                result.put("fanart", false);
              }

              // poner request como persistida
              request.status = TvShowRequest.Status.Persisted;

              // respuesta ok - devolvemos datos obtenidos
              try {
                JsonNode jsonNode = Json.parse(new ObjectMapper()
                        .writerWithView(TvShowViews.FullTvShow.class)
                        .writeValueAsString(tvShow));
                result.set("tvShow", jsonNode);
                return ok(result);
              } catch (JsonProcessingException e) {
                Logger.error("Error parseando datos serie a JSON");
                // si da error parseando devolvemos el ok y si hemos obtenido las imagenes
                return ok(result);
              }

            } else {
              // no podemos obtener serie de TVDB
              request.status = TvShowRequest.Status.Requested;
              result.put("error", "Imposible conectar con el servicio externo");
              return internalServerError(result);
            }
          } else {
            // error - ya tenemos el tv show!
            request.status = TvShowRequest.Status.Persisted;
            result.put("error", "La serie se encuentra ya en nuestra BBDD");
            return badRequest(result);
          }
        } else {
          // estado no es Requested
          result.put("error", "La serie está en estado: " + request.status.toString());
          return badRequest(result);
        }
      } else {
        // la request no existe ?
        result.put("error", "request cannot be find");
        return badRequest(result);
      }

    } else {
      // peticion erronea ? tvdbId es null
      result.put("error", "tvdbId/userId can't be null");
      return badRequest(result);
    }
  }

  // Peticion PATCH Request TV Show rechazada
  // rechazar request
  @Transactional
  @Security.Authenticated(Auth.class)
  public Result rejectTvShowRequest() {
    ObjectNode result = Json.newObject();
    Integer requestId;

    // obtenemos datos de la petición post
    DynamicForm requestForm = formFactory.form().bindFromRequest();

    try {
      requestId = Integer.valueOf(requestForm.get("requestId"));
    } catch (Exception ex) {
      result.put("error", "requestId null or not number");
      return badRequest(result);
    }

    // obtenemos request
    if (requestId != null) {
      TvShowRequest request = tvShowRequestService.findById(requestId);

      if (request != null) {
        // comprobamos que la request no esté en otro estado que 'Requested' por asincronía
        if (request.status.equals(TvShowRequest.Status.Requested)) {
          // ponemos la request a rejected
          if (tvShowRequestService.reject(request.id)) {
            // peticion rechazada
            result.put("ok", "Serie rechazada con éxito");
            return ok(result);
          } else {
            // peticion no se ha podido rechazar
            result.put("error", "La petición no se ha podido rechazar en estos momentos");
            return badRequest(result);
          }
        } else {
          // estado no es Requested
          result.put("error", "La serie está en estado: " + request.status.toString());
          return badRequest(result);
        }
      } else {
        // la request no existe ?
        result.put("error", "request cannot be find");
        return badRequest(result);
      }

    } else {
      // peticion erronea ? tvdbId es null
      result.put("error", "tvdbId/userId can't be null");
      return badRequest(result);
    }
  }

}
