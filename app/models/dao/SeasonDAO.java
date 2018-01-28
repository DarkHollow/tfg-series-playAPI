package models.dao;

import com.google.inject.Inject;
import models.Season;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.validation.ConstraintViolationException;

public class SeasonDAO {

  private static String TABLE = Season.class.getName();
  private final JPAApi jpa;

  @Inject
  public SeasonDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public Season create(Season season) throws ConstraintViolationException {
    jpa.em().persist(season);
    jpa.em().flush();
    jpa.em().refresh(season);
    Logger.info("Persistencia - Temporada creada: " + season.tvShow.name + " - Temporada " + season.seasonNumber);
    return season;
  }

  // Read de busqueda
  // buscar por id
  public Season find(Integer id) {
    return jpa.em().find(Season.class, id);
  }


  // Delete
  public void delete(Season season) {
    jpa.em().remove(season);
  }
}
