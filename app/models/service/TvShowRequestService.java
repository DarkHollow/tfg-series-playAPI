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


  // buscar peticiones por id de TVDB
  public List<TvShowRequest> findTvShowRequests(Integer tvdbId) { return rqDAO.findTvShowRequestsByTvdbId(tvdbId); }

  // POST petición TV Show
  public Boolean requestTvShow(Integer tvdbId, Integer userId) {
    Boolean result = false;

    // comprobamos que no esté en nuetra base de datos ya
    if (tvShowService.findByTvdbId(tvdbId) == null) {
      // encontrar al user
      User user = userService.find(userId);
      if (user != null) {
        // hacemos la peticion
        TvShowRequest request = new TvShowRequest(tvdbId, user);
        request.status = TvShowRequest.Status.Requested;
        try {
          request = rqDAO.create(request);

          if (request != null) {
            result = true;
          }
        } catch (Exception ex) {
          // el mismo user pidiendo el mismo TV Show ?
          Logger.debug("TvShow ya pedido por este user");
        }
      }
    }

    return result;
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

}
