package controllers;

import com.google.inject.Inject;
import models.TvShowRequest;
import models.service.TvShowRequestService;
import models.service.TvShowService;
import models.service.UserService;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Security.Auth;
import views.html.administration.index;
import views.html.administration.login;

import java.util.List;

public class AdminController extends Controller {

  private final TvShowService tvShowService;
  private final TvShowRequestService tvShowRequestService;
  private final UserService userService;
  private final Auth auth;

  @Inject
  public AdminController(TvShowService tvShowService, TvShowRequestService tvShowRequestService, UserService userService, Auth auth) {
    this.tvShowService = tvShowService;
    this.tvShowRequestService = tvShowRequestService;
    this.userService = userService;
    this.auth = auth;
  }

  @Transactional(readOnly = true)
  @Security.Authenticated(Auth.class)
  public Result index() {
    // sumario datos

    // series en nuestra base de datos
    Integer tvShowCount = tvShowService.all().size();
    // peticiones nuevas series
    Integer tvShowRequestCount = tvShowRequestService.all().size();

    return ok(index.render("Trending Series Administration - Dashboard", "dashboard", tvShowCount, tvShowRequestCount));
  }

  @Transactional(readOnly = true)
  @Security.Authenticated(Auth.class)
  public Result tvShows() {
    List<TvShowRequest> tvShowRequests = tvShowRequestService.all();
    return ok(views.html.administration.tvShows.render("Trending Series Administration - Series", "tvShows", tvShowRequests));
  }

  public Result loginView() {
    // llamar al método manualmente
    String email = auth.getUsername(Http.Context.current());
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
