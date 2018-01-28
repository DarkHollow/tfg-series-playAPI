package models.service;

import com.google.inject.Inject;
import models.Episode;
import models.EpisodeSeen;
import models.TvShow;
import models.User;
import models.dao.EpisodeSeenDAO;
import play.Logger;

import java.util.Date;
import java.util.List;

public class EpisodeSeenService {

  private final EpisodeSeenDAO episodeSeenDAO;
  private final UserService userService;
  private final EpisodeService episodeService;

  @Inject
  public EpisodeSeenService(EpisodeSeenDAO episodeSeenDAO, UserService userService, EpisodeService episodeService) {
    this.episodeSeenDAO = episodeSeenDAO;
    this.userService = userService;
    this.episodeService = episodeService;
  }

  // CRUD

  // Create
  public EpisodeSeen create(EpisodeSeen episodeSeen) {
    if (episodeSeen != null && episodeSeen.user != null && episodeSeen.episode != null) {
      // creamos episode seen
      episodeSeen.date = new Date();
      EpisodeSeen episodeSeenCreated = episodeSeenDAO.create(episodeSeen);
      if (episodeSeenCreated != null) {
        return episodeSeenCreated;
      } else {
        // episode seen no creado
        Logger.error("EpisodeSeenService.create - no creado");
        return null;
      }
    } else {
      Logger.error("Al episodeSeen le falta datos para poder ser creado");
      return null;
    }
  }

  // Read de busqueda
  // buscar por id
  public EpisodeSeen find(Integer id) {
    return episodeSeenDAO.find(id);
  }

  // Read de obtener todos
  public List<EpisodeSeen> all() {
    return episodeSeenDAO.all();
  }

  // obtener si un usuario ha visto un episodio en concreto
  public EpisodeSeen findByEpisodeIdUserId(Integer episodeId, Integer userId) {
    EpisodeSeen result = null;

    // obtenemos el usuario, y de su lista de votaciones filtramos por episodeSeenId
    User user = userService.find(userId);
    if (user != null) {
      result = user.episodesSeen.stream().filter(episodeSeen -> episodeSeen.episode.id.equals(episodeId)).findFirst().orElse(null);
    }

    return result;
  }

  // Delete por id
  public Boolean delete(Integer id) {
    EpisodeSeen episodeSeen = episodeSeenDAO.find(id);

    if (episodeSeen != null) {
      // me elimino de mis padres
      Integer userId = episodeSeen.user.id;
      Integer episodeId = episodeSeen.episode.id;
      userService.find(userId).episodesSeen.remove(episodeSeen);
      episodeService.find(episodeId).episodesSeen.remove(episodeSeen);

      // finalmente, me elimino yo
      episodeSeenDAO.delete(episodeSeen);
      Logger.debug("en teoria existe y borrado...");
      return true;
    } else {
      Logger.debug("No existe?");
      return false;
    }
  }

  public EpisodeSeen setEpisodeAsSeen(TvShow tvShow, Integer seasonNumber, Integer episodeNumber, Integer userId) {
    Episode episode = episodeService.getEpisodeByNumber(tvShow, seasonNumber, episodeNumber);
    User user = userService.find(userId);

    if (tvShow != null && user != null && episode != null) {
      EpisodeSeen oldEpisodeSeen = findByEpisodeIdUserId(episode.id, userId);

      if (oldEpisodeSeen == null) {
        EpisodeSeen newEpisodeSeen = new EpisodeSeen(user, episode, new Date());
        newEpisodeSeen = create(newEpisodeSeen);
        return newEpisodeSeen;
      } else {
        return oldEpisodeSeen;
      }
    } else {
      return null;
    }
  }

}
