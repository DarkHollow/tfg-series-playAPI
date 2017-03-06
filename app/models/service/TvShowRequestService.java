package models.service;

import com.google.inject.Inject;
import models.TvShowRequest;
import models.Usuario;
import models.dao.TvShowRequestDAO;
import play.Logger;

public class TvShowRequestService {

  private final SerieService serieService;
  private final UsuarioService usuarioService;
  private final TvShowRequestDAO rqDAO;

  @Inject
  public TvShowRequestService(SerieService serieService, UsuarioService usuarioService, TvShowRequestDAO rqDAO) {
    this.serieService = serieService;
    this.usuarioService = usuarioService;
    this.rqDAO = rqDAO;
  }

  // POST petición serie
  public Boolean requestTvShow(Integer tvdbId, Integer usuarioId) {
    Boolean result = false;

    // comprobamos que no esté en nuetra base de datos ya
    if (serieService.findByTvdbId(tvdbId) == null) {
      // encontrar al usuario
      Usuario usuario = usuarioService.find(usuarioId);
      if (usuario != null) {
        // hacemos la peticion
        TvShowRequest request = new TvShowRequest(tvdbId, usuario);
        try {
          request = rqDAO.create(request);

          if (request != null) {
            result = true;
          }
        } catch (Exception ex) {
          // el mismo usuario pidiendo la misma serie?
          Logger.debug("Serie ya pedida por este usuario");
        }
      }
    }

    return result;
  }

}
