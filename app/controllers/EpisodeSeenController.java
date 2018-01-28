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

  /*
  // Acción de borrar votación
  @Transactional
  @Security.Authenticated(User.class)
  public Result deleteEpisodeSeen(Integer tvShowId) {
    ObjectNode result = Json.newObject();

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(request().username());

    if (tvShowId != null && user != null) {
      // comprobamos si existe una votación del usuario identificado a esta serie
      EpisodeSeen episodeSeen;
      episodeSeen = episodeSeenService.findByTvShowIdUserId(tvShowId, user.id);
      if (episodeSeen != null) {
        // actualizar score de borrado
        if (episodeSeenService.updateDeletedScore(episodeSeen)) {
          // intentar eliminar votación
          if (deleteVote(episodeSeen.id)) {
            result.put("ok", "vote deleted");
            return ok(result);
          } else {
            Logger.error("EpisodeSeenService.deleteEpisodeSeen - EpisodeSeen no borrado");
            result.put("error", "data updated (warning: vote not deleted");
            return internalServerError(result);
          }
        } else {
          // no se ha podido actualizar datos
          result.put("error", "score cannot be updated (vote not deleted)");
          return ok(result);
        }
      } else {
        // la votación no existe, nada que borrar
        result.put("error", "logged user didn't vote this tv show");
        result.put("mensaje", "No has votado esta serie");
        return notFound(result);
      }
    } else {
      result.put("error", "user not valid or tvShowId null/not number");
      return badRequest(result);
    }
  }

  @Transactional
  private Boolean deleteVote(Integer id) {
    return episodeSeenService.delete(id);
  }
*/
}
