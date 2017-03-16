package models.service;

import com.google.inject.Inject;
import models.TvShow;
import models.dao.TvShowDAO;

import java.util.List;

public class TvShowService {

  private final TvShowDAO tvShowDAO;

  @Inject
  public TvShowService(TvShowDAO tvShowDAO) {
    this.tvShowDAO = tvShowDAO;
  }

  // CRUD

  // Create
  public TvShow create(TvShow tvShow) {
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

  // Delete por id
  public Boolean delete(Integer id) {
    TvShow tvShow = tvShowDAO.find(id);
    if (tvShow != null) {
      tvShowDAO.delete(tvShow);
      return true;
    } else {
      return false;
    }
  }

}
