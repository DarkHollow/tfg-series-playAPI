package models.service;

import com.google.inject.Inject;
import models.Season;
import models.dao.SeasonDAO;
import play.Logger;

public class SeasonService {

  private final SeasonDAO seasonDAO;
  private final TvShowService tvShowService;

  @Inject
  public SeasonService(SeasonDAO seasonDAO, TvShowService tvShowService) {
    this.seasonDAO = seasonDAO;
    this.tvShowService = tvShowService;
  }

  // CRUD

  // Create
  public Season create(Season season) {
    if (season != null && season.seasonNumber != null && season.tvShow != null) {
      // creamos temporada
      Season seasonCreated = seasonDAO.create(season);
      if (seasonCreated != null) {
        return seasonCreated;
      } else {
        // votacion no creada
        Logger.error("SeasonService.create - Temporada no creada");
        return null;
      }
    } else {
      Logger.error("A la temporada le faltan datos para poder ser creado");
      return null;
    }
  }

  // Read de busqueda
  // buscar por id
  public Season find(Integer id) {
    return seasonDAO.find(id);
  }

  // Delete por id
  public Boolean delete(Integer id) {
    Season season = seasonDAO.find(id);

    if (season != null) {
      // me elimino de mis padres
      Integer tvShowId = season.tvShow.id;
      tvShowService.find(tvShowId).seasons.remove(season);

      // finalmente, me elimino yo
      seasonDAO.delete(season);
      Logger.debug("en teoria existe y borrado...");
      return true;
    } else {
      Logger.debug("No existe?");
      return false;
    }
  }

}
