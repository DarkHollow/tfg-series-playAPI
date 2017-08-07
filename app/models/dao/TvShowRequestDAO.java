package models.dao;

import com.google.inject.Inject;
import models.TvShowRequest;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.persistence.NoResultException;
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

  // buscar por id de TVDB
  public TvShowRequest findTvShowRequetByTvdbId(Integer tvdbId) {
    try {
      return jpa.em().createQuery("SELECT r FROM " + TABLE + " r WHERE r.tvdbId = " + tvdbId, TvShowRequest.class).getSingleResult();
    } catch (NoResultException ex) {
      // no existe request con esta tvdb id
      return null;
    }
  }

  // Read de obtener todas las requests
  public List<TvShowRequest> all() {
    return jpa.em().createQuery("SELECT r FROM " + TABLE + " r ORDER BY r.id", TvShowRequest.class).getResultList();
  }

  // Read de obtener todas las requests solicitadas
  public List<TvShowRequest> getRequested() {
    return jpa.em().createQuery("SELECT r FROM " + TABLE + " r WHERE r.status = 'Requested' ORDER BY r.requestDate", TvShowRequest.class).getResultList();
  }

  // Read de obtener todas las requests que se están procesando
  public List<TvShowRequest> getProcessing() {
    return jpa.em().createQuery("SELECT r FROM " + TABLE + " r WHERE r.status = 'Processing' ORDER BY r.requestDate", TvShowRequest.class).getResultList();
  }

  // Read de obtener todas las requests persistidas
  public List<TvShowRequest> getPersisted() {
    return jpa.em().createQuery("SELECT r FROM " + TABLE + " r WHERE r.status = 'Persisted' ORDER BY r.requestDate", TvShowRequest.class).getResultList();
  }

  // Read de obtener todas las requests rechazadas
  public List<TvShowRequest> getRejected() {
    return jpa.em().createQuery("SELECT r FROM " + TABLE + " r WHERE r.status = 'Rejected' ORDER BY r.requestDate", TvShowRequest.class).getResultList();
  }

  // Delete
  public void delete(TvShowRequest request) {
    jpa.em().remove(request);
  }
}
