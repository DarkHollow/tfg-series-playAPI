package models.dao;

import com.google.inject.Inject;
import models.RequestedSeries;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class RequestedSeriesDAO {

  private static String TABLE = RequestedSeries.class.getName();
  private final JPAApi jpa;

  @Inject
  public RequestedSeriesDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public RequestedSeries create(RequestedSeries request) throws ConstraintViolationException {
    Logger.debug("Persistencia - intentando crear request: " + request.idTVDB);
    request.requestDate = Date.from(Instant.now());
    jpa.em().persist(request);
    jpa.em().flush();
    jpa.em().refresh(request);
    Logger.debug("Persistencia - request añadida: usuario " + request.usuario.id + ", idTVDB " + request.idTVDB);
    return request;
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public RequestedSeries find(Integer id) {
    return jpa.em().find(RequestedSeries.class, id);
  }

  // buscar por campo exacto
  public List<RequestedSeries> findByExact(String field, String value) {
    TypedQuery<RequestedSeries> query = jpa.em().createQuery("SELECT r FROM " + TABLE + " r WHERE " + field + " = :value", RequestedSeries.class);
    try {
      return query.setParameter("value", value).getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // Read de obtener todas las requests
  public List<RequestedSeries> all() {
    return jpa.em().createQuery("SELECT r FROM " + TABLE + " r ORDER BY r.id", RequestedSeries.class).getResultList();
  }

  // Delete
  public void delete(RequestedSeries request) {
    jpa.em().remove(request);
  }
}
