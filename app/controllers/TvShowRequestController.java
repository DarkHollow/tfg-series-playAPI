package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Popular;
import models.TvShow;
import models.TvShowRequest;
import models.service.*;
import models.service.external.TmdbService;
import models.service.external.TvdbService;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Administrator;
import utils.Security.Roles;
import utils.Security.User;
import utils.json.JsonViews;

public class TvShowRequestController extends Controller {

  private final TvdbService tvdbService;
  private final TmdbService tmdbService;
  private final TvShowService tvShowService;
  private final SeasonService seasonService;
  private final EpisodeService episodeService;
  private final PopularService popularService;
  private final TvShowRequestService tvShowRequestService;
  private final UserService userService;
  private final FormFactory formFactory;
  private final utils.Security.User userAuth;
  private final utils.json.Utils jsonUtils;

  @Inject
  public TvShowRequestController(TvdbService tvdbService, TmdbService tmdbService, TvShowService tvShowService,
                                 SeasonService seasonService, EpisodeService episodeService,
                                 PopularService popularService, TvShowRequestService tvShowRequestService,
                                 UserService userService, FormFactory formFactory, utils.Security.User userAuth,
                                 utils.json.Utils jsonUtils) {
    this.tvdbService = tvdbService;
    this.tmdbService = tmdbService;
    this.tvShowService = tvShowService;
    this.seasonService = seasonService;
    this.episodeService = episodeService;
    this.popularService = popularService;
    this.tvShowRequestService = tvShowRequestService;
    this.userService = userService;
    this.formFactory = formFactory;
    this.userAuth = userAuth;
    this.jsonUtils = jsonUtils;
  }

