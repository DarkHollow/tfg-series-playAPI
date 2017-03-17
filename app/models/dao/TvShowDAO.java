package models.dao;

import com.google.inject.Inject;
import models.TvShow;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class TvShowDAO {

  private static String TABLE = TvShow.class.getName();
  private final JPAApi jpa;

  @Inject
  public TvShowDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public TvShow create(TvShow tvShow) {
    Logger.debug("Persistencia - intentando crear tvShow: " + tvShow.name);
    jpa.em().persist(tvShow);
    jpa.em().flush();
    jpa.em().refresh(tvShow);
    Logger.debug("Persistencia - tvShow añadido: id " + tvShow.id + ", nombre " + tvShow.name);
    return tvShow;
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public TvShow find(Integer id) {
    return jpa.em().find(TvShow.class, id);
  }

  // buscar por tvdbId
  public TvShow findByTvdbId(Integer tvdbId) {
    TypedQuery<TvShow> query = jpa.em().createQuery("SELECT t FROM " + TABLE + " t WHERE t.tvdbId = :value", TvShow.class);
    try {
      return query.setParameter("value", tvdbId).getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  // buscar por campo exacto
  public List<TvShow> findByExact(String field, String value) {
    TypedQuery<TvShow> query = jpa.em().createQuery("SELECT t FROM " + TABLE + " t WHERE " + field + " = :value", TvShow.class);
    try {
      return query.setParameter("value", value).getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // buscar por campo LIKE
  public List<TvShow> findByLike(String field, String value) {
    TypedQuery<TvShow> query = jpa.em().createQuery("SELECT t FROM " + TABLE + " t WHERE " + field + " LIKE :value", TvShow.class);
    try {
      return query.setParameter("value", "%" + value + "%").getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // Read de obtener todos los TV Shows
  public List<TvShow> all() {
    return jpa.em().createQuery("SELECT t FROM " + TABLE + " t ORDER BY t.name", TvShow.class).getResultList();
  }

  // Delete
  public void delete(TvShow tvShow) {
    jpa.em().remove(tvShow);
  }
}
