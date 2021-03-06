package controllers;

import com.google.inject.Inject;
import models.Popular;
import models.TvShow;
import models.TvShowRequest;
import models.service.*;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Administrator;
import views.html.administration.index;
import views.html.administration.login;

import java.util.Date;
import java.util.List;

public class AdminController extends Controller {

  private final TvShowService tvShowService;
  private final PopularService popularService;
  private final TvShowRequestService tvShowRequestService;
  private final UserService userService;
  private final utils.Security.Administrator adminAuth;
  private final EvolutionService evolutionService;

  @Inject
  public AdminController(TvShowService tvShowService, PopularService popularService, TvShowRequestService tvShowRequestService, UserService userService, utils.Security.Administrator adminAuth, EvolutionService evolutionService) {
    this.tvShowService = tvShowService;
    this.popularService = popularService;
    this.tvShowRequestService = tvShowRequestService;
    this.userService = userService;
    this.adminAuth = adminAuth;
    this.evolutionService = evolutionService;
  }

  @Transactional
  @Security.Authenticated(Administrator.class)
  public Result index() {
    // sumario datos

    // series en nuestra base de datos
    Integer tvShowCount = tvShowService.all().size();
    // series eliminadas a partir de peticiones de series eliminadas
    Integer deletedTvShowCount = tvShowRequestService.getDeleted().size();
    // peticiones nuevas series
    Integer requestsCount = tvShowRequestService.all().size();
    Integer pendingRequestsCount = tvShowRequestService.getPending().size();
    Integer persistedRequestsCount = tvShowRequestService.getPersisted().size();
    Integer rejectedRequestsCount = tvShowRequestService.getRejected().size();
    return ok(index.render("Trending Series Administration - Dashboard", "dashboard", tvShowCount, deletedTvShowCount, requestsCount, pendingRequestsCount, persistedRequestsCount, rejectedRequestsCount, evolutionService));
  }

  @Transactional
  @Security.Authenticated(Administrator.class)
  public Result tvShows() {
    List<TvShow> tvShows = tvShowService.all();
    List<TvShowRequest> deletedRequests = tvShowRequestService.getDeleted();
    return ok(views.html.administration.tvShows.render("Trending Series Administration - Series", "tvShows", tvShows, deletedRequests, evolutionService));
  }

  @Transactional
  @Security.Authenticated(Administrator.class)
  public Result tvShow(Integer tvShowId) {
    TvShow tvShow = tvShowService.find(tvShowId);
    String title = "Trending Series Administration - Serie";
    if (tvShow != null) {
      title = title + " " + tvShow.name;
    }
    return ok(views.html.administration.tvShow.render(title, "tvShow", tvShow, evolutionService));
  }

  @Transactional
  @Security.Authenticated(Administrator.class)
  public Result tvShowRequests() {
    List<TvShowRequest> pendingRequests = tvShowRequestService.getPending();
    List<TvShowRequest> persistedRequests = tvShowRequestService.getPersisted();
    List<TvShowRequest> rejectedRequests = tvShowRequestService.getRejected();
    return ok(views.html.administration.tvShowRequests.render("Trending Series Administration - Peticiones", "tvShowRequests", pendingRequests, persistedRequests, rejectedRequests, evolutionService));
  }

  @Transactional(readOnly = true)
  @Security.Authenticated(Administrator.class)
  public Result trending() {
    List<Popular> populars = popularService.getPopular(10);
    List<TvShow> topRated = tvShowService.getTopRatedTvShows(10);
    return ok(views.html.administration.popularAndRating.render("Trending Series Administration - Popular y Tendencia", "trending", populars, topRated, evolutionService));
  }

  @Transactional(readOnly = true)
  public Result loginView() {
    // llamar al método manualmente
    String email = adminAuth.getUsername(Http.Context.current());
    // si está identificado, comprobar si es admin
    if (email != null) {
      if (userService.findByEmail(email).isAdmin()) {
        // si es admin, redirigir a admin, si no, no autorizado
        return redirect(routes.AdminController.index());
      }
    }

    // si no está identificado, mostrar login
    return ok(login.render("Trending Series Administration - Login"));
  }

}
