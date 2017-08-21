package models.service;

import com.google.inject.Inject;
import models.TvShowRequest;
import models.User;
import models.dao.TvShowRequestDAO;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

public class TvShowRequestService {

  private final TvShowService tvShowService;
  private final UserService userService;
  private final TvShowRequestDAO rqDAO;

  @Inject
  public TvShowRequestService(TvShowService tvShowService, UserService userService, TvShowRequestDAO rqDAO) {
    this.tvShowService = tvShowService;
    this.userService = userService;
    this.rqDAO = rqDAO;
  }

  // create
  public TvShowRequest create(TvShowRequest request) {
    // comprobamos que la serie no exista ya
    if (tvShowService.findByTvdbId(request.tvdbId) == null) {
      // comprobar si existe ya petición para actualizarla
      try {
        if (reRequest(request)) {
          return request;
        } else {
          // crear
          request.status = TvShowRequest.Status.Requested;
          request.requestCount = 1;
          return rqDAO.create(request);
        }
      } catch(Exception ex) {
        Logger.error("TvShowRequest Service - create: " + ex.getMessage());
        return null;
      }
    } else {
      Logger.error("TvShowRequest Service - create: la serie ya existe");
      return null;
    }
  }

  // obtener peticion por id
  public TvShowRequest findById(Integer id) { return rqDAO.find(id); }

  // obtener todas las peticiones
  public List<TvShowRequest> all() { return rqDAO.all(); }

  // obtener peticiones de tipo Requested
  public List<TvShowRequest> getRequested() {
    return rqDAO.getRequested();
  }

  // obtener peticiones de tipo Processing
  public List<TvShowRequest> getProcessing() {
    return rqDAO.getProcessing();
  }

  // obtener peticiones pendientes: Requested + Processing
  public List<TvShowRequest> getPending() {
    List<TvShowRequest> requests = new ArrayList<>();
    requests.addAll(getRequested());
    requests.addAll(getProcessing());
    return requests;
  }

  // obtener peticiones de tipo Persisted
  public List<TvShowRequest> getPersisted() {
    return rqDAO.getPersisted();
  }

  // obtener peticiones de tipo Rejected
  public List<TvShowRequest> getRejected() {
    return rqDAO.getRejected();
  }

  // obtener peticiones de tipo Deleted
  public List<TvShowRequest> getDeleted() { return rqDAO.getDeleted(); }

  // buscar peticiones por id de TVDB
  public TvShowRequest findTvShowRequestByTvdbId(Integer tvdbId) { return rqDAO.findTvShowRequetByTvdbId(tvdbId); }

  // rechazar peticion
  public Boolean reject(Integer id) {
    TvShowRequest tvShowRequest = rqDAO.find(id);
    if (tvShowRequest != null) {
      tvShowRequest.lastStatus = tvShowRequest.status;
      tvShowRequest.status = TvShowRequest.Status.Rejected;
      return true;
    } else {
      return false;
    }
  }

  // delete por id
  public Boolean delete(Integer id) {
    TvShowRequest tvShowRequest = rqDAO.find(id);
    if (tvShowRequest != null) {
      rqDAO.delete(tvShowRequest);
      return true;
    } else {
      return false;
    }
  }

  // re request - volver a pedir una serie que ya tiene request (rejected, deleted)
  private Boolean reRequest(TvShowRequest request) throws Exception {
    Boolean result = false;

    TvShowRequest actualRequest = findTvShowRequestByTvdbId(request.tvdbId);
    if (actualRequest != null) {
      // como ya existe, si el estado es rejected o deleted, la actualizamos
      if (actualRequest.status.equals(TvShowRequest.Status.Rejected) || actualRequest.status.equals(TvShowRequest.Status.Deleted)) {
        actualRequest.lastStatus = actualRequest.status;
        actualRequest.status = TvShowRequest.Status.Requested;
        actualRequest.requestCount = actualRequest.requestCount + 1;
        actualRequest.user = request.user;
        result = true;
      } else {
        throw new Exception("reRequest threw an exception - request status: " + actualRequest.status.toString());
      }
    }
    return result;
  }

  // delete TV Show, necesario para cuando se borra un TV Show, cambia el estado de la petición a deleted
  // no borra la petición
  public Boolean deleteTvShow(Integer tvdbId) {
    TvShowRequest request = findTvShowRequestByTvdbId(tvdbId);
    if (request != null) {
      request.lastStatus = request.status;
      request.status = TvShowRequest.Status.Deleted;
      return true;
    } else {
      // no existe la peticion ?
      Logger.error("TvShowRequestService - deleteTvShow: no se ha podido cambiar el estado de la petición a Deleted");
      return false;
    }
  }

}
