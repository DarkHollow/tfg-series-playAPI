package models.service;

import models.Serie;
import models.dao.SerieDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class SerieService {

  // CRUD

  // Create
  public static Serie create(Serie serie) {
    return SerieDAO.create(serie);
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public static Serie find(Integer id) {
    return SerieDAO.find(id);
  }

  // buscar por idTVDB
  public static Serie findByIdTvdb(Integer idTVDB) {
    return SerieDAO.findByIdTvdb(idTVDB);
  }

  // buscar por campo exacto o LIKE
  public static List<Serie> findBy(String field, String value, Boolean exact) {
    if (exact) {
      return SerieDAO.findByExact(field, value);
    } else {
      return SerieDAO.findByLike(field, value);
    }
  }

  // Read de obtener todas las series
  public static List<Serie> all() {
    return SerieDAO.all();
  }

  // Delete por id
  public static Boolean delete(Integer id) {
    Serie serie = SerieDAO.find(id);
    if (serie != null) {
      SerieDAO.delete(serie);
      return true;
    } else {
      return false;
    }
  }

}
