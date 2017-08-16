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

  @Inject
  public EvolutionService(EvolutionDAO evolutionDAO, TvShowService tvShowService) {
    this.evolutionDAO = evolutionDAO;
    this.tvShowService = tvShowService;
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

    // comparamos el tamaño de las listas
    if (evolutions.size() < playEvolutions.size()) {
      // hay más evolutions de play, por lo que hay evolutions que debemos importar
      Integer index = playEvolutions.size() - evolutions.size() - 1;
      for (int i = index; i < playEvolutions.size(); i++) {
        createEvolution(new Evolution((Integer)playEvolutions.get(i)[0], null));
      }
      result = true;
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
      Logger.error("IllegalAccessException - No se ha podido aplicar la evolución: \" + version");
    } catch (InvocationTargetException e) {
      Logger.error("InvocationTargetException - No se ha podido aplicar la evolución: \" + version");
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

}
