package models.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Season;
import models.TvShow;
import models.dao.SeasonDAO;
import models.service.external.ExternalUtils;
import models.service.external.TmdbService;
import play.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SeasonService {

  private final SeasonDAO seasonDAO;
  private final TvShowService tvShowService;
  private final TmdbService tmdbService;
  private final ExternalUtils externalUtils;

  private static char SEPARATOR = File.separatorChar;

  @Inject
  public SeasonService(SeasonDAO seasonDAO, TvShowService tvShowService, TmdbService tmdbService,
                       ExternalUtils externalUtils) {
    this.seasonDAO = seasonDAO;
    this.tvShowService = tvShowService;
    this.tmdbService = tmdbService;
    this.externalUtils = externalUtils;
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

      // elimino mi serie asignada
      season.tvShow = null;

      // finalmente, me elimino yo
      seasonDAO.delete(season);
      Logger.debug("en teoria existe y borrado...");
      return true;
    } else {
      Logger.debug("No existe?");
      return false;
    }
  }

  // obtener lista de temporadas de TMDb por TMDb id (temporadas sencillas: poca información)
  public List<Season> getSeasonsFromTmdbByTmdbId(Integer tmdbId) {
    List<Season> seasons = null;
    if (tmdbId != null) {
      seasons = new ArrayList<>();
      JsonNode jsonTvShow = null;
      try {
        jsonTvShow = tmdbService.getJsonTvShowByTmdbId(tmdbId);
        if (jsonTvShow.withArray("seasons").size() > 0) {
          for (int i = 0; i < jsonTvShow.withArray("seasons").size(); i++) {
            JsonNode jsonSeason = jsonTvShow.withArray("seasons").get(i);
            Season season = new Season();
            season.seasonNumber = jsonSeason.get("season_number").asInt();
            seasons.add(season);
          }
        }
      } catch (Exception ex) {
        Logger.error("Error obteniendo serie en TMDb - " + ex.getMessage());
      }
    }
    return seasons;
  }

  // asignar a una tv show una lista de temporadas y persistimos
  public Boolean setSeasons(TvShow tvShow, List<Season> seasons) {
    Boolean result = false;
    try {
      if (seasons != null && seasons.size() > 0) {
        // si hay temporadas las creamos
        for (Season season: seasons) {
          Logger.info(tvShow.seasons.size() + " temporadas");
          season.tvShow = tvShow;
          tvShow.seasons.add(create(season));
        }
        Logger.info(tvShow.seasons.size() + " temporadas");
      }
      result = true;
    } catch (Exception ex) {
      Logger.error(ex.getMessage());
    }
    return result;
  }

  // borrar las temporadas de una tv show
  public Boolean deleteSeasons(TvShow tvShow) {
    Boolean result = false;

    if (tvShow != null) {
      try {
        if (tvShow.seasons != null) {
          List<Integer> toRemove = new ArrayList<>();
          for (Season season: tvShow.seasons) {
            toRemove.add(season.id);
          }
          for (Integer id: toRemove) {
            delete(id);
          }
        }
        result = true;
      } catch (Exception ex) {
        Logger.error("No se han podido borrar las temporadas de la serie - " + ex.toString());
      }
    } else {
      Logger.info("Borrar temporadas - tvShow null");
    }

    return result;
  }

}
