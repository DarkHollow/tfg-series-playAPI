package models.service;

import com.google.inject.Inject;
import models.Episode;
import models.Season;
import models.TvShow;
import models.dao.EpisodeDAO;
import models.service.external.ExternalUtils;
import models.service.external.TmdbService;
import play.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class EpisodeService {

  private final EpisodeDAO episodeDAO;
  private final SeasonService seasonService;
  private final TvShowService tvShowService;
  private final TmdbService tmdbService;
  private final ExternalUtils externalUtils;

  private static char SEPARATOR = File.separatorChar;

  @Inject
  public EpisodeService(EpisodeDAO episodeDAO, SeasonService seasonService, TvShowService tvShowService,
                        TmdbService tmdbService, ExternalUtils externalUtils) {
    this.episodeDAO = episodeDAO;
    this.seasonService = seasonService;
    this.tvShowService = tvShowService;
    this.tmdbService = tmdbService;
    this.externalUtils = externalUtils;
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
        Logger.error("EpisodeService.create - Episode no creado");
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

  // obtener por episode number
  public Episode getEpisodeByNumber(TvShow tvShow, Integer seasonNumber, Integer episodeNumber) {
    Season season = seasonService.getSeasonByNumber(tvShow, seasonNumber);
    if (season != null && season.episodes != null) {
      return season.episodes.stream().filter(episode -> episode.episodeNumber.equals(episodeNumber)).findAny().orElse(null);
    }
    return null;
  }

  // Boolean asignar episodios a una temporada y persistir
  public Boolean setEpisodes(Season season, List<Episode> episodes) {
    Boolean result = false;
    try {
      if (episodes != null && episodes.size() > 0) {
        // si hay episodios los creamos
        for (Episode episode: episodes) {
          Logger.info(season.episodes.size() + " episodios");
          episode.season = season;
          season.episodes.add(create(episode));
        }
        Logger.info(season.episodes.size() + " episodios");
        // descargar imágenes episodios
        for (Episode episode: season.episodes) {
          // descargar poster
          if (episode.screenshot != null && !episode.screenshot.isEmpty() && !episode.screenshot.equals("null")) {
            String baseUrl = "https://image.tmdb.org/t/p/original";
            URL downloadURL = new URL(baseUrl + episode.screenshot);
            // generamos nombre a guardar a partir de la primera letra del tipo con la mitad del hashCode en positivo
            String saveName = "e" + season.seasonNumber + "x" + episode.episodeNumber + "-" + externalUtils.positiveHalfHashCode(episode.screenshot.substring(1).hashCode());
            // sacamos la extensión del fichero de imagen
            String format = episode.screenshot.substring(episode.screenshot.lastIndexOf('.') + 1);
            // generamos la ruta donde se guardará la imagen
            String folderPath = "." + SEPARATOR + "public" + SEPARATOR + "images" + SEPARATOR + "series" + SEPARATOR + season.tvShow.id.toString();
            // ruta absoluta
            String path = folderPath + SEPARATOR + saveName + "." + format;
            // descargamos imagen
            String resultPath = externalUtils.downloadImage(downloadURL, format, path);
            if (resultPath != null) {
              episode.screenshot = resultPath.replace("public", "assets");
              Logger.info(season.tvShow.name + " - " + "screenshot episodio " + season.seasonNumber + "x" + episode.episodeNumber + " descargado");
              // borrar imagen antigua
              externalUtils.deleteOldImages(folderPath, "e" + season.seasonNumber + "x" + episode.episodeNumber, saveName + "." + format);
            }
            result = true;
          } else {
            Logger.info("Set episodes - el episodio no tiene screenshot");
          }
        }
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

  // actualizar los episodios de una tv show mediante servicios externos (borramos, y conseguimos de nuevo)
  public TvShow updateEpisodes(TvShow tvShow) throws InterruptedException, ExecutionException, TimeoutException {
    if (tvShow != null) {
      // primero comprobamos si  tiene tmdbId, y si no, lo conseguimos
      if (tvShow.tmdbId == null) {
        tvShow.tmdbId = tvShowService.getObtainTmdbId(tvShow);
      }

      if (tvShow.tmdbId != null) {
        // borramos los episodios actuales
        if (deleteAllEpisodesFromTvShow(tvShow)) {
          // obtenemos todos los episodios
          if (setEpisodesAllSeasonsFromTmdb(tvShow)) {
            Logger.info("Update all episodes - con éxito");
          } else {
            Logger.error("Update all episodes - No se ha podido setear los episodios");
            tvShow = null;
          }
        } else {
          Logger.error("Update all episodes - No se ha podido borrar los episodios actuales");
          tvShow = null;
        }
      } else {
        Logger.info("Update all episodes - No se ha podido obtener la serie en TMDB, por lo tanto, tampoco sus episodios");
        tvShow = null;
      }
    }
    return tvShow;
  }

  // borrar todos los episodios de una temporada
  public Boolean deleteAllEpisodesFromSeason(Season season) {
    Boolean result = false;

    if (season != null) {
      try {
        if (season.episodes != null) {
          List<Integer> toRemove = new ArrayList<>();
          for (Episode episode: season.episodes) {
            toRemove.add(episode.id);
          }
          for (Integer id: toRemove) {
            delete(id);
          }
        }
        result = true;
      } catch (Exception ex) {
        Logger.error("No se han podido borrar los episodes de la serie - " + ex.toString());
      }
    } else {
      Logger.info("Borrar episodios - season null");
    }

    return result;
  }

  // borrar todos los episodios de todas las temporadas de una serie
  public Boolean deleteAllEpisodesFromTvShow(TvShow tvShow) {
    final Boolean[] result = {true};

    if (tvShow != null) {
      try {
        tvShow.seasons.forEach(season -> {
          try {
            if (!deleteAllEpisodesFromSeason(season)) {
              Logger.info("deleteAllEpisodesFromTvShow - no se ha podido eliminar los de la seasonId " + season.id);
            }
          } catch (Exception ex) {
            Logger.error(ex.getMessage());
            result[0] = false;
          }
        });
      } catch (Exception ex) {
        Logger.error("No se han podido borrar TODOS los episodes de la serie - " + ex.toString());
        result[0] = false;
      }
    } else {
      Logger.info("Borrar TODOS los episodios - tvShow null");
      result[0] = false;
    }
    return result[0];
  }

}
