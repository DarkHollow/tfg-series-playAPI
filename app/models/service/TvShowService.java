package models.service;

import com.google.inject.Inject;
import models.TvShow;
import models.dao.TvShowDAO;
import models.service.external.TvdbService;
import org.apache.commons.io.FileUtils;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class TvShowService {

  private final TvShowDAO tvShowDAO;
  private final TvdbService tvdbService;

  @Inject
  public TvShowService(TvShowDAO tvShowDAO, TvdbService tvdbService) {
    this.tvShowDAO = tvShowDAO;
    this.tvdbService = tvdbService;
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

  // Delete por id - TODO acordarse de llamar también a TvShowRequestService.delete !!!
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

}
