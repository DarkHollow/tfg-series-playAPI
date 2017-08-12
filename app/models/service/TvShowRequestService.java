package models.service;

import com.google.inject.Inject;
import models.TvShow;
import models.TvShowRequest;
import models.User;
import models.dao.TvShowRequestDAO;
import play.Logger;

import javax.validation.ConstraintViolationException;
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
  public TvShowRequest createRequest(TvShowRequest request) {
    return rqDAO.create(request);
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

  // petición TV Show (si existe actualizar, si no existe crear)
  public Boolean requestTvShow(Integer tvdbId, Integer userId) {
    Boolean result = false;

    // comprobamos que no esté en nuetra base de datos ya
    if (tvShowService.findByTvdbId(tvdbId) == null) {
      // encontrar al user
      User user = userService.find(userId);
      if (user != null) {
        // comprobar si existe ya petición para actualizarla
        TvShowRequest actualRequest = findTvShowRequestByTvdbId(tvdbId);
        if (actualRequest != null) {
          // como ya existe, si el estado es rejected o deleted, la actualizamos
          if (actualRequest.status.equals(TvShowRequest.Status.Rejected) || actualRequest.status.equals(TvShowRequest.Status.Deleted)) {
            actualRequest.lastStatus = actualRequest.status;
            actualRequest.status = TvShowRequest.Status.Requested;
            actualRequest.requestCount = actualRequest.requestCount + 1;
            actualRequest.user = user;
            result = true;
          }
        } else {
          // no existe petición de esta serie, crearla
          TvShowRequest request = new TvShowRequest(tvdbId, user);
          request.status = TvShowRequest.Status.Requested;
          request.requestCount = 1;
          try {
            request = createRequest(request);
            if (request != null) {
              result = true;
            }
          } catch (Exception ex) {
            // problema al crear
            Logger.info("TvShowRequestService - requestTvShow() Exception: " + ex.getClass());
          }
        }
      }
    }

    return result;
  }

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
