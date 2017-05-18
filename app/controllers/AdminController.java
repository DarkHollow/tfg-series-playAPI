package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.administration.admin;

public class AdminController extends Controller {

  public Result index() {
    return ok(admin.render("hola mundo"));
  }
}
