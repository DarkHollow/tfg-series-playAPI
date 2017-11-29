package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.service.EpisodeService;
import utils.json.JsonViews;
import models.TvShow;
import models.TvShowRequest;
import models.service.SeasonService;
import models.service.TvShowRequestService;
import models.service.TvShowService;
import models.service.external.TvdbService;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Administrator;
import utils.Security.Roles;

import java.util.List;

public class TvShowController extends Controller {

  private final TvShowService tvShowService;
  private final TvShowRequestService tvShowRequestService;
  private final SeasonService seasonService;
  private final EpisodeService episodeService;
  private final TvdbService tvdbService;
  private final FormFactory formFactory;
  private final utils.json.Utils jsonUtils;

  @Inject
  public TvShowController(TvShowService tvShowService, SeasonService seasonService, EpisodeService episodeService,
                          TvdbService tvdbService, FormFactory formFactory, TvShowRequestService tvShowRequestService,
                          utils.json.Utils jsonUtils) {
    this.tvShowService = tvShowService;
    this.tvShowRequestService = tvShowRequestService;
    this.seasonService = seasonService;
    this.episodeService = episodeService;
    this.tvdbService = tvdbService;
    this.formFactory = formFactory;
    this.jsonUtils = jsonUtils;
  }

  // devolver todas los TV Shows (NOTE: futura paginacion?)
  // JSON View TvShowView.SearchTvShow: vista que solo incluye los campos
  // relevante de una búsqueda
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result all(String search, Integer tvdb) {

    // comprobar si es búsqueda
    if (!search.isEmpty()) {
      // comprobamos si es búsqueda en tvdb
      if (tvdb == 1) {
        return searchTvShowTVDBbyName(search);
      } else {
        return searchTvShowNameLike(search);
      }
    }

    List<TvShow> tvShows = tvShowService.all();

    // si la lista está vacía, not found
    if (tvShows.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = jsonUtils.jsonParseObject(tvShows, JsonViews.SearchTvShow.class);
      return ok(jsonNode);
    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

  @Transactional
  @Security.Authenticated(Administrator.class)
  public Result create() {
    ObjectNode result = Json.newObject();

    Form<TvShow> tvShowForm = formFactory.form(TvShow.class).bindFromRequest();

    if (tvShowForm.hasErrors()) {
      result.put("error", "tv show data error");
      result.set("errors", tvShowForm.errorsAsJson());
      return badRequest(result);
    }

    try {
      TvShow tvShow = tvShowForm.get();
      // comprobamos si ya existe
      if (tvShowService.findByTvdbId(tvShow.tvdbId) == null) {
        tvShow = tvShowService.create(tvShow);
        if (tvShow != null) {
          result.put("ok", "tv show created");
          response().setHeader("Location", "/api/tvshows/" + tvShow.id);
          return created(result);
        } else {
          Logger.error("TV Show Controller create - tv show no creado");
          result.put("error", "tv show not created");
          return internalServerError(result);
        }
      } else {
        result.put("error", "tv show exists already (tvdb id conflict");
        return badRequest(result);
      }
    } catch (Exception ex) {
      Logger.error("TV Show Controller create - " + ex.getClass());
      result.put("error", "some error creating tv show");
      return internalServerError(result);
    }
  }

  // devolver un TV Show por id
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result tvShowById(Integer id) {
    TvShow tvShow = tvShowService.find(id);
    if (tvShow == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = jsonUtils.jsonParseObject(tvShow, JsonViews.FullTvShow.class);

      // contar numero episodio por temporada
      jsonNode.withArray("seasons").forEach(season -> ((ObjectNode)season).put("episodeCount", seasonService.getSeasonByNumber(tvShow, season.get("seasonNumber").asInt()).episodes.size()));

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
  private Result searchTvShowNameLike(String query) {
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
        return ok(jsonUtils.jsonParseObject(tvShows, JsonViews.SearchTvShow.class));
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

  // buscar TV Show en TVDB y marcar las locales
  // devolver la busqueda de TV Show LIKE
  @Transactional(readOnly = true)
  private Result searchTvShowTVDBbyName(String query) {
    if (query.length() >= 3) {
      try {
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
            tvShow.score = localTvShow.score;
            tvShow.voteCount = localTvShow.voteCount;
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
          JsonNode jsonNode = jsonUtils.jsonParseObject(tvShows, JsonViews.SearchTVDB.class);
          return ok(jsonNode);
        } catch (Exception ex) {
          // si hubiese un error, devolver error interno
          Logger.debug(ex.getClass().toString());
          ObjectNode result = Json.newObject();
          result.put("error", "It can't be processed");
          return internalServerError(result);
        }
      } catch (Exception ex) {
        ObjectNode result = Json.newObject();
        result.put("error", "cannot connect with external API");
        return status(504, result); // gateway timeout
      }

    } else {
      ObjectNode result = Json.newObject();
      result.put("error", "Bad request");
      return badRequest(result);
    }
  }

  @Transactional
  @Security.Authenticated(Administrator.class)
  public Result updateData(Integer id) {
    ObjectNode result = Json.newObject();
    TvShow tvShow = tvShowService.find(id);

    if (tvShow != null) {
      // comprobamos qué recurso ha de ser actualizado
      DynamicForm requestForm = formFactory.form().bindFromRequest();
      String request;
      try {
        request = requestForm.get("update");
      } catch (Exception ex) {
        result.put("error", "update null");
        return badRequest(result);
      }

      if (request != null) {
        switch (request) {
          case "data":
            try {
              tvShow = tvShowService.updateData(tvShow);
            } catch (Exception ex) {
              Logger.error("Actualizar datos serie - timeout con API externa");
              result.put("error", "cannot connect with external API");
              return status(504, result); // gateway timeout
            }
            break;
          case "seasons":
            try {
              tvShow = seasonService.updateSeasons(tvShow);
            } catch (Exception ex) {
              Logger.error("Actualizar temporadas serie - timeout con API externa");
              result.put("error", "cannot connect with external API");
              return status(504, result); // gateway timeout
            }
            break;
          case "episodes":
            try {
              tvShow = episodeService.updateEpisodes(tvShow);
            } catch (Exception ex) {
              Logger.error("Actualizar episodios serie - timeout con API externa");
              result.put("error", "cannot connect with external API");
              return status(504, result); // gateway timeout
            }
            break;
          case "images":
            // las imagenes deben ser una a una desde aquí
            result.put("banner", tvShowService.getAndSetImage(tvShow, "banner"));
            result.put("poster", tvShowService.getAndSetImage(tvShow, "poster"));
            result.put("fanart", tvShowService.getAndSetImage(tvShow, "fanart"));
            break;
          default:
            Logger.error("Actualizar " + request + ": el tipo de datos a actualizar de la serie no coincide con ninguno conocido: '" + request + "'");
            tvShow = null;
        }
      } else {
        result.put("error", "update null");
        return badRequest(result);
      }


      // si el tvShow no es null, se ha actualizado correctamente
      if (tvShow != null) {
        result.put("ok", "TV Show " + request + " successfully updated");
        result.put("message", "Serie " + request + "  actualizada correctamente");
        try {
          JsonNode jsonNode = jsonUtils.jsonParseObject(tvShow, JsonViews.FullTvShow.class);
          result.set("tvShow", jsonNode);
        } catch (JsonProcessingException e) {
          Logger.error("Error parseando datos serie a JSON, serie  " + request + "  actualizada igualmente");
        }
      } else {
        // no se ha podido actualizar
        result.put("error", "Not found");
        result.put("message", "Recurso no encontrado o error");
        return notFound(result);
      }
    } else {
      // no se ha podido actualizar
      result.put("error", "Not found");
      result.put("message", "Recurso no encontrado");
      return notFound(result);
    }

    return ok(result);
  }

  @Transactional
  @Security.Authenticated(Administrator.class)
  public Result delete(Integer id) {
    ObjectNode result = Json.newObject();
    TvShow tvShow = tvShowService.find(id);
    if (tvShow != null) {
      Integer tvdbId = tvShow.tvdbId;
      TvShowRequest request = tvShowRequestService.findTvShowRequestByTvdbId(tvdbId);
      if (request != null) {
        TvShowRequest.Status actualStatus = request.status;
        if (tvShowRequestService.update(request, null, TvShowRequest.Status.Processing) != null) {
          if (tvShowService.delete(id)) {
            result.put("ok", "tv show deleted");
            result.put("message", "serie eliminada");
            // cambiar estado de su petición
            if (tvShowRequestService.deleteTvShow(tvdbId)) {
              result.put("request", "petición pasada a Deleted");
            } else {
              result.put("request", "no se ha encontrado la request");
            }
          } else {
            result.put("error", "tv show not deleted");
            result.put("message", "serie no eliminada");
            tvShowRequestService.update(request, null, actualStatus);
            return notFound(result);
          }
          return ok(result);
        } else {
          result.put("error", "request cannot be updated");
          return internalServerError(result);
        }
      } else {
        // la serie no tiene request
        result.put("error", "tv show doesn't have request");
        return internalServerError(result);
      }
    } else {
      result.put("error", "tv show not found");
      return notFound(result);
    }
  }

}