  // Peticion POST TvShowRequest
  // realizar la petición del TV Show
  @Transactional
  @Security.Authenticated(User.class)
  public Result create() {
    ObjectNode result = Json.newObject();
    Integer tvdbId;

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(userAuth.getUsername(Http.Context.current()));
    // obtenemos tvdbId de la petición post
    DynamicForm requestForm = formFactory.form().bindFromRequest();

    try {
      tvdbId = Integer.valueOf(requestForm.get("tvdbId"));
    } catch (Exception ex) {
      result.put("error", "tvdbId null or not number");
      return badRequest(result);
    }

    // comprobamos que exista en tvdb y que no la tengamos en local
    if (tvdbId != null && user != null) {
      TvShow tvShow;
      try {
        tvShow = tvdbService.findOnTvdbByTvdbId(tvdbId);
        if (tvShow != null) {
          // comprobamos que no esté en local
          if (tvShowService.findByTvdbId(tvShow.tvdbId) == null) {
            // intentamos hacer la peticion
            TvShowRequest request = new TvShowRequest(tvdbId, user);
            request = tvShowRequestService.create(request);
            if (request != null) {
              result.put("ok", "TV Show request done");
              response().setHeader("Location", "/api/requests/" + request.id);
              return created(result);
            } else {
              // tv show ya solicitado?
              result.put("error", "TV Show already requested");
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
      } catch (Exception ex) {
        result.put("error", "cannot connect with external API");
        return status(504, result); // gateway timeout
      }
    } else {
      // peticion erronea ? tvdbId es null
      result.put("error", "user not valid or tvdbId is null");
      return badRequest(result);
    }
  }

  // Peticion PATCH Request TV Show (update)
  @Transactional
  @Security.Authenticated(Roles.class)
  public Result update(Integer requestId) {
    ObjectNode result = Json.newObject();

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(request().username());
    // obtenemos estado nuevo de la petición post
    String status;
    DynamicForm requestForm = formFactory.form().bindFromRequest();

    try {
      status = requestForm.get("status");
    } catch (Exception ex) {
      result.put("error", "state not valid");
      return badRequest(result);
    }

    // comprobación rápida de autorización, usuarios normales no pueden realizar ciertas actualizaciones
    if (user.rol.equals("u") && (status.equals("Persisted") || status.equals("Rejected") || status.equals("Deleted"))) {
      return unauthorized();
    }

    // obtenemos request
    if (requestId != null) {
      TvShowRequest request = tvShowRequestService.findById(requestId);
      if (request != null) {
        TvShowRequest.Status actualStatus = request.status;
        // actualizamos status de la request
        if (tvShowRequestService.update(request, null, TvShowRequest.Status.Processing) != null) {
          TvShowRequest requestResult = tvShowRequestService.update(request, user, status);
          if (requestResult != null) {
            result.put("ok", "Petición actualizada con éxito");
            return ok(result);
          } else {
            result.put("error", "request cannot be updated");
            result.put("attempt", request.status.toString() + " -> " + status);
            // reseteamos estado
            tvShowRequestService.update(request, null, actualStatus);
            return badRequest(result);
          }
        } else {
          result.put("error", "request cannot be updated");
          return internalServerError(result);
        }
      } else {
        // la request no existe
        result.put("error", "request not found");
        return notFound(result);
      }
    } else {
      // no se encuentra el parametro
      result.put("error", "query parameter requestId can't be null");
      return badRequest(result);
    }
  }

  // Peticion PUT Request TV Show serie aprobada o reaprobada
  // aceptar request y crear tv show
  @Transactional
  @Security.Authenticated(Administrator.class)
  public Result newTvShow(Integer requestId) {
    ObjectNode result = Json.newObject();
    // obtenemos request
    if (requestId != null) {
      TvShowRequest request = tvShowRequestService.findById(requestId);

      if (request != null) {
        TvShowRequest.Status actualStatus = request.status;
        // comprobamos que la request no esté en otro estado que 'Requested', 'Rejected' o 'Deleted' por asincronía
        if (request.status.equals(TvShowRequest.Status.Requested) ||
                request.status.equals(TvShowRequest.Status.Rejected) ||
                request.status.equals(TvShowRequest.Status.Deleted)) {
          // ponemos la request en proceso
          request = tvShowRequestService.update(request, null, TvShowRequest.Status.Processing);
          if (request != null) {
            Integer tvdbId = request.tvdbId;
            // comprobamos que no tenemos el tv show por cualquier casualidad del mundo en la bbdd
            if (tvShowService.findByTvdbId(tvdbId) == null) {
              // obtenemos tv show
              try {
                TvShow tvShow = tvdbService.getTvShowTVDB(tvdbId);
                if (tvShow != null) {
                  // persistimos
                  tvShow = tvShowService.create(tvShow);
                  if (tvShow != null) {
                    result.put("ok", "Serie persistida");
                    // descargamos las imagenes del TV Show y la seteamos al show
                    result.put("banner", tvShowService.getAndSetImage(tvShow, "banner"));
                    result.put("poster", tvShowService.getAndSetImage(tvShow, "poster"));
                    result.put("fanart", tvShowService.getAndSetImage(tvShow, "fanart"));

                    // popularidad
                    Popular popular = new Popular();
                    popular.tvShow = tvShow;
                    tvShow.popular = popularService.create(popular);

                    // TMDb
                    TvShow tmdbShow = tmdbService.findByTvdbId(tvShow.tvdbId);
                    if (tmdbShow != null) {
                      // obtenemos tmdbId
                      tvShow.tmdbId = tmdbShow.tmdbId;
                      // obtenemos temporadas vacías
                      seasonService.setSeasons(tvShow, seasonService.getSeasonsFromTmdbByTmdbId(tvShow.tmdbId));
                      // refrescamos tvShow
                      tvShow = tvShowService.find(tvShow.id);
                      // obtenemos las temporadas completas (sin episodios)
                      result.put("seasons", seasonService.fullfillSeasons(tvShow));
                      // obtenemos episodios de cada temporada
                      result.put("episodes", episodeService.setEpisodesAllSeasonsFromTmdb(tvShow));
                    } else {
                      result.put("seasons", false);
                      result.put("episodes", false);
                      Logger.info("No se ha podido obtener la serie en TMDB, por lo tanto, tampoco sus temporadas ni episodios");
                    }

                    // poner request como persistida
                    tvShowRequestService.update(request, null, TvShowRequest.Status.Persisted);

                    // respuesta ok - devolvemos datos obtenidos
                    try {
                      JsonNode jsonNode = jsonUtils.jsonParseObject(tvShow, JsonViews.FullTvShow.class);
                      result.set("tvShow", jsonNode);
                      return ok(result);
                    } catch (JsonProcessingException e) {
                      Logger.error("Error parseando datos serie a JSON");
                      // si da error parseando devolvemos el ok y si hemos obtenido las imagenes
                      return ok(result);
                    }
                  } else {
                    result.put("error", "cannot create tv show");
                    // reseteamos estado
                    tvShowRequestService.update(request, null, actualStatus);
                    return internalServerError(result);
                  }
                } else {
                  // no podemos obtener serie de TVDB
                  // reseteamos estado
                  tvShowRequestService.update(request, null, actualStatus);
                  result.put("error", "cannot connect with external API");
                  return internalServerError(result);
                }
              } catch (Exception ex) {
                Logger.error(ex.getClass().toString());
                // reseteamos estado
                tvShowRequestService.update(request, null, actualStatus);
                result.put("error", "cannot connect with external API");
                return status(504, result); // gateway timeout
              }
            } else {
              // error - ya tenemos el tv show!
              tvShowRequestService.update(request, null, TvShowRequest.Status.Persisted);
              result.put("error", "La serie se encuentra ya en nuestra BBDD");
              return badRequest(result);
            }
          } else {
            result.put("error", "No se puede actualizar el estado de la petición");
            return badRequest(result);
          }
        } else {
          // estado no es Requested o Deleted
          result.put("error", "La serie está en estado: " + request.status.toString());
          return badRequest(result);
        }
      } else {
        // la request no existe ?
        result.put("error", "request cannot be find");
        return notFound(result);
      }
    } else {
      // peticion erronea ? tvdbId es null
      result.put("error", "tvdbId can't be null");
      Logger.debug(result.toString());
      return badRequest(result);
    }
  }

}
