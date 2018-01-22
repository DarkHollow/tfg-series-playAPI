package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Popular;
import models.TvShow;
import models.service.PopularService;
import models.service.TvShowService;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Roles;
import utils.json.JsonViews;

import java.util.ArrayList;
import java.util.List;

public class PopularController extends Controller {

  private final TvShowService tvShowService;
  private final PopularService popularService;
  private final utils.json.Utils jsonUtils;

  @Inject
  public PopularController(TvShowService tvShowService, PopularService popularService, utils.json.Utils jsonUtils) {
    this.tvShowService = tvShowService;
    this.popularService = popularService;
    this.jsonUtils = jsonUtils;
  }

  // devolver las series más populares
  @Transactional(readOnly = true)
  @Security.Authenticated(Roles.class)
  public Result getPopular(Integer querySize) {

    Integer size = 5;
    Integer minimum = 1;
    Integer maximum = 10;
    // comprobar si tiene tamaño en la query
    if (querySize != null && querySize > minimum && querySize <= maximum) {
      size = querySize;
    }

    List<Popular> populars = popularService.getPopular(size);
    // si la lista está vacía, not found
    if (populars.isEmpty()) {
      ObjectNode result = Json.newObject();
      result.put("error", "Not found");
      return notFound(result);
    } else {
      // si la lista no está vacía, devolvemos datos
      try {
        List<TvShow> tvShows = new ArrayList<>();
        for (Popular popular: populars) {
          tvShows.add(popular.tvShow);
        }

        JsonNode jsonNode = jsonUtils.jsonParseObject(tvShows, JsonViews.SearchTvShow.class);
        ObjectNode objectNode = Json.newObject();
        // añadimos popularidad y tendencia de cada serie
        jsonNode.forEach((tvShow) -> {
          Popular popular = tvShowService.find(tvShow.get("id").asInt()).popular;
          ((ObjectNode)tvShow).put("popularity", popular.getPopularity());
          ((ObjectNode)tvShow).put("trend", popular.getTrend());
        });
        // añadimos tamaño
        objectNode.put("size", tvShows.size());
        // añadimos series
        objectNode.set("tvShows", jsonNode);

        return ok(objectNode);
      } catch (Exception ex) {
        // si hubiese un error, devolver error interno
        Logger.debug(ex.getMessage());
        ObjectNode result = Json.newObject();
        result.put("error", "It can't be processed");
        return internalServerError(result);
      }
    }

  }

}
