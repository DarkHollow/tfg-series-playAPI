package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.api.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class RootController extends Controller {

  public Result index() {
    ObjectNode result = Json.newObject();
    result.put("status", "API working well");
    result.put("API_doc", "http://" + request().host() + "/api/docs");
    return ok(result);
  }

}
