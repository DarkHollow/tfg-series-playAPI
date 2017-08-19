package models.service;

import com.google.inject.Inject;
import models.TvShow;
import models.TvShowVote;
import models.User;
import models.dao.TvShowVoteDAO;
import play.Logger;

import java.util.List;

public class TvShowVoteService {

  private final TvShowVoteDAO tvShowVoteDAO;
  private final UserService userService;
  private final TvShowService tvShowService;

  @Inject
  public TvShowVoteService(TvShowVoteDAO tvShowVoteDAO, UserService userService, TvShowService tvShowService) {
    this.tvShowVoteDAO = tvShowVoteDAO;
    this.userService = userService;
    this.tvShowService = tvShowService;
  }

  // CRUD

  // Create
  public TvShowVote create(TvShowVote tvShowVote) {
    if (tvShowVote != null && tvShowVote.user != null && tvShowVote.tvShow != null && (tvShowVote.score >= 0.0f && tvShowVote.score <= 10.0f)) {
      // creamos votación
      TvShowVote tvShowVoteCreated = tvShowVoteDAO.create(tvShowVote);
      if (tvShowVoteCreated != null) {
        // si se ha creado, actualizamos votación media y número votos del tv show
        if (updateScore(tvShowVote, tvShowVoteCreated.score, true)) {
          return tvShowVoteCreated;
        } else {
          Logger.error("TvShowVoteService.create - Votación creada pero no actualizada");
          return tvShowVoteCreated;
        }
      } else {
        // votacion no creada
        Logger.error("TvShowVoteService.create - Votación no creada");
        return null;
      }
    } else {
      Logger.error("Al tvShowVote le faltan datos para poder ser creado");
      return null;
    }
  }

  // Read de busqueda
  // buscar por id
  public TvShowVote find(Integer id) {
    return tvShowVoteDAO.find(id);
  }

  // Read de obtener todos
  public List<TvShowVote> all() {
    return tvShowVoteDAO.all();
  }

  // obtener votación segun tv show y usuario
  public TvShowVote findByTvShowIdUserId(Integer tvShowId, Integer userId) {
    TvShowVote result = null;

    // obtenemos el usuario, y de su lista de votaciones filtramos por tvShowId
    User user = userService.find(userId);
    if (user != null) {
      result = user.tvShowVotes.stream().filter(tvShowVote -> tvShowVote.tvShow.id.equals(tvShowId)).findFirst().orElse(null);
    }

    return result;
  }

  // Delete por id
  public Boolean delete(Integer id) {
    TvShowVote tvShowVote = tvShowVoteDAO.find(id);

    if (tvShowVote != null) {
      tvShowVoteDAO.delete(tvShowVote);
      Logger.debug("en teoria existe y borrado...");
      return true;
    } else {
      Logger.debug("No existe?");
      return false;
    }
  }

  public Boolean updateDeletedScore(TvShowVote tvShowVote) {
    TvShow tvShow = tvShowService.find(tvShowVote.tvShow.id);
    if (tvShow != null) {
      Float voteScore = tvShowVote.score;
      Float totalScore = tvShow.score * tvShow.voteCount - voteScore;
      tvShow.voteCount--;
      if (tvShow.voteCount == 0) { // evitar divisón por 0
        tvShow.score = 0f;
      } else {
        tvShow.score = totalScore / tvShow.voteCount;
      }

      tvShow = null;
      return true;
    } else {
      return false;
    }
  }

  public Boolean updateScore(TvShowVote tvShowVote, Float newScore, Boolean newVote) {
    Boolean result = false;

    TvShow tvShow = tvShowService.find(tvShowVote.tvShow.id);
    if (tvShow != null) {

      // obtenemos votación total actual
      Float actualTotalScore = tvShow.score * tvShow.voteCount;

      // si es una nueva votación
      if (newVote) {
        tvShow.voteCount++;
        actualTotalScore += newScore;
      } else {
        // si no es votación nueva, restamos al total y sumamos la nueva
        actualTotalScore = actualTotalScore - tvShowVote.score + newScore;
        // actualizamos score en la propia votación
        tvShowVote.score = newScore;
      }

      tvShow.score = actualTotalScore / tvShow.voteCount; // actualizamos media
      result = true;
    }

    return result;
  }

}
