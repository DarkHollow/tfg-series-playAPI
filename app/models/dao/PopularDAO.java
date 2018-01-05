package models.dao;

import com.google.inject.Inject;
import models.Popular;
import models.TvShow;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class PopularDAO {

  private static String TABLE = Popular.class.getName();
  private final JPAApi jpa;

  @Inject
  public PopularDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public Popular create(Popular popular) {
    Logger.debug("Persistencia - intentando crear popular de: " + popular.tvShow.name);
    jpa.em().persist(popular);
    jpa.em().flush();
    jpa.em().refresh(popular);
    Logger.debug("Persistencia - popular de " + popular.tvShow.name + " añadido: id " + popular.id);
    return popular;
  }

  // find by id
  public Popular find(Integer id) {
    return jpa.em().find(Popular.class, id);
  }

  // get all
  public List<Popular> all() {
    return jpa.em().createQuery("SELECT t FROM " + TABLE + " t", Popular.class).getResultList();
  }

  public void delete(Popular popular) {
    jpa.em().remove(popular);
  }
}
