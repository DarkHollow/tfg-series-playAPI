package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.administration.index;
import views.html.administration.tvShows;

public class AdminController extends Controller {

  public Result index() {
    return ok(index.render("Hola mundo", "dashboard"));
  }

  public Result tvShows() {
    return ok(tvShows.render("Hola mundo", "tvShows"));
  }
}
