package controllers;

import com.google.inject.Inject;
import models.TvShow;
import models.TvShowRequest;
import models.service.TvShowRequestService;
import models.service.TvShowService;
import models.service.tvdb.TvdbService;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.administration.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AdminController extends Controller {

  private final TvShowService tvShowService;
  private final TvShowRequestService tvShowRequestService;
  private final TvdbService tvdbService;
  private final JPAApi jpa;

  @Inject
  public AdminController(TvShowService tvShowService, TvShowRequestService tvShowRequestService, TvdbService tvdbService, JPAApi jpa) {
    this.tvShowService = tvShowService;
    this.tvShowRequestService = tvShowRequestService;
    this.tvdbService = tvdbService;
    this.jpa = jpa;
  }

  @Transactional(readOnly = true)
  public Result index() {
    // sumario datos

    // series en nuestra base de datos
    Integer tvShowCount = tvShowService.all().size();
    // peticiones nuevas series
    Integer tvShowRequestCount = tvShowRequestService.all().size();

    return ok(index.render("Trending Series Administration - Dashboard", "dashboard", tvShowCount, tvShowRequestCount));
  }

  private List<TvShow> get() {
    // obtener listado de peticiones de serie
    List<TvShowRequest> tvShowRequests = jpa.withTransaction(tvShowRequestService::all);
    // obtener datos de TVDB de cada serie pedida
    List<TvShow> tvShows = new ArrayList<>();
    if (!tvShowRequests.isEmpty()) {
      // recorremos el array de peticiones pidiendo los datos a TVDB
      for (TvShowRequest tvShowRequest : tvShowRequests) {
        TvShow tvShow = tvdbService.findOnTvdbByTvdbId(tvShowRequest.tvdbId);
        if (tvShow != null) {
          tvShows.add(tvShow);
          Logger.debug("" + tvShows.size());
        }
      }
    }

    return tvShows;
  }

  @Transactional
  public CompletionStage<Result> tvShows() {
    return CompletableFuture.supplyAsync(this::get)
            .thenApply(tvShows -> ok(views.html.administration.tvShows.render("Trending Series Administration - Series", "tvShows", tvShows)));
  }
}
