package models.service;

import models.Evolution;
import models.TvShow;
import models.dao.EvolutionDAO;
import play.Logger;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EvolutionService {
  private final EvolutionDAO evolutionDAO;
  private final TvShowService tvShowService;
  private final SeasonService seasonService;
  private final EpisodeService episodeService;

  @Inject
  public EvolutionService(EvolutionDAO evolutionDAO, TvShowService tvShowService, SeasonService seasonService,
                          EpisodeService episodeService) {
    this.evolutionDAO = evolutionDAO;
    this.tvShowService = tvShowService;
    this.seasonService = seasonService;
    this.episodeService = episodeService;
  }

  public Evolution createEvolution(Evolution evolution) {
    return evolutionDAO.create(evolution);
  }

  // buscar por id
  public Evolution find(Integer id) {
    return evolutionDAO.find(id);
  }

  public List<Evolution> all() {
    return evolutionDAO.all();
  }

  public Evolution getLastApplied() {
    Evolution result = null;

    // obtenemos todas
    List<Evolution> evolutions = all();
    // devolvemos la última aplicada
    for (Evolution evolution: evolutions) {
      if (evolution.state != null && evolution.state.equals("applied")) {
        result = evolution;
      }
    }

    return result;
  }

  public List<Evolution> getNotApplied() {
    List<Evolution> evolutions = all();
    evolutions.removeIf(evolution -> evolution.state != null);
    Logger.debug("Evolutions sin aplicar: " + evolutions.size());
    return evolutions;
  }

  // importa las evolutions de play que no tengamos ya importadas
  // devuelve true si ha importado una o más evolutions
  private Boolean importPlayEvolutions() {
    Boolean result = false;

    // obtenemos nuestras evolutions
    List<Evolution> evolutions = evolutionDAO.all();
    // obtenemos las play evolutions aplicadas
    List<Object[]> playEvolutions = evolutionDAO.getAppliedPlayEvolutions();

    // ver qué evolutions no tenemos aplicadas
    if (evolutions != null && playEvolutions != null) {
      // sacamos ids de todas las play evolutions
      List<Integer> playEvolutionsIds = new ArrayList<>();
      for (Object[] playEvolution: playEvolutions) {
        playEvolutionsIds.add((Integer)playEvolution[0]);
      }

      // obtenemos ids de las play evolutions no aplicadas
      List<Integer> notAppliedEvolutionsIds = new ArrayList<>();
      for (Integer id: playEvolutionsIds) {
        if (evolutions.stream().noneMatch(evolution -> id.equals(evolution.version))) {
          notAppliedEvolutionsIds.add(id);
          result = true;
        }
      }

      // creamos evolutions de ids que no tenemos
      for (Integer id: notAppliedEvolutionsIds) {
        createEvolution(new Evolution(id, null));
      }
    }

    return result;
  }

  public Boolean needEvolution() {
    Boolean result = false;

    // intentamos importar evolutions no efectuadas
    if (importPlayEvolutions()) {
      Logger.info("Se han importado evolutions");
    }

    // comprobar si hay evolutions aplicadas con datos sin migrar
    List<Evolution> evolutions = evolutionDAO.all();
    if (evolutions.stream().anyMatch(item -> null == item.state)) {
      result = true;
    }

    return result;
  }

  public Boolean applyEvolution(Evolution evolution) {
    Boolean result = false;
    String version = evolution.version.toString();

    try {
      Method method = this.getClass().getDeclaredMethod("applyEvolution" + version);
      method.invoke(this);

      // si se ha aplicado, evolution = applied
      evolution.state = "applied";
      result = true;
    } catch (NoSuchMethodException e) {
      Logger.error("NoSuchMethodException - No se ha podido aplicar la evolución: " + version);
    } catch (IllegalAccessException e) {
      Logger.error("IllegalAccessException - No se ha podido aplicar la evolución: " + version);
    } catch (InvocationTargetException e) {
      Logger.error("InvocationTargetException - No se ha podido aplicar la evolución: " + version);
    }

    return result;
  }

  /* ejemplo de evolucion
   *  X = versión a aplicar
  public Boolean applyEvolutionX() {
    Boolean result = false;

    // hacemos lo necesario para actualizar los datos
    result = true;

    return result;
  }
  */

  // Evolution 1: nuevos atributos de TvShow: score, voteCount, inicializarlos en series ya persistidas
  public Boolean applyEvolution1() {
    try {
      List<TvShow> tvShows = tvShowService.all();
      if (!tvShows.isEmpty()) {
        Logger.info("Evolution 1 - Actualizando series...");
        // si no está vacía, inicializar nuevos campos
        for (TvShow tvShow: tvShows) {
          if (tvShow.score == null || tvShow.voteCount == null) {
            tvShow.score = 0.0f;
            tvShow.voteCount = 0;
          }
        }
      } else {
        Logger.info("Evolution 1 - No hay series que actualizar");
      }
    } catch (Exception ex) {
      Logger.error("Evolution 1 - No se ha podido actualizar: " + ex.getClass().toString());
      return false;
    }

    Logger.info("Evolution 1 - Actualización finalizada con éxito");
    return true;
  }

  // Evolution 2
  // - nuevo atributo de TvShow: tmdbId, obtener ese id en series ya persistidas
  // - nuevo model Season, obtener temporadas para las series ya persistidas
  public Boolean applyEvolution2() {
    try {
      List<TvShow> tvShows = tvShowService.all();
      if (!tvShows.isEmpty()) {
        Logger.info("Evolution 2 - Actualizando series...\nEvolution 2 - este proceso tardará en proporción a la cantidad" +
                " de series de las que disponga el sistema persistidas.");
        // si no está vacía, obtener TMDb ids de todas la series y obtener sus temporadas
        for (TvShow tvShow: tvShows) {
          // updateSeasons se encarga de ambas cosas, de obtener el tmdbId si no lo tiene
          // y después borra las temporadas actuales y descarga todas
          seasonService.updateSeasons(tvShow);
        }
      } else {
        Logger.info("Evolution 2 - No hay series que actualizar");
      }
    } catch (Exception ex) {
      Logger.error("Evolution 2 - No se ha podido actualizar: " + ex.getClass().toString());
      return false;
    }

    Logger.info("Evolution 2 - Actualización finalizada con éxito");
    return true;
  }

  // Evolution 3
  // nuevo model Episode, obtener episodios para las series ya persistidas
  public Boolean applyEvolution3() {
    try {
      List<TvShow> tvShows = tvShowService.all();
      if (tvShows != null && !tvShows.isEmpty()) {
        Logger.info("Evolution 3 - Actualizando series...\nEvolution 3 - este proceso tardará en proporción a la cantidad" +
                " de series de las que disponga el sistema persistidas.");
        for (TvShow tvShow: tvShows) {
          // updateEpisodes
          if (tvShow.seasons == null) {
            // temporadas null ?? Obtenerlas
            seasonService.updateSeasons(tvShow);
          }
          if (tvShow.seasons != null) {
            episodeService.updateEpisodes(tvShow);
          } else {
            Logger.error("No se puede obtener los episodios por error de temporadas");
          }
        }
      } else {
        Logger.info("Evolution 3 - No hay series que actualizar");
      }
    } catch (Exception ex) {
      Logger.error("Evolution 3 - No se ha podido actualizar: " + ex.getClass().toString());
      return false;
    }

    Logger.info("Evolution 3 - Actualización finalizada con éxito");
    return true;
  }

}
