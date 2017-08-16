package models.dao;

import com.google.inject.Inject;
import models.TvShowVote;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class TvShowVoteDAO {

  private static String TABLE = TvShowVote.class.getName();
  private final JPAApi jpa;

  @Inject
  public TvShowVoteDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public TvShowVote create(TvShowVote tvShowVote) throws ConstraintViolationException {
    Logger.info("Persistencia - intentando crear tvShowVote: " + tvShowVote.user.email + " vota la serie " + tvShowVote.tvShow.name + " con un " + tvShowVote.score.toString());
    jpa.em().persist(tvShowVote);
    jpa.em().flush();
    jpa.em().refresh(tvShowVote);
    Logger.info("Persistencia - tvShowVote añadida: user " + tvShowVote.user.email + " vota la serie " + tvShowVote.tvShow.name + " con un " + tvShowVote.score.toString());
    return tvShowVote;
  }

  // Read de busqueda
  // buscar por id
  public TvShowVote find(Integer id) {
    return jpa.em().find(TvShowVote.class, id);
  }

  // Read de obtener todas las tvShowVotes
  public List<TvShowVote> all() {
    return jpa.em().createQuery("SELECT v FROM " + TABLE + " v ORDER BY v.id", TvShowVote.class).getResultList();
  }

  // Delete
  public void delete(TvShowVote tvShowVote) {
    jpa.em().remove(tvShowVote);
  }
}
