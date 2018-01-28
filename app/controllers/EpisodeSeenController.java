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

}
