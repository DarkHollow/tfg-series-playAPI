package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import json.SerieViews;
import models.Serie;
import models.service.SerieService;
import models.service.TvShowRequestService;
import models.service.TvdbService;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class SerieController extends Controller {

  private final SerieService serieService;
  private final TvdbService tvdbService;
  private final TvShowRequestService tvShowRequestService;
  private final FormFactory formFactory;

  @Inject
  public SerieController(SerieService serieService, TvdbService tvdbService, TvShowRequestService tvShowRequestService, FormFactory formFactory) {
    this.serieService = serieService;
    this.tvdbService = tvdbService;
    this.tvShowRequestService = tvShowRequestService;
    this.formFactory = formFactory;
  }

  // devolver todas las series (NOTE: futura paginacion?)
  // JSON View SeriesView.SearchSerie: vista que solo incluye los campos
  // relevante de una búsqueda
  @Transactional(readOnly = true)
  public Result all() {
    List<Serie> series = serieService.all();

    // si la lista está vacía, not found
    if (series.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = Json.parse(new ObjectMapper()
                                  .writerWithView(SerieViews.SearchSerie.class)
                                  .writeValueAsString(series));
      return ok(jsonNode);

    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

  // devolver una serie por id
  @Transactional(readOnly = true)
  public Result serieById(Integer id) {
    Serie serie = serieService.find(id);
    if (serie == null) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    }

    // si la lista no está vacía, devolvemos datos
    try {
      JsonNode jsonNode = Json.parse(new ObjectMapper()
                                  .writerWithView(SerieViews.FullSerie.class)
                                  .writeValueAsString(serie));
      return ok(jsonNode);

    } catch (Exception ex) {
      // si hubiese un error, devolver error interno
      ObjectNode result = Json.newObject();
      result.put("error", "It can't be processed");
      return internalServerError(result);
    }
  }

  // devolver la busqueda de series LIKE
  @Transactional(readOnly = true)
  public Result searchSeriesNameLike(String query) {
    if (query.length() >= 3) {
      List<Serie> series = serieService.findBy("seriesName", query, false);

      // si la lista está vacía, not found
      if (series.isEmpty()) {
        ObjectNode result = Json.newObject();
        result.put("error", "Not found");
        return notFound(result);
      }

      // si la lista no está vacía, devolvemos datos
      try {
        JsonNode jsonNode = Json.parse(new ObjectMapper()
                                    .writerWithView(SerieViews.SearchSerie.class)
                                    .writeValueAsString(series));
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

  // Peticion POST TvShow nueva
  // realizar la petición de la serie
  @Transactional
  public Result requestSeries() {
    ObjectNode result = Json.newObject();
    Integer tvdbId, usuarioId;

    // obtenemos datos de la petición post
    DynamicForm requestForm = formFactory.form().bindFromRequest();

    try {
      tvdbId = Integer.valueOf(requestForm.get("tvdbId"));
      usuarioId = Integer.valueOf(requestForm.get("usuarioId"));
    } catch (Exception ex) {
      result.put("error", "tvdbId/usuarioId null or not number");
      return badRequest(result);
    }

    // comprobamos que exista en tvdb y que no la tengamos en local
    if (tvdbId != null && usuarioId != null) {
      Serie serie = tvdbService.findOnTvdbByTvdbId(tvdbId);
      if (serie != null) {
        // comprobamos que esté en local
        if (!serie.local) {
          // intentamos hacer la peticion
          if (tvShowRequestService.requestTvShow(tvdbId, usuarioId)) {
            result.put("ok", "Series request done");
            return ok(result);
          } else {
            // serie ya solicitada por este usuario?
            result.put("error", "This user requested this series already");
            return badRequest(result);
          }
        } else {
          result.put("error", "Series is on local already");
          return badRequest(result);
        }

      } else {
        // la serie no existe en TVDB o ya la tenemos en local
        result.put("error", "Series doesn't exist on TVDB");
        return notFound(result);
      }
    } else {
      // peticion erronea ? tvdbId es null
      result.put("error", "tvdbId/usuarioId can't be null");
      return badRequest(result);
    }
  }

}
