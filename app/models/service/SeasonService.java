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

  // obtener por season number
  public Season getSeasonByNumber(TvShow tvShow, Integer seasonNumber) {
    if (tvShow != null && tvShow.seasons != null) {
      return tvShow.seasons.stream().filter(season -> season.seasonNumber.equals(seasonNumber)).findAny().orElse(null);
    }
    return null;
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
      JsonNode jsonTvShow;
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

  // rellenar temporadas - una vez las temporadas están persistidas, rellenamos los campos restantes pidiendo a TMDb
  public Boolean fullfillSeasons(TvShow tvShow) {
    Boolean result = false;
    try {
      if (tvShow.seasons != null) {
        for (Season season: tvShow.seasons) {
          Season tmdbSeason = tmdbService.getCompleteSeasonByTmdbIdAndSeasonNumber(tvShow.tmdbId, season.seasonNumber);
          if (tmdbSeason != null) {
            season.name = tmdbSeason.name;
            season.overview = tmdbSeason.overview;
            season.firstAired = tmdbSeason.firstAired;
            // descargar poster
            if (tmdbSeason.poster != null && !tmdbSeason.poster.isEmpty() && !tmdbSeason.poster.equals("null")) {
              String baseUrl = "https://image.tmdb.org/t/p/original";
              URL downloadURL = new URL(baseUrl + tmdbSeason.poster);
              // generamos nombre a guardar a partir de la primera letra del tipo con la mitad del hashCode en positivo
              String saveName = "s" + season.seasonNumber + "-" + externalUtils.positiveHalfHashCode(tmdbSeason.poster.substring(1).hashCode());
              // sacamos la extensión del fichero de imagen
              String format = tmdbSeason.poster.substring(tmdbSeason.poster.lastIndexOf('.') + 1);
              // generamos la ruta donde se guardará la imagen
              String folderPath = "." + SEPARATOR + "public" + SEPARATOR + "images" + SEPARATOR + "series" + SEPARATOR + tvShow.id.toString();
              // ruta absoluta
              String path = folderPath + SEPARATOR + saveName + "." + format;
              // descargamos imagen
              String resultPath = externalUtils.downloadImage(downloadURL, format, path);
              if (resultPath != null) {
                season.poster = resultPath.replace("public", "assets");
                Logger.info(tvShow.name + " - " + "poster season " + season.seasonNumber + " descargado");
                // borrar imagen antigua
                externalUtils.deleteOldImages(folderPath, "s" + season.seasonNumber, saveName + "." + format);
              }
              result = true;
            } else {
              Logger.info("Season Fullfill - la temporada no tiene poster");
            }
          }
        }
      } else {
        Logger.info("Seasons Fullfill - no hay temporadas");
      }
    } catch (Exception ex) {
      Logger.error("Get Complete Season error - " + tvShow.name);
    }
    return result;
  }

  // actualizar la temporadas de una tv show mediante servicios externos (borramos, y conseguimos de nuevo)
  public TvShow updateSeasons(TvShow tvShow) throws InterruptedException, ExecutionException, TimeoutException {
    if (tvShow != null) {
      // primero comprobamos si  tiene tmdbId, y si no, lo conseguimos
      if (tvShow.tmdbId == null) {
        TvShow tmdbShow = tmdbService.findByTvdbId(tvShow.tvdbId);
        if (tmdbShow != null) {
          tvShow.tmdbId = tmdbShow.tmdbId;
        }
      }

      if (tvShow.tmdbId != null) {
        // obtenemos temporadas
        List<Season> seasons = getSeasonsFromTmdbByTmdbId(tvShow.tmdbId);

        // borramos las temporadas actuales
        if (deleteSeasons(tvShow)) {
          if (setSeasons(tvShow, seasons)) {
            // obtenemos todos los datos de las temporadas ya creadas
            fullfillSeasons(tvShow);
          } else {
            Logger.error("Update Seasons - No se ha podido setear las temporadas vacías");
            tvShow = null;
          }
        } else {
          Logger.error("Update Seasons - No se ha podido borrar las temporadas actuales");
          tvShow = null;
        }
      } else {
        Logger.info("Update Seasons - No se ha podido obtener la serie en TMDB, por lo tanto, tampoco sus temporadas");
        tvShow = null;
      }
    }
    return tvShow;
  }

}
