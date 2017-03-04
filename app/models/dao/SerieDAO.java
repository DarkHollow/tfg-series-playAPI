package models.dao;

import com.google.inject.Inject;
import models.Serie;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class SerieDAO {

  private static String TABLE = Serie.class.getName();
  private final JPAApi jpa;

  @Inject
  public SerieDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public Serie create(Serie serie) {
    Logger.debug("Persistencia - intentando crear serie: " + serie.seriesName);
    jpa.em().persist(serie);
    jpa.em().flush();
    jpa.em().refresh(serie);
    Logger.debug("Persistencia - serie añadida: id " + serie.id + ", nombre " + serie.seriesName);
    return serie;
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public Serie find(Integer id) {
    return jpa.em().find(Serie.class, id);
  }

  // buscar por tvdbId
  public Serie findByTvdbId(Integer tvdbId) {
    TypedQuery<Serie> query = jpa.em().createQuery("SELECT s FROM " + TABLE + " s WHERE s.tvdbId = :value", Serie.class);
    try {
      return query.setParameter("value", tvdbId).getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  // buscar por campo exacto
  public List<Serie> findByExact(String field, String value) {
    TypedQuery<Serie> query = jpa.em().createQuery("SELECT s FROM " + TABLE + " s WHERE " + field + " = :value", Serie.class);
    try {
      return query.setParameter("value", value).getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // buscar por campo LIKE
  public List<Serie> findByLike(String field, String value) {
    TypedQuery<Serie> query = jpa.em().createQuery("SELECT s FROM " + TABLE + " s WHERE " + field + " LIKE :value", Serie.class);
    try {
      return query.setParameter("value", "%" + value + "%").getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // Read de obtener todas las series
  public List<Serie> all() {
    return jpa.em().createQuery("SELECT s FROM " + TABLE + " s ORDER BY s.seriesName", Serie.class).getResultList();
  }

  // Delete
  public void delete(Serie serie) {
    jpa.em().remove(serie);
  }
}
