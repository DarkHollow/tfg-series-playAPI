package models.dao;

import com.google.inject.Inject;
import models.TvShowRequest;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class TvShowRequestDAO {

  private static String TABLE = TvShowRequest.class.getName();
  private final JPAApi jpa;

  @Inject
  public TvShowRequestDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public TvShowRequest create(TvShowRequest request) throws ConstraintViolationException {
    Logger.debug("Persistencia - intentando crear request: " + request.tvdbId);
    request.requestDate = Date.from(Instant.now());
    jpa.em().persist(request);
    jpa.em().flush();
    jpa.em().refresh(request);
    Logger.debug("Persistencia - request añadida: user " + request.user.id + ", tvdbId " + request.tvdbId);
    return request;
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public TvShowRequest find(Integer id) {
    return jpa.em().find(TvShowRequest.class, id);
  }

  // Read de obtener todas las requests
  public List<TvShowRequest> all() {
    return jpa.em().createQuery("SELECT r FROM " + TABLE + " r ORDER BY r.id", TvShowRequest.class).getResultList();
  }

  // Delete
  public void delete(TvShowRequest request) {
    jpa.em().remove(request);
  }
}
