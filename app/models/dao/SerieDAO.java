package models.dao;

import models.Serie;
import play.db.jpa.*;
import play.Logger;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

public class SerieDAO {

  static String TABLE = Serie.class.getName();

  // CRUD

  // Create
  public static Serie create(Serie serie) {
    Logger.debug("Persistencia - intentando crear serie: " + serie.seriesName);
    JPA.em().persist(serie);
    JPA.em().flush();
    JPA.em().refresh(serie);
    Logger.debug("Persistencia - serie añadida: id " + serie.id + ", nombre " + serie.seriesName);
    return serie;
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public static Serie find(Integer id) {
    return JPA.em().find(Serie.class, id);
  }

  // buscar por campo exacto
  public static List<Serie> findByExact(String field, String value) {
    TypedQuery<Serie> query = JPA.em().createQuery("SELECT s FROM " + TABLE + " s WHERE " + field + " = :value", Serie.class);
    try {
      return query.setParameter("value", value).getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // buscar por campo LIKE
  public static List<Serie> findByLike(String field, String value) {
    TypedQuery<Serie> query = JPA.em().createQuery("SELECT s FROM " + TABLE + " s WHERE " + field + " LIKE :value", Serie.class);
    try {
      return query.setParameter("value", "%" + value + "%").getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // Read de obtener todas las series
  public static List<Serie> all() {
    return JPA.em().createQuery("SELECT s FROM " + TABLE + " s ORDER BY seriesName").getResultList();
  }

  // Delete
  public static void delete(Serie serie) {
    JPA.em().remove(serie);
  }
}
