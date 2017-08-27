package models.service;

import com.google.inject.Inject;
import models.TvShowRequest;
import models.TvShowRequest.Status;
import models.User;
import models.dao.TvShowRequestDAO;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

public class TvShowRequestService {

  private final TvShowService tvShowService;
  private final TvShowRequestDAO rqDAO;

  @Inject
  public TvShowRequestService(TvShowService tvShowService, TvShowRequestDAO rqDAO) {
    this.tvShowService = tvShowService;
    this.rqDAO = rqDAO;
  }

  // create
  public TvShowRequest create(TvShowRequest request) {
    // comprobamos que la serie no exista ya
    if (tvShowService.findByTvdbId(request.tvdbId) == null) {
      // comprobar si existe ya petición para actualizarla
      TvShowRequest actualRequest = findTvShowRequestByTvdbId(request.tvdbId);
      if (actualRequest != null) {
        if (update(actualRequest, null, Status.Processing) != null) {
          return update(actualRequest, request.user, Status.Requested);
        } else {
          Logger.error("TvShowRequest Service - create: no se puede actualizar request ya existente");
          return null;
        }
      } else {
        // crear request nueva
        request.status = Status.Requested;
        request.requestCount = 1;
        return rqDAO.create(request);
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

  // update peticion
  public TvShowRequest update(TvShowRequest request, User user, Status status) {
    if (request != null) {
      // comprobamos status
      if (status != null) {
        Status actualStatus = request.status;
        // cambios posibles (los ponemos por si el status fuera no válido!)
        switch (status) {
          case Requested:
            if (!(actualStatus.equals(Status.Requested) ||
                    actualStatus.equals(Status.Processing))) {
              return null;
            }
            break;
          case Processing:
            if (!(actualStatus.equals(Status.Requested) ||
                    actualStatus.equals(Status.Processing) ||
                    actualStatus.equals(Status.Persisted) ||
                    actualStatus.equals(Status.Rejected) ||
                    actualStatus.equals(Status.Deleted))) {
              return null;
            }
            break;
          case Persisted:
            if (!(actualStatus.equals(Status.Persisted) ||
                    actualStatus.equals(Status.Processing))) {
              return null;
            }
            break;
          case Rejected:
            if (!(actualStatus.equals(Status.Rejected) ||
                    actualStatus.equals(Status.Processing))) {
              return null;
            }
            break;
          case Deleted:
            if (!(actualStatus.equals(Status.Deleted) ||
                    actualStatus.equals(Status.Processing))) {
              return null;
            }
            break;
          default:
            Logger.error("Request Service update - Status de petición no reconocido");
            return null;
        }

        // si el estado estado anterior es rejected or deleted y el nuevo no es processing:
        // actualizar usuario y contador + 1, porque significa que el usuario está volviendo a pedir serie
        if (user != null &&
                status != Status.Processing &&
                (request.lastStatus.equals(Status.Rejected) || request.lastStatus.equals(Status.Deleted))) {
          request.user = user;
          request.requestCount++;
        }

        // guardamos en lastStatus el estado anterior siempre que el actual no sea processing,
        // o que el nuevo sea igual que el actual
        if (actualStatus != Status.Processing) {
          request.lastStatus = request.status;
        }
        request.status = status;
        return request;
      } else {
        // status null
        return null;
      }
    } else {
      Logger.error("Request Service update - request null");
      return null;
    }
  }

  // update peticion status string
  public TvShowRequest update(TvShowRequest request, User user, String status) {
    Status newStatus = null;
    // comprobamos si hay status nuevo a transformar
    if (status != null) {
      switch (status) {
        case "Requested":
          newStatus = Status.Requested;
          break;
        case "Processing":
          newStatus = Status.Processing;
          break;
        case "Persisted":
          newStatus = Status.Persisted;
          break;
        case "Rejected":
          newStatus = Status.Rejected;
          break;
        case "Deleted":
          newStatus = Status.Deleted;
          break;
        default:
          Logger.error("Request Service update - Status de petición no reconocido");
          return null;
      }
    }

    return update(request, user, newStatus);
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
    Boolean result = false;
    TvShowRequest request = findTvShowRequestByTvdbId(tvdbId);
    if (request != null) {
      if (update(request, null, Status.Deleted) != null) {
        result = true;
      } else {
        Logger.error("TvShowRequestService - delete: no se ha podido cambiar el estado de la petición a Deleted");
      }
    } else {
      // no existe la peticion
      Logger.error("TvShowRequestService - delete: no existe la petición");
    }
    return result;
  }

}
