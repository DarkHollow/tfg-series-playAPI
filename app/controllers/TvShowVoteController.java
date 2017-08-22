package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.TvShow;
import models.TvShowVote;
import models.service.TvShowService;
import models.service.TvShowVoteService;
import models.service.UserService;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Roles;
import utils.Security.User;

public class TvShowVoteController extends Controller {

  private final TvShowVoteService tvShowVoteService;
  private final UserService userService;
  private final TvShowService tvShowService;
  private final FormFactory formFactory;
  private final utils.Security.User userAuth;

  @Inject
  public TvShowVoteController(TvShowVoteService tvShowVoteService, UserService userService, TvShowService tvShowService,
                              FormFactory formFactory, utils.Security.User userAuth) {
    this.tvShowVoteService = tvShowVoteService;
    this.userService = userService;
    this.tvShowService = tvShowService;
    this.formFactory = formFactory;
    this.userAuth = userAuth;
  }

  // Acción de votar (crear votación/modificar votación)
  @Transactional
  @Security.Authenticated(User.class)
  public Result voteTvShow(Integer tvShowId) {
    ObjectNode result = Json.newObject();

    // obtenemos la nota de votación del cuerpo de la petición
    Float score;
    DynamicForm requestForm = formFactory.form().bindFromRequest();

    try {
      score = Float.valueOf(requestForm.get("score"));
    } catch (Exception ex) {
      result.put("error", "score null or not float");
      return badRequest(result);
    }

    // comprobaciones del valor
    if (!(score >= 0 && score <= 10)) {
      result.put("error", "score out of range");
      return badRequest(result);
    } else {
      if (!((int) (double) score == score)) {
        result.put("error", "score must be integer unit float");
        return badRequest(result);
      }
    }

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(userAuth.getUsername(Http.Context.current()));

    if (tvShowId != null && user != null) {
      // comprobamos si ya había votado este usuario a esta serie
      TvShowVote tvShowVote;
      tvShowVote = tvShowVoteService.findByTvShowIdUserId(tvShowId, user.id);
      if (tvShowVote != null) {
        // ya había votado esta serie, modificamos votación media y contador
        if (!tvShowVoteService.updateScore(tvShowVote, score, false)) {
          Logger.error("TvShowVoteService.updateScore - TvShowVote actualizada, votación media y votos no");
        }
        // devolvemos los datos
        ObjectNode tvShowVoteJSON = Json.newObject();
        tvShowVoteJSON.put("id", tvShowVote.id);
        tvShowVoteJSON.put("tvShowId", tvShowVote.tvShow.id);
        tvShowVoteJSON.put("userId", tvShowVote.user.id);
        tvShowVoteJSON.put("score", tvShowVote.score);

        result.put("ok", "vote updated");
        result.set("tvShowVote", tvShowVoteJSON);
        return ok(result);
      } else {
        // no había votado antes a esta serie, creamos votación
        // obtenemos la serie
        TvShow tvShow = tvShowService.find(tvShowId);
        if (tvShow != null) {
          // creamos votación
          TvShowVote tvShowVoteNew = new TvShowVote(user, tvShow, score);
          tvShowVoteNew = tvShowVoteService.create(tvShowVoteNew);
          if (tvShowVoteNew != null) {
            // votación creada, devolvemos los datos
            ObjectNode tvShowVoteJSON = Json.newObject();
            tvShowVoteJSON.put("id", tvShowVoteNew.id);
            tvShowVoteJSON.put("tvShowId", tvShowVoteNew.tvShow.id);
            tvShowVoteJSON.put("userId", tvShowVoteNew.user.id);
            tvShowVoteJSON.put("score", tvShowVoteNew.score);

            result.put("ok", "vote created");
            result.set("tvShowVote", tvShowVoteJSON);
            return created(result);
          } else {
            // no se ha podido crear la votación
            result.put("error", "uncreated vote");
            return internalServerError(result);
          }
        } else {
          // no existe ?
          result.put("error", "tv show doesn't exist");
          return notFound(result);
        }
      }
    } else {
      result.put("error", "user not valid or tvShowId null/not number");
      return badRequest(result);
    }
  }

  // Devolver votacion segun usuario y tvshow
  @Transactional(readOnly = true)
  @Security.Authenticated(User.class)
  public Result getByTvShowUser(Integer tvShowId) {
    ObjectNode result = Json.newObject();
    TvShowVote tvShowVote;

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(request().username());

    if (user != null && tvShowId != null) {
      tvShowVote = tvShowVoteService.findByTvShowIdUserId(tvShowId, user.id);
      if (tvShowVote != null) {
        ObjectNode tvShowVoteJSON = Json.newObject();
        tvShowVoteJSON.put("id", tvShowVote.id);
        tvShowVoteJSON.put("tvShowId", tvShowVote.tvShow.id);
        tvShowVoteJSON.put("userId", tvShowVote.user.id);
        tvShowVoteJSON.put("score", tvShowVote.score);

        result.put("ok", "vote found");
        result.set("tvShowVote", tvShowVoteJSON);
        return ok(result);
      } else {
        // no se ha encontrado
        result.put("error", "vote doesn't exist");
        return notFound(result);
      }
    } else {
      result.put("error", "tvShowId null/not number");
      return badRequest(result);
    }

  }

  // Acción de borrar votación
  @Transactional
  @Security.Authenticated(User.class)
  public Result deleteTvShowVote(Integer tvShowId) {
    ObjectNode result = Json.newObject();

    // obtenemos el usuario identificado
    models.User user = userService.findByEmail(userAuth.getUsername(Http.Context.current()));

    if (tvShowId != null && user != null) {
      // comprobamos si existe una votación del usuario identificado a esta serie
      TvShowVote tvShowVote;
      tvShowVote = tvShowVoteService.findByTvShowIdUserId(tvShowId, user.id);
      if (tvShowVote != null) {
        // actualizar score de borrado
        if (tvShowVoteService.updateDeletedScore(tvShowVote)) {
          // intentar eliminar votación
          if (deleteVote(tvShowVote.id)) {
            result.put("ok", "vote deleted");
            return ok(result);
          } else {
            Logger.error("TvShowVoteService.deleteTvShowVote - TvShowVote no borrado");
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
    return tvShowVoteService.delete(id);
  }

}
