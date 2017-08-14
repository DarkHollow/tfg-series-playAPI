package models.service;

import models.Evolution;
import models.dao.EvolutionDAO;
import play.Logger;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class EvolutionService {
  private final EvolutionDAO evolutionDAO;

  @Inject
  public EvolutionService(EvolutionDAO evolutionDAO) {
    this.evolutionDAO = evolutionDAO;
  }

  public Evolution createEvolution(Evolution evolution) {
    return evolutionDAO.create(evolution);
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

}
