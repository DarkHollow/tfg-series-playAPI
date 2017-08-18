package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.TvShowVote;
import models.service.TvShowVoteService;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Roles;

public class TvShowVoteController extends Controller {

  private final TvShowVoteService tvShowVoteService;

  @Inject
  public TvShowVoteController(TvShowVoteService tvShowVoteService) {
    this.tvShowVoteService = tvShowVoteService;
  }

  // Devolver votacion segun usuario y tvshow
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result getTvShowVoteByUserTvShow(Integer userId, Integer tvShowId) {
    ObjectNode result = Json.newObject();
    TvShowVote tvShowVote;

    if (userId != null && tvShowId != null) {
      tvShowVote = tvShowVoteService.findByTvShowIdUserId(tvShowId, userId);
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
      result.put("error", "userId null/not number or tvShowId null/not number");
      return badRequest(result);
    }

  }

}
