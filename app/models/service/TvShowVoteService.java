package models.service;

import com.google.inject.Inject;
import models.TvShowVote;
import models.User;
import models.dao.TvShowVoteDAO;
import play.Logger;

import java.util.List;

public class TvShowVoteService {

  private final TvShowVoteDAO tvShowVoteDAO;
  private final UserService userService;

  @Inject
  public TvShowVoteService(TvShowVoteDAO tvShowVoteDAO, UserService userService) {
    this.tvShowVoteDAO = tvShowVoteDAO;
    this.userService = userService;
  }

  // CRUD

  // Create
  public TvShowVote create(TvShowVote tvShowVote) {
    if (tvShowVote != null && tvShowVote.user != null && tvShowVote.tvShow != null && (tvShowVote.score >= 0.0f && tvShowVote.score <= 10.0f)) {
      return tvShowVoteDAO.create(tvShowVote);
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
      return true;
    } else {
      return false;
    }
  }

}
