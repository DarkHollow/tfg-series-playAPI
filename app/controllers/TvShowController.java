package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.*;
import models.service.*;
import models.service.external.TvdbService;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Administrator;
import utils.Security.Roles;
import utils.json.JsonViews;

import java.util.List;

public class TvShowController extends Controller {

  private final TvShowService tvShowService;
  private final TvShowRequestService tvShowRequestService;
  private final SeasonService seasonService;
  private final EpisodeService episodeService;
  private final PopularService popularService;
  private final EpisodeSeenService episodeSeenService;
  private final TvdbService tvdbService;
  private final Roles roles;
  private final FormFactory formFactory;
  private final utils.json.Utils jsonUtils;

  @Inject
  public TvShowController(TvShowService tvShowService, SeasonService seasonService, EpisodeService episodeService,
                          PopularService popularService, TvdbService tvdbService, FormFactory formFactory,
                          TvShowRequestService tvShowRequestService, EpisodeSeenService episodeSeenService,
                          utils.json.Utils jsonUtils, Roles roles) {
    this.tvShowService = tvShowService;
    this.tvShowRequestService = tvShowRequestService;
    this.seasonService = seasonService;
    this.episodeService = episodeService;
    this.popularService = popularService;
    this.episodeSeenService = episodeSeenService;
    this.tvdbService = tvdbService;
    this.roles = roles;
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

    // actualizar popularity
    Integer popularity = popularService.growPopularity(tvShow);

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = jsonUtils.jsonParseObject(tvShow, JsonViews.FullTvShow.class);

      // contar numero episodio por temporada
      jsonNode.withArray("seasons").forEach(season -> ((ObjectNode)season).put("episodeCount", seasonService.getSeasonByNumber(tvShow, season.get("seasonNumber").asInt()).episodes.size()));
      // mostrar si el usuario identificado sigue la serie o no
      Boolean following = tvShowService.checkFollowTvShow(tvShow.id, roles.getUser(Http.Context.current()).id);
      ((ObjectNode) jsonNode).put("following", following);
      // si el usuario sigue la serie, comprobar cuántos episodio de cada temporada no ha visto
      if (following) {
        Integer totalEpisodes = 0;
        Integer totalSeenEpisodes = 0;

        for (JsonNode season : jsonNode.withArray("seasons")) {
          Integer seasonEpisodesCount = 0;
          Integer seasonSeenEpisodesCount = 0;

          Season seasonObject = seasonService.getSeasonByNumber(tvShow, season.get("seasonNumber").asInt());
          if (seasonObject != null) {
            seasonEpisodesCount = seasonObject.episodes.size();
            seasonSeenEpisodesCount = episodeSeenService.getSeasonEpisodesSeen(seasonObject, roles.getUser(Http.Context.current()).id).size();
          }

          totalEpisodes += seasonEpisodesCount;
          totalSeenEpisodes += seasonSeenEpisodesCount;

          // mostrar en cada temporada numero de episodios vistos y no vistos
          ((ObjectNode)season).put("seenCount", seasonSeenEpisodesCount);
          ((ObjectNode)season).put("unseenCount", seasonEpisodesCount - seasonSeenEpisodesCount);
        }
        // mostrar en la serie numero de episodio total, numero de episodios vistos total y numero de episodio no vistos total
        ((ObjectNode) jsonNode).put("episodeCount", totalEpisodes);
        ((ObjectNode) jsonNode).put("seenCount", totalSeenEpisodes);
        ((ObjectNode) jsonNode).put("unseenCount", totalEpisodes - totalSeenEpisodes);
      }
      // mostramos popularity
      ((ObjectNode) jsonNode).put("popularity", popularity);
      // mostramos trend
      ((ObjectNode) jsonNode).put("trend", tvShow.popular.getTrend());

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
              tvShow = episodeService.updateEpisodes(tvShow);
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

  // devolver las series mejor valoradas
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result getTopRated(Integer querySize) {
    Integer size = 5;
    Integer minimum = 1;
    Integer maximum = 10;
    // comprobar si tiene tamaño en la query
    if (querySize != null && querySize > minimum && querySize <= maximum) {
      size = querySize;
    }

    List<TvShow> topRated = tvShowService.getTopRatedTvShows(size);
    // si la lista está vacía, not found
    if (topRated.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    } else {
      // si la lista no está vacía, devolvemos datos
      try {
        JsonNode jsonNode = jsonUtils.jsonParseObject(topRated, JsonViews.FullTvShow.class);
        ObjectNode objectNode = Json.newObject();
        // añadimos popularidad y tendencia de cada serie
        final Integer[] i = {0};
        jsonNode.forEach((tvShow) -> {
          i[0]++;
          Popular popular = tvShowService.find(tvShow.get("id").asInt()).popular;
          ((ObjectNode)tvShow).put("poster", popular.tvShow.poster);
          ((ObjectNode)tvShow).put("top", i[0]);
          // mostrar si el usuario identificado sigue la serie o no
          Boolean following = tvShowService.checkFollowTvShow(tvShow.get("id").asInt(), roles.getUser(Http.Context.current()).id);
          ((ObjectNode)tvShow).put("following", following);
          // si el usuario sigue la serie, comprobar cuántos episodio de cada temporada no ha visto
          if (following) {
            Integer totalEpisodes = 0;
            Integer totalSeenEpisodes = 0;
            for (JsonNode season : tvShow.withArray("seasons")) {

              Integer seasonEpisodesCount = 0;
              Integer seasonSeenEpisodesCount = 0;

              Season seasonObject = seasonService.getSeasonByNumber(tvShowService.find(tvShow.get("id").asInt()), season.get("seasonNumber").asInt());
              if (seasonObject != null) {
                seasonEpisodesCount = seasonObject.episodes.size();
                seasonSeenEpisodesCount = episodeSeenService.getSeasonEpisodesSeen(seasonObject, roles.getUser(Http.Context.current()).id).size();
              }
              totalEpisodes += seasonEpisodesCount;
              totalSeenEpisodes += seasonSeenEpisodesCount;
            }
            // mostrar en la serie numero de episodio total, numero de episodios vistos total y numero de episodio no vistos total
            ((ObjectNode) tvShow).put("episodeCount", totalEpisodes);
            ((ObjectNode) tvShow).put("seenCount", totalSeenEpisodes);
            ((ObjectNode) tvShow).put("unseenCount", totalEpisodes - totalSeenEpisodes);
          }
          // por ultimo, borrar los campos innecesarios que han sido necesarios poner para calcular...
          // TODO: mejorar todo esto !
          ((ObjectNode) tvShow).remove("tvdbId");
          ((ObjectNode) tvShow).remove("tmdbId");
          ((ObjectNode) tvShow).remove("overview");
          ((ObjectNode) tvShow).remove("banner");
          ((ObjectNode) tvShow).remove("fanart");
          ((ObjectNode) tvShow).remove("network");
          ((ObjectNode) tvShow).remove("runtime");
          ((ObjectNode) tvShow).remove("genre");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("status");
          ((ObjectNode) tvShow).remove("tvShowVotes");
          ((ObjectNode) tvShow).remove("seasons");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("rating");
        });
        // añadimos tamaño
        objectNode.put("size", topRated.size());
        // añadimos series
        objectNode.set("tvShows", jsonNode);
        return ok(objectNode);
      } catch (Exception ex) {
        // si hubiese un error, devolver error interno
        Logger.debug(ex.getMessage());
        ObjectNode result = Json.newObject();
        result.put("error", "It can't be processed");
        return internalServerError(result);
      }
    }

  }

  // seguir una serie
  @Transactional
  @Security.Authenticated(Roles.class)
  public Result follow(Integer tvShowId) {
    if (tvShowService.followTvShow(tvShowId, roles.getUser(Http.Context.current()).id)) {
      try {
        return noContent();
      } catch (Exception ex) {
        // si hubiese un error, devolver error interno
        ObjectNode result = Json.newObject();
        result.put("error", "It can't be processed");
        return internalServerError(result);
      }
    } else {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

  }

  // dejar deseguir una serie
  @Transactional
  @Security.Authenticated(Roles.class)
  public Result unfollow(Integer tvShowId) {
    if (tvShowService.unfollowTvShow(tvShowId, roles.getUser(Http.Context.current()).id)) {
      try {
        return noContent();
      } catch (Exception ex) {
        // si hubiese un error, devolver error interno
        ObjectNode result = Json.newObject();
        result.put("error", "It can't be processed");
        return internalServerError(result);
      }
    } else {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

  }

  // comprobar seguimiento de una serie
  @Transactional
  @Security.Authenticated(Roles.class)
  public Result followCheck(Integer tvShowId) {
    if (tvShowService.checkFollowTvShow(tvShowId, roles.getUser(Http.Context.current()).id)) {
      return noContent();
    } else {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }
  }

  // devolver listado de ids de series que sigue el usuario identificado
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result following() {
    List<TvShow> tvShows = tvShowService.getFollowingTvShows(roles.getUser(Http.Context.current()).id);
    if (tvShows.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    } else {
      try {
        JsonNode jsonNode = jsonUtils.jsonParseObject(tvShows, JsonViews.FullTvShow.class);
        ObjectNode objectNode = Json.newObject();
        // añadimos popularidad y tendencia de cada serie
        final Integer[] i = {0};
        jsonNode.forEach((tvShow) -> {
          i[0]++;
          Popular popular = tvShowService.find(tvShow.get("id").asInt()).popular;
          ((ObjectNode)tvShow).put("poster", popular.tvShow.poster);
          ((ObjectNode)tvShow).put("top", i[0]);
          // mostrar si el usuario identificado sigue la serie o no
          Boolean following = tvShowService.checkFollowTvShow(tvShow.get("id").asInt(), roles.getUser(Http.Context.current()).id);
          ((ObjectNode)tvShow).put("following", following);
          // si el usuario sigue la serie, comprobar cuántos episodio de cada temporada no ha visto
          if (following) {
            Integer totalEpisodes = 0;
            Integer totalSeenEpisodes = 0;
            for (JsonNode season : tvShow.withArray("seasons")) {

              Integer seasonEpisodesCount = 0;
              Integer seasonSeenEpisodesCount = 0;

              Season seasonObject = seasonService.getSeasonByNumber(tvShowService.find(tvShow.get("id").asInt()), season.get("seasonNumber").asInt());
              if (seasonObject != null) {
                seasonEpisodesCount = seasonObject.episodes.size();
                seasonSeenEpisodesCount = episodeSeenService.getSeasonEpisodesSeen(seasonObject, roles.getUser(Http.Context.current()).id).size();
              }
              totalEpisodes += seasonEpisodesCount;
              totalSeenEpisodes += seasonSeenEpisodesCount;
            }
            // mostrar en la serie numero de episodio total, numero de episodios vistos total y numero de episodio no vistos total
            ((ObjectNode) tvShow).put("episodeCount", totalEpisodes);
            ((ObjectNode) tvShow).put("seenCount", totalSeenEpisodes);
            ((ObjectNode) tvShow).put("unseenCount", totalEpisodes - totalSeenEpisodes);
          }
          // por ultimo, borrar los campos innecesarios que han sido necesarios poner para calcular...
          // TODO: mejorar todo esto !
          ((ObjectNode) tvShow).remove("tvdbId");
          ((ObjectNode) tvShow).remove("tmdbId");
          ((ObjectNode) tvShow).remove("overview");
          ((ObjectNode) tvShow).remove("banner");
          ((ObjectNode) tvShow).remove("fanart");
          ((ObjectNode) tvShow).remove("network");
          ((ObjectNode) tvShow).remove("runtime");
          ((ObjectNode) tvShow).remove("genre");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("status");
          ((ObjectNode) tvShow).remove("tvShowVotes");
          ((ObjectNode) tvShow).remove("seasons");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("rating");
        });
        // añadimos tamaño
        objectNode.put("size", tvShows.size());
        // añadimos series
        objectNode.set("tvShows", jsonNode);
        return ok(objectNode);
      } catch (Exception ex) {
        // si hubiese un error, devolver error interno
        Logger.debug(ex.getMessage());
        ObjectNode result = Json.newObject();
        result.put("error", "It can't be processed");
        return internalServerError(result);
      }
    }
  }

  // devolver las series populares con mejor ratio en twitter
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result getTopTwitter() {
    final Integer SIZE = 5;

    List<TvShow> topTwitter = tvShowService.getTvShowsFromPopulars(popularService.getTwitterPopular()).subList(0, SIZE);
    // si la lista está vacía, not found
    if (topTwitter.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    } else {
      // si la lista no está vacía, devolvemos datos
      try {
        JsonNode jsonNode = jsonUtils.jsonParseObject(topTwitter, JsonViews.FullTvShow.class);
        ObjectNode objectNode = Json.newObject();
        // añadimos popularidad y tendencia de cada serie
        final Integer[] i = {0};
        jsonNode.forEach((tvShow) -> {
          i[0]++;
          Popular popular = tvShowService.find(tvShow.get("id").asInt()).popular;
          ((ObjectNode)tvShow).put("poster", popular.tvShow.poster);
          ((ObjectNode)tvShow).put("top", i[0]);
          ((ObjectNode)tvShow).put("ratio", popular.tvShow.twitterRatio);

          // mostrar si el usuario identificado sigue la serie o no
          Boolean following = tvShowService.checkFollowTvShow(tvShow.get("id").asInt(), roles.getUser(Http.Context.current()).id);
          ((ObjectNode)tvShow).put("following", following);
          // si el usuario sigue la serie, comprobar cuántos episodio de cada temporada no ha visto
          if (following) {
            Integer totalEpisodes = 0;
            Integer totalSeenEpisodes = 0;
            for (JsonNode season : tvShow.withArray("seasons")) {

              Integer seasonEpisodesCount = 0;
              Integer seasonSeenEpisodesCount = 0;

              Season seasonObject = seasonService.getSeasonByNumber(tvShowService.find(tvShow.get("id").asInt()), season.get("seasonNumber").asInt());
              if (seasonObject != null) {
                seasonEpisodesCount = seasonObject.episodes.size();
                seasonSeenEpisodesCount = episodeSeenService.getSeasonEpisodesSeen(seasonObject, roles.getUser(Http.Context.current()).id).size();
              }
              totalEpisodes += seasonEpisodesCount;
              totalSeenEpisodes += seasonSeenEpisodesCount;
            }
            // mostrar en la serie numero de episodio total, numero de episodios vistos total y numero de episodio no vistos total
            ((ObjectNode) tvShow).put("episodeCount", totalEpisodes);
            ((ObjectNode) tvShow).put("seenCount", totalSeenEpisodes);
            ((ObjectNode) tvShow).put("unseenCount", totalEpisodes - totalSeenEpisodes);
          }
          // por ultimo, borrar los campos innecesarios que han sido necesarios poner para calcular...
          // TODO: mejorar todo esto !
          ((ObjectNode) tvShow).remove("tvdbId");
          ((ObjectNode) tvShow).remove("tmdbId");
          ((ObjectNode) tvShow).remove("overview");
          ((ObjectNode) tvShow).remove("banner");
          ((ObjectNode) tvShow).remove("fanart");
          ((ObjectNode) tvShow).remove("network");
          ((ObjectNode) tvShow).remove("runtime");
          ((ObjectNode) tvShow).remove("genre");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("status");
          ((ObjectNode) tvShow).remove("tvShowVotes");
          ((ObjectNode) tvShow).remove("seasons");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("rating");
          ((ObjectNode) tvShow).remove("rating");
        });
        // añadimos tamaño
        objectNode.put("size", topTwitter.size());
        // añadimos series
        objectNode.set("tvShows", jsonNode);
        return ok(objectNode);
      } catch (Exception ex) {
        // si hubiese un error, devolver error interno
        Logger.debug(ex.getMessage());
        ObjectNode result = Json.newObject();
        result.put("error", "It can't be processed");
        return internalServerError(result);
      }
    }

  }

}
