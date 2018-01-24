package models.service;

import com.google.inject.Inject;
import models.TvShow;
import models.User;
import models.dao.TvShowDAO;
import models.service.external.TmdbService;
import models.service.external.TvdbService;
import org.apache.commons.io.FileUtils;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class TvShowService {

  private final TvShowDAO tvShowDAO;
  private final UserService userService;
  private final TvdbService tvdbService;
  private final TmdbService tmdbService;

  @Inject
  public TvShowService(TvShowDAO tvShowDAO, UserService userService, TvdbService tvdbService, TmdbService tmdbService) {
    this.tvShowDAO = tvShowDAO;
    this.userService = userService;
    this.tvdbService = tvdbService;
    this.tmdbService = tmdbService;
  }

  // CRUD

  // Create
  public TvShow create(TvShow tvShow) {
    if (tvShow.score == null || tvShow.voteCount == null) {
      tvShow.score = 0.0f;
      tvShow.voteCount = 0;
    }
    return tvShowDAO.create(tvShow);
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public TvShow find(Integer id) {
    return tvShowDAO.find(id);
  }

  // buscar por tvdbId
  public TvShow findByTvdbId(Integer tvdbId) {
    return tvShowDAO.findByTvdbId(tvdbId);
  }

  // buscar por campo exacto o LIKE
  public List<TvShow> findBy(String field, String value, Boolean exact) {
    if (exact) {
      return tvShowDAO.findByExact(field, value);
    } else {
      return tvShowDAO.findByLike(field, value);
    }
  }

  // Read de obtener todos los TV Shows
  public List<TvShow> all() {
    return tvShowDAO.all();
  }

  // Delete por id - TODO pensar en si llamar también a TvShowRequestService.delete !!!
  public Boolean delete(Integer id) {
    TvShow tvShow = tvShowDAO.find(id);
    if (tvShow != null) {
      tvShowDAO.delete(tvShow);

      // borrar imagenes !
      File folder = new File("./public/images/series/" + tvShow.id + "/");
      try {
        FileUtils.deleteDirectory(folder);
      } catch (IOException e) {
        Logger.error("Delete serie - no se ha podido eliminar la carpeta de imagenes de la serie");
      }
      return true;
    } else {
      return false;
    }
  }

  // Update mediante servicios externos
  public TvShow updateData(TvShow tvShow) throws InterruptedException, ExecutionException, TimeoutException {
    if (tvShow != null) {
      // TVDB: pedimos los datos provenientes de este servicio
      TvShow tvdbShow = tvdbService.getTvShowTVDB(tvShow.tvdbId);
      if (tvdbShow != null) {
        // actualizamos datos
        tvShow.imdbId = tvdbShow.imdbId;
        tvShow.name = tvdbShow.name;
        tvShow.network = tvdbShow.network;
        tvShow.overview = tvdbShow.overview;
        tvShow.rating = tvdbShow.rating;
        tvShow.runtime = tvdbShow.runtime;
        tvShow.firstAired = tvdbShow.firstAired;
        tvShow.genre = tvdbShow.genre;
        tvShow.status = tvdbShow.status;
      } else {
        // no se ha encontrado en TVDB o error en la solicitud
        tvShow = null;
      }
    }
    return tvShow;
  }

  public Boolean getAndSetImage(TvShow tvShow, String type) {
    String path = null;

    switch (type) {
      case "banner":
        path = tvdbService.getBanner(tvShow);
        break;
      case "poster":
      case "fanart":
        path = tvdbService.getImage(tvShow, type);
        break;
      default:
    }

    if (path != null && !path.equals("")) {
      path = path.replace("public", "assets");
      switch (type) {
        case "banner":
          tvShow.banner = path;
          break;
        case "poster":
          tvShow.poster = path;
          break;
        case "fanart":
          tvShow.fanart = path;
          break;
        default:
      }
      return true;
    } else {
      // no se ha podido obtener la imagen
      Logger.info(tvShow.name + " - no se ha podido obtener la imagen " + type);
      return false;
    }
  }

  Integer getObtainTmdbId(TvShow tvShow) {
    if (tvShow != null) {
      try {
        if (tvShow.tmdbId == null) {
          TvShow tmdbShow = tmdbService.findByTvdbId(tvShow.tvdbId);
          if (tmdbShow != null) {
            tvShow.tmdbId = tmdbShow.tmdbId;
          }
        }
      } catch (Exception ex) {
        Logger.info("No se ha podido obtener el tmdbId para la serie " + tvShow.name);
      }
    } else {
      return null;
    }
    return tvShow.tmdbId;
  }

  public List<TvShow> getTopRatedTvShows(Integer size) {
    List<TvShow> topRatedTvShows = tvShowDAO.all().stream().sorted(Comparator.comparing((TvShow tvShow) -> tvShow.score)
            .thenComparing(tvShow -> tvShow.voteCount).reversed()).collect(Collectors.toList());
    topRatedTvShows.removeIf(tvShow -> tvShow.score == 0);
    if (topRatedTvShows.isEmpty()) {
      return topRatedTvShows;
    } else if (topRatedTvShows.size() < size) {
      return topRatedTvShows.subList(0, topRatedTvShows.size());
    } else {
      return topRatedTvShows.subList(0, size);
    }
  }

  public Boolean followTvShow(Integer tvShowId, Integer userId) {
    TvShow tvShow = find(tvShowId);
    User user = userService.find(userId);
    if (tvShow != null && user != null) {
      if (!user.followedTvShows.contains(tvShow)) {
        user.followedTvShows.add(tvShow);
      }
      if (!tvShow.followingUsers.contains(user)) {
        tvShow.followingUsers.add(user);
      }
      return true;
    } else {
      return false;
    }
  }

  public Boolean unfollowTvShow(Integer tvShowId, Integer userId) {
    TvShow tvShow = find(tvShowId);
    User user = userService.find(userId);
    if (tvShow != null && user != null) {
      user.followedTvShows.remove(tvShow);
      tvShow.followingUsers.remove(user);
      return true;
    } else {
      return false;
    }
  }

  public Boolean checkFollowTvShow(Integer tvShowId, Integer userId) {
    TvShow tvShow = find(tvShowId);
    User user = userService.find(userId);
    return (tvShow != null && user != null && tvShow.followingUsers.contains(user) && user.followedTvShows.contains(tvShow));
  }

}
