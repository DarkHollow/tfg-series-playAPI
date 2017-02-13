package models.service;

import com.google.inject.Inject;
import models.Serie;
import models.dao.SerieDAO;

import java.util.List;

public class SerieService {

  private final SerieDAO serieDAO;

  @Inject
  public SerieService(SerieDAO serieDAO) {
    this.serieDAO = serieDAO;
  }

  // CRUD

  // Create
  public Serie create(Serie serie) {
    return serieDAO.create(serie);
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public Serie find(Integer id) {
    return serieDAO.find(id);
  }

  // buscar por idTVDB
  public Serie findByIdTvdb(Integer idTVDB) {
    return serieDAO.findByIdTvdb(idTVDB);
  }

  // buscar por campo exacto o LIKE
  public List<Serie> findBy(String field, String value, Boolean exact) {
    if (exact) {
      return serieDAO.findByExact(field, value);
    } else {
      return serieDAO.findByLike(field, value);
    }
  }

  // Read de obtener todas las series
  public List<Serie> all() {
    return serieDAO.all();
  }

  // Delete por id
  public Boolean delete(Integer id) {
    Serie serie = serieDAO.find(id);
    if (serie != null) {
      serieDAO.delete(serie);
      return true;
    } else {
      return false;
    }
  }

}
