package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import json.TvShowViews;
import models.TvShow;
import models.service.TvShowService;
import models.service.tvdb.TvdbService;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Administrator;
import utils.Security.Roles;

import java.util.List;

public class TvShowController extends Controller {

  private final TvShowService tvShowService;
  private final FormFactory formFactory;

  @Inject
  public TvShowController(TvShowService tvShowService, FormFactory formFactory) {
    this.tvShowService = tvShowService;
    this.formFactory = formFactory;
  }

  // devolver todas los TV Shows (NOTE: futura paginacion?)
  // JSON View TvShowView.SearchTvShow: vista que solo incluye los campos
  // relevante de una búsqueda
  @Transactional(readOnly = true)
  @Security.Authenticated(Administrator.class)
  public Result all() {
    List<TvShow> tvShows = tvShowService.all();

    // si la lista está vacía, not found
    if (tvShows.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = Json.parse(new ObjectMapper()
                                  .writerWithView(TvShowViews.SearchTvShow.class)
                                  .writeValueAsString(tvShows));
      return ok(jsonNode);

    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

  // devolver un TV Show por id
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result tvShowById(Integer id) {
    TvShow tvShow = tvShowService.find(id);
    if (tvShow == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = Json.parse(new ObjectMapper()
                                  .writerWithView(TvShowViews.FullTvShow.class)
                                  .writeValueAsString(tvShow));
      return ok(jsonNode);

    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

  // devolver la busqueda de TV Shows LIKE
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result searchTvShowNameLike(String query) {
    if (query.length() >= 3) {
      List<TvShow> tvShows = tvShowService.findBy("name", query, false);

      // si la lista está vacía, not found
      if (tvShows.isEmpty()) {
        ObjectNode result = Json.newObject();
        result.put("error", "Not found");
        return notFound(result);
      }

      // si la lista no está vacía, devolvemos datos
      try {
        JsonNode jsonNode = Json.parse(new ObjectMapper()
                                    .writerWithView(TvShowViews.SearchTvShow.class)
                                    .writeValueAsString(tvShows));
        return ok(jsonNode);

      } catch (Exception ex) {
        // si hubiese un error, devolver error interno
        ObjectNode result = Json.newObject();
        result.put("error", "It can't be processed");
        return internalServerError(result);
      }
    } else {
      ObjectNode result = Json.newObject();
      result.put("error", "Bad request");
      return badRequest(result);
    }
  }

  @Transactional
  @Security.Authenticated(Administrator.class)
  public Result updateTvShowData(Integer id) {
    ObjectNode result = Json.newObject();
    TvShow tvShow = tvShowService.find(id);

    if (tvShow != null) {

      // comprobamos qué recurso ha de ser actualizado
      DynamicForm requestForm = formFactory.form().bindFromRequest();
      String request;
      try {
        request = requestForm.get("update");
      } catch (Exception ex) {
        result.put("error", "update null");
        return badRequest(result);
      }

      switch (request) {
        case "data":
          tvShow = tvShowService.updateData(tvShow);
          break;
        default:
          Logger.error("Actualizar " + request + ": el tipo de datos a actualizar de la serie no coincide con ninguno conocido: '" + request + "'");
      }

      // si el tvShow no es null, se ha actualizado correctamente
      if (tvShow != null) {
        result.put("ok", "TV Show " + request + " successfully updated");
        result.put("message", "Serie " + request + "  actualizada correctamente");
        try {
          JsonNode jsonNode = Json.parse(new ObjectMapper()
                  .writerWithView(TvShowViews.FullTvShow.class)
                  .writeValueAsString(tvShow));
          result.set("tvShow", jsonNode);
        } catch (JsonProcessingException e) {
          Logger.error("Error parseando datos serie a JSON, serie  " + request + "  actualizada igualmente");
        }
      } else {
        // no se ha podido actualizar
        result.put("error", "Not found");
        result.put("message", "Recurso no encontrado o error");
        return notFound(result);
      }
    } else {
      // no se ha podido actualizar
      result.put("error", "Not found");
      result.put("message", "Recurso no encontrado");
      return notFound(result);
    }

    return ok(result);
  }


}
