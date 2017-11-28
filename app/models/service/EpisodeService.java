package models.service;

import com.google.inject.Inject;
import models.Episode;
import models.Season;
import models.TvShow;
import models.dao.EpisodeDAO;
import models.service.external.TmdbService;
import play.Logger;

import java.io.File;
import java.util.List;

public class EpisodeService {

  private final EpisodeDAO episodeDAO;
  private final SeasonService seasonService;
  private final TvShowService tvShowService;
  private final TmdbService tmdbService;

  private static char SEPARATOR = File.separatorChar;

  @Inject
  public EpisodeService(EpisodeDAO episodeDAO, SeasonService seasonService, TvShowService tvShowService,
                        TmdbService tmdbService) {
    this.episodeDAO = episodeDAO;
    this.seasonService = seasonService;
    this.tvShowService = tvShowService;
    this.tmdbService = tmdbService;
  }

  // CRUD

  // Create
  public Episode create(Episode episode) {
    if (episode != null && episode.episodeNumber != null && episode.season != null) {
      // creamos episode
      Episode episodeCreated = episodeDAO.create(episode);
      if (episodeCreated != null) {
        return episodeCreated;
      } else {
        // votacion no creada
        Logger.error("EpisodeService.create - Episode no creadn");
        return null;
      }
    } else {
      Logger.error("EpisodeService.create - Al episode le faltan datos para poder ser creado");
      return null;
    }
  }

  // Read de busqueda
  // buscar por id
  public Episode find(Integer id) {
    return episodeDAO.find(id);
  }

  // Delete por id
  public Boolean delete(Integer id) {
    Episode episode = episodeDAO.find(id);

    if (episode != null) {
      // me elimino de mis padres
      Integer seasonId = episode.season.id;
      seasonService.find(seasonId).episodes.remove(episode);

      // elimino mi season asignada
      episode.season = null;

      // finalmente, me elimino yo
      episodeDAO.delete(episode);
      Logger.debug("EpisodeSerice.delete - en teoria existe y borrado...");
      return true;
    } else {
      Logger.debug("EpisodeSerice.delete - No existe?");
      return false;
    }
  }

  // Boolean asignar episodios a una temporada y persistir
  public Boolean setEpisodes(Season season, List<Episode> episodes) {
    Boolean result = false;
    try {
      if (episodes != null && episodes.size() > 0) {
        // si hay temporadas las creamos
        for (Episode episode: episodes) {
          Logger.info(season.episodes.size() + " episodios");
          episode.season = season;
          season.episodes.add(create(episode));
        }
        Logger.info(season.episodes.size() + " episodios");
      }
      result = true;
    } catch (Exception ex) {
      Logger.error(ex.getMessage());
    }
    return result;
  }

  // Boolean asignar episodios a todas las temporadas de un tv show obtenidos de TMDb
  public Boolean setEpisodesAllSeasonsFromTmdb(TvShow tvShow) {
    Boolean result = false;
    Integer tmdbId = tvShowService.getObtainTmdbId(tvShow);
    if (tmdbId != null) {
      tvShow.seasons.forEach(season -> {
        try {
          setEpisodes(season, tmdbService.getAllSeasonEpisodesByTmdbIdAndSeasonNumber(tmdbId, season.seasonNumber));
        } catch (Exception ex) {
          Logger.error(ex.getMessage());
        }
      });
      result = true;
    }

    return result;
  }

}
