package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Episode;
import models.EpisodeSeen;
import models.TvShow;
import models.service.EpisodeSeenService;
import models.service.EpisodeService;
import models.service.TvShowService;
import models.service.UserService;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.User;

public class EpisodeSeenController extends Controller {

  private final EpisodeSeenService episodeSeenService;
  private final UserService userService;
  private final TvShowService tvShowService;
  private final EpisodeService episodeService;
  private final FormFactory formFactory;

  @Inject
  public EpisodeSeenController(EpisodeSeenService episodeSeenService, UserService userService, TvShowService tvShowService,
                               EpisodeService episodeService, FormFactory formFactory) {
    this.episodeSeenService = episodeSeenService;
    this.userService = userService;
    this.tvShowService = tvShowService;
    this.episodeService = episodeService;
    this.formFactory = formFactory;
  }

  // Devolver votacion segun usuario y tvshow
  @Transactional(readOnly = true)
  @Security.Authenticated(User.class)
  public Result getEpisodeSeen(Integer tvShowId, Integer seasonNumber, Integer episodeNumber) {
    ObjectNode result = Json.newObject();
    EpisodeSeen episodeSeen;

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(request().username());

    if (tvShowId != null && seasonNumber != null && episodeNumber != null && user != null) {
      TvShow tvShow = tvShowService.find(tvShowId);
      if (tvShow != null) {
        Episode episode = episodeService.getEpisodeByNumber(tvShow, seasonNumber, episodeNumber);
        if (episode != null) {
          episodeSeen = episodeSeenService.findByEpisodeIdUserId(episode.id, user.id);
          if (episodeSeen != null) {
            ObjectNode episodeSeenJSON = Json.newObject();
            episodeSeenJSON.put("date", episodeSeen.date.toString());
            result.put("ok", "seen episode");
            result.set("episodeSeen", episodeSeenJSON);
            return ok(result);
          } else {
            // no se ha encontrado
            result.put("not found", "unseen episode");
            return notFound(result);
          }
        } else {
          result.put("error", "season or episode not found");
          return notFound(result);
        }
      } else {
        result.put("error", "tv show not found");
        return notFound(result);
      }
    } else {
      result.put("error", "invalid parameters");
      return badRequest(result);
    }
  }

  // marcar un episodio como visto
  @Transactional
  @Security.Authenticated(User.class)
  public Result setEpisodeSeen(Integer tvShowId, Integer seasonNumber, Integer episodeNumber) {
    ObjectNode result = Json.newObject();

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(request().username());

    if (tvShowId != null && seasonNumber != null && episodeNumber != null && user != null) {
      TvShow tvShow = tvShowService.find(tvShowId);
      if (tvShow != null) {
        EpisodeSeen episodeSeen = episodeSeenService.setEpisodeAsSeen(tvShow, seasonNumber, episodeNumber, user.id);
        if (episodeSeen != null) {
          // devolvemos los datos
          ObjectNode episodeSeenJSON = Json.newObject();
          episodeSeenJSON.put("date", episodeSeen.date.toString());
          result.put("ok", "seen episode");
          result.set("episodeSeen", episodeSeenJSON);
          return ok(result);
        } else {
          // no se ha podido marcar como visto, objetos no encontrados
          result.put("error", "not found");
          return notFound(result);
        }
      } else {
        // serie no encontrada
        result.put("error", "tv show not found");
        return notFound(result);
      }
    } else {
      result.put("error", "invalid parameters");
      return badRequest(result);
    }
  }

  // desmarcar un episodio como visto (no visto)
  @Transactional
  @Security.Authenticated(User.class)
  public Result setEpisodeUnseen(Integer tvShowId, Integer seasonNumber, Integer episodeNumber) {
    ObjectNode result = Json.newObject();

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(request().username());

    if (tvShowId != null && seasonNumber != null && episodeNumber != null && user != null) {
      TvShow tvShow = tvShowService.find(tvShowId);
      if (tvShow != null) {
        if (episodeSeenService.setEpisodeAsUnseen(tvShow, seasonNumber, episodeNumber, user.id)) {
          // devolvemos los datos
          result.put("ok", "unseen episode");
          return ok(result);
        } else {
          // no se ha podido marcar como no visto, objetos no encontrados
          result.put("error", "not found");
          return notFound(result);
        }
      } else {
        // serie no encontrada
        result.put("error", "tv show not found");
        return notFound(result);
      }
    } else {
      result.put("error", "invalid parameters");
      return badRequest(result);
    }
  }

