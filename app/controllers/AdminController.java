package controllers;

import com.google.inject.Inject;
import models.TvShow;
import models.TvShowRequest;
import models.service.TvShowRequestService;
import models.service.TvShowService;
import models.service.UserService;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Administrator;
import views.html.administration.index;
import views.html.administration.login;

import java.util.List;

public class AdminController extends Controller {

  private final TvShowService tvShowService;
  private final TvShowRequestService tvShowRequestService;
  private final UserService userService;
  private final utils.Security.Administrator adminAuth;

  @Inject
  public AdminController(TvShowService tvShowService, TvShowRequestService tvShowRequestService, UserService userService, utils.Security.Administrator adminAuth) {
    this.tvShowService = tvShowService;
    this.tvShowRequestService = tvShowRequestService;
    this.userService = userService;
    this.adminAuth = adminAuth;
  }

  @Transactional(readOnly = true)
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
    return ok(index.render("Trending Series Administration - Dashboard", "dashboard", tvShowCount, deletedTvShowCount, requestsCount, pendingRequestsCount, persistedRequestsCount, rejectedRequestsCount));
  }

  @Transactional(readOnly = true)
  @Security.Authenticated(Administrator.class)
  public Result tvShows() {
    List<TvShow> tvShows = tvShowService.all();
    return ok(views.html.administration.tvShows.render("Trending Series Administration - Series", "tvShows", tvShows));
  }

  @Transactional(readOnly = true)
  @Security.Authenticated(Administrator.class)
  public Result tvShow(Integer tvShowId) {
    TvShow tvShow = tvShowService.find(tvShowId);
    String title = "Trending Series Administration - Serie";
    if (tvShow != null) {
      title = title + " " + tvShow.name;
    }
    return ok(views.html.administration.tvShow.render(title, "tvShow", tvShow));
  }

  @Transactional(readOnly = true)
  @Security.Authenticated(Administrator.class)
  public Result tvShowRequests() {
    List<TvShowRequest> pendingRequests = tvShowRequestService.getPending();
    List<TvShowRequest> persistedRequests = tvShowRequestService.getPersisted();
    List<TvShowRequest> rejectedRequests = tvShowRequestService.getRejected();
    return ok(views.html.administration.tvShowRequests.render("Trending Series Administration - Peticiones", "tvShowRequests", pendingRequests, persistedRequests, rejectedRequests));
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
      } else {
        // si no es admin, no autorizado
        return unauthorized();
      }
    }

    // si no está identificado, mostrar login
    return ok(login.render("Trending Series Administration - Login"));
  }

}
