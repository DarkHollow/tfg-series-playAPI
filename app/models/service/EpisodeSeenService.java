package models.service;

import com.google.inject.Inject;
import models.*;
import models.dao.EpisodeSeenDAO;
import play.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

  public Boolean setEpisodeAsUnseen(TvShow tvShow, Integer seasonNumber, Integer episodeNumber, Integer userId) {
    Episode episode = episodeService.getEpisodeByNumber(tvShow, seasonNumber, episodeNumber);
    User user = userService.find(userId);

    if (tvShow != null && user != null && episode != null) {
      EpisodeSeen episodeSeen = findByEpisodeIdUserId(episode.id, userId);
      if (episodeSeen == null) {
        return true;
      } else {
        return (delete(episodeSeen.id));
      }
    } else {
      return false;
    }
  }

  public Boolean setSeasonAsSeen(TvShow tvShow, Integer seasonNumber, Integer userId) {
    if (tvShow != null) {
      Season foundSeason = tvShow.seasons.stream().filter(season -> season.seasonNumber.equals(seasonNumber)).findFirst().orElse(null);
      if (foundSeason != null) {
        AtomicReference<Boolean> result = new AtomicReference<>(true);
        foundSeason.episodes.forEach(episode -> {
          if (setEpisodeAsSeen(tvShow, seasonNumber, episode.episodeNumber, userId) == null) {
            result.set(false);
          }
        });
        return result.get();
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public Boolean setSeasonAsUnseen(TvShow tvShow, Integer seasonNumber, Integer userId) {
    if (tvShow != null) {
      Season foundSeason = tvShow.seasons.stream().filter(season -> season.seasonNumber.equals(seasonNumber)).findFirst().orElse(null);
      if (foundSeason != null) {
        AtomicReference<Boolean> result = new AtomicReference<>(true);
        foundSeason.episodes.forEach(episode -> {
          if (!setEpisodeAsUnseen(tvShow, seasonNumber, episode.episodeNumber, userId)) {
            result.set(false);
          }
        });
        return result.get();
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public Boolean setTvShowAsSeen(TvShow tvShow, Integer userId) {
    Boolean result = true;

    if (tvShow != null) {
      for (Season season : tvShow.seasons) {
        Boolean resultSeason = setSeasonAsSeen(tvShow, season.seasonNumber, userId);
        if (!resultSeason) {
          return false;
        }
      }
    } else {
      result = false;
    }
    return result;
  }

  public Boolean setTvShowAsUnseen(TvShow tvShow, Integer userId) {
    Boolean result = true;

    if (tvShow != null) {
      for (Season season : tvShow.seasons) {
        Boolean resultSeason = setSeasonAsUnseen(tvShow, season.seasonNumber, userId);
        if (!resultSeason) {
          return false;
        }
      }
    } else {
      result = false;
    }
    return result;
  }

  public List<EpisodeSeen> getSeasonEpisodesSeen(Season season, Integer userId) {
    List<EpisodeSeen> episodesSeen = new ArrayList<>();
    Integer result = 0;
    if (season != null) {
      for (Episode episode : season.episodes) {
        EpisodeSeen episodeSeen = findByEpisodeIdUserId(episode.id, userId);
        if (episodeSeen != null) {
          episodesSeen.add(episodeSeen);
        }
      }
    }
    return episodesSeen;
  }

}
