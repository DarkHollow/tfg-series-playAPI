package models.service;

import com.google.inject.Inject;
import models.TvShowRequest;
import models.User;
import models.dao.TvShowRequestDAO;
import play.Logger;

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

  // buscar peticiones por id de TVDB
  public List<TvShowRequest> findTvShowRequests(Integer tvdbId) {
    return rqDAO.findTvShowRequestsByTvdbId(tvdbId);
  }

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

}
