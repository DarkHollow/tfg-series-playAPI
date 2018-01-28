package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import utils.json.JsonViews;
import models.Evolution;
import models.service.EvolutionService;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Administrator;

import java.util.List;

public class EvolutionController extends Controller {
  private final EvolutionService evolutionService;
  private final FormFactory formFactory;
  private final utils.json.Utils jsonUtils;

  @Inject
  public EvolutionController(EvolutionService evolutionService, FormFactory formFactory, utils.json.Utils jsonUtils) {
    this.evolutionService = evolutionService;
    this.formFactory = formFactory;
    this.jsonUtils = jsonUtils;
  }

  @Transactional(readOnly = true)
  @Security.Authenticated(Administrator.class)
  public Result getEvolutions(String status) {
    ObjectNode result = Json.newObject();

    // comprobar si contiene parámetros
    if (!status.isEmpty()) {
      // comprobamos si se quiere obtener las evolutions no aplicadas
      if (status.equals("not-applied")) {
        return getNotApplied();
      } else {
        // status no reconocido
        result.put("error", "parameters not valid");
        return badRequest(result);
      }
    }

    // todas la evolutions
    List<Evolution> allEvolutions = evolutionService.all();
    try {
      JsonNode jsonNode = jsonUtils.jsonParseObject(allEvolutions, JsonViews.FullTvShow.class);
      result.set("evolutions", jsonNode);
    } catch (Exception ex) {
      Logger.error("Cannot parse evolutions object");
      result.put("evolutions", "error parsing");
    }

    // información de última versión aplicada
    Evolution lastApplied = evolutionService.getLastApplied();
    if (lastApplied != null) {
      result.put("actualVersion", lastApplied.version.toString());
    } else {
      result.put("actualVersion", "0");
    }

    // información de nueva y última versión
    List<Evolution> notApplied = evolutionService.getNotApplied();
    if (!notApplied.isEmpty()) {
      // si hay evolution no aplicada
      result.put("newVersion", notApplied.get(notApplied.size() - 1).version.toString());
    } else {
      // si no hay evolution no aplicada
      result.put("newVersion", "none");
    }

    return ok(result);
  }

  @Transactional(readOnly = true)
  @Security.Authenticated(Administrator.class)
  private Result getNotApplied() {
    ObjectNode result = Json.newObject();

    // información de actualizaciones necesarias para alcanzar la nueva y última versión
    List<Evolution> notApplied = evolutionService.getNotApplied();
    try {
      JsonNode jsonNode = Json.parse(new ObjectMapper().writeValueAsString(notApplied));
      result.set("evolutions", jsonNode);
    } catch (JsonProcessingException e) {
      Logger.error("Error parseando datos lista evolutions a JSON");
      result.put("evolutions", "error");
      return internalServerError(result);
    }

    return ok(result);
  }

  @Transactional
  @Security.Authenticated(Administrator.class)
    public Result applyEvolutionJSON() {
    ObjectNode result = Json.newObject();
    Integer evolutionId;

    // obtenemos datos de la evolution a aplicar de la petición post
    DynamicForm requestForm = formFactory.form().bindFromRequest();

    try {
      evolutionId = Integer.valueOf(requestForm.get("evolutionId"));
    } catch (Exception ex) {
      result.put("error", "evolutionId null or not number");
      return badRequest(result);
    }

    // comprobaciones rutinarias
    if (evolutionId != null) {
      // comprobamos que exista
      Evolution evolution = evolutionService.find(evolutionId);
      if (evolution != null) {
        // comprobamos que esté sin aplicar
        if (evolution.state == null || !evolution.state.equals("applied")) {
          // intentamos aplicar evolution
          if (evolutionService.applyEvolution(evolution)) {
            result.put("ok", "evolution applied successfully");
            result.put("message", "Evolución v" + evolution.version + " aplicada con éxito");
            return ok(result);
          } else {
            // esta aplicada??
            result.put("error", "evolution not applied correctly");
            result.put("message", "La evolución no se ha ejecutado correctamente");
            return internalServerError(result);
          }
        } else {
          // esta aplicada??
          result.put("error", "evolution already applied");
          result.put("message", "Evolución ya aplicada");
          return badRequest(result);
        }
      } else {
        // evolution no encontrada
        // esta aplicada??
        result.put("error", "evolution not found");
        result.put("message", "Evolución no encontrada");
        return notFound(result);
      }
    } else {
      // evolution id nula
      result.put("error", "evolutionId null or not number");
      result.put("message", "El parámetro evolutionId es null o no es tipo número");
      return badRequest(result);
    }

  }

}
