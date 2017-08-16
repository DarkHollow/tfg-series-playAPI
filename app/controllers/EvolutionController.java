package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Evolution;
import models.TvShow;
import models.TvShowRequest;
import models.service.EvolutionService;
import models.service.TvShowRequestService;
import models.service.TvShowService;
import models.service.UserService;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Administrator;
import views.html.administration.index;
import views.html.administration.login;

import java.util.List;

public class EvolutionController extends Controller {
  private final EvolutionService evolutionService;

  @Inject
  public EvolutionController(EvolutionService evolutionService) {
    this.evolutionService = evolutionService;
  }

  @Transactional(readOnly = true)
  @Security.Authenticated(Administrator.class)
  public Result getEvolutionsJSON() {
    ObjectNode result = Json.newObject();

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
      return notFound(result);
    }

    return ok(result);
  }

  @Transactional(readOnly = true)
  @Security.Authenticated(Administrator.class)
  public Result getNotAppliedJSON() {
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

}
