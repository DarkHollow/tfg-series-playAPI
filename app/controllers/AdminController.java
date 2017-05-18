package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.administration.index;

public class AdminController extends Controller {

  public Result index() {
    return ok(index.render("Hola mundo"));
  }
}
