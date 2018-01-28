package models.dao;

import com.google.inject.Inject;
import models.EpisodeSeen;
import models.TvShowVote;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.validation.ConstraintViolationException;
import java.util.List;

public class EpisodeSeenDAO {

  private static String TABLE = EpisodeSeen.class.getName();
  private final JPAApi jpa;

  @Inject
  public EpisodeSeenDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public EpisodeSeen create(EpisodeSeen episodeSeen) throws ConstraintViolationException {
    jpa.em().persist(episodeSeen);
    jpa.em().flush();
    jpa.em().refresh(episodeSeen);
    Logger.info("Persistencia - EpisodeSeen: user " + episodeSeen.user.email + " ha visto el episodio id: " + episodeSeen.id);
    return episodeSeen;
  }

  // Read de busqueda
  // buscar por id
  public EpisodeSeen find(Integer id) {
    return jpa.em().find(EpisodeSeen.class, id);
  }

  // Read de obtener todas las EpisodesSeen
  public List<EpisodeSeen> all() {
    return jpa.em().createQuery("SELECT v FROM " + TABLE + " v ORDER BY v.id", EpisodeSeen.class).getResultList();
  }

  // Delete
  public void delete(EpisodeSeen EpisodeSeen) {
    jpa.em().remove(EpisodeSeen);
  }
}
