package models.dao;

import com.google.inject.Inject;
import models.Evolution;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.persistence.Query;
import java.util.List;

public class EvolutionDAO {

  private static String TABLE = Evolution.class.getName();
  private final JPAApi jpa;

  @Inject
  public EvolutionDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public Evolution create(Evolution evolution) {
    Logger.debug("Persistencia - intentando crear evolution: " + evolution.version);
    jpa.em().persist(evolution);
    jpa.em().flush();
    jpa.em().refresh(evolution);
    Logger.debug("Persistencia - evolution añadida: id " + evolution.id);
    return evolution;
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public Evolution find(Integer id) {
    return jpa.em().find(Evolution.class, id);
  }

  // Read de obtener todos los TV Shows
  public List<Evolution> all() {
    return jpa.em().createQuery("SELECT e FROM " + TABLE + " e ORDER BY e.id", Evolution.class).getResultList();
  }

  // Delete
  public void delete(Evolution evolution) {
    jpa.em().remove(evolution);
  }

  // obtener evolutions de play definidas en conf/evolutions/default/
  public List<Object[]> getPlayEvolutions() {
    Query query = jpa.em().createNativeQuery("SELECT id, state FROM play_evolutions ORDER BY play_evolutions.id");
    return query.getResultList();
  }

  // obtener evolutions de play definidas en conf/evolutions/default/ que estén 'applied' !
  public List<Object[]> getAppliedPlayEvolutions() {
    Query query = jpa.em().createNativeQuery("SELECT id, state FROM play_evolutions WHERE state = 'applied' ORDER BY play_evolutions.id");
    return query.getResultList();
  }

}