  // marcar una temporada entera como vista (todos sus episodios)
  @Transactional
  @Security.Authenticated(User.class)
  public Result setSeasonSeen(Integer tvShowId, Integer seasonNumber) {
    ObjectNode result = Json.newObject();

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(request().username());

    if (tvShowId != null && seasonNumber != null && user != null) {
      TvShow tvShow = tvShowService.find(tvShowId);
      if (tvShow != null) {
        if (episodeSeenService.setSeasonAsSeen(tvShow, seasonNumber, user.id)) {
          // devolvemos los datos
          result.put("ok", "seen season");
          return ok(result);
        } else {
          // no se ha podido marcar como visto, objetos no encontrados
          result.put("error", "not found");
          return notFound(result);
        }
      } else {
        // serie no encontrada
        result.put("error", "tv show not found");
        return notFound(result);
      }
    } else {
      result.put("error", "invalid parameters");
      return badRequest(result);
    }
  }

  // marcar una temporada entera como NO vista (todos sus episodios)
  @Transactional
  @Security.Authenticated(User.class)
  public Result setSeasonUnseen(Integer tvShowId, Integer seasonNumber) {
    ObjectNode result = Json.newObject();

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(request().username());

    if (tvShowId != null && seasonNumber != null && user != null) {
      TvShow tvShow = tvShowService.find(tvShowId);
      if (tvShow != null) {
        if (episodeSeenService.setSeasonAsUnseen(tvShow, seasonNumber, user.id)) {
          // devolvemos los datos
          result.put("ok", "unseen season");
          return ok(result);
        } else {
          // no se ha podido marcar como visto, objetos no encontrados
          result.put("error", "not found");
          return notFound(result);
        }
      } else {
        // serie no encontrada
        result.put("error", "tv show not found");
        return notFound(result);
      }
    } else {
      result.put("error", "invalid parameters");
      return badRequest(result);
    }
  }

  // marcar una temporada entera como vista (todos sus episodios)
  @Transactional
  @Security.Authenticated(User.class)
  public Result setTvShowSeen(Integer tvShowId) {
    ObjectNode result = Json.newObject();

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(request().username());

    if (tvShowId != null && user != null) {
      TvShow tvShow = tvShowService.find(tvShowId);
      if (tvShow != null) {
        if (episodeSeenService.setTvShowAsSeen(tvShow, user.id)) {
          // devolvemos los datos
          result.put("ok", "seen tv show");
          return ok(result);
        } else {
          // no se ha podido marcar como visto, objetos no encontrados
          result.put("error", "not found");
          return notFound(result);
        }
      } else {
        // serie no encontrada
        result.put("error", "tv show not found");
        return notFound(result);
      }
    } else {
      result.put("error", "invalid parameters");
      return badRequest(result);
    }
  }

  // marcar una temporada entera como vista (todos sus episodios)
  @Transactional
  @Security.Authenticated(User.class)
  public Result setTvShowUnseen(Integer tvShowId) {
    ObjectNode result = Json.newObject();

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(request().username());

    if (tvShowId != null && user != null) {
      TvShow tvShow = tvShowService.find(tvShowId);
      if (tvShow != null) {
        if (episodeSeenService.setTvShowAsUnseen(tvShow, user.id)) {
          // devolvemos los datos
          result.put("ok", "unseen tv show");
          return ok(result);
        } else {
          // no se ha podido marcar como visto, objetos no encontrados
          result.put("error", "not found");
          return notFound(result);
        }
      } else {
        // serie no encontrada
        result.put("error", "tv show not found");
        return notFound(result);
      }
    } else {
      result.put("error", "invalid parameters");
      return badRequest(result);
    }
  }

}
