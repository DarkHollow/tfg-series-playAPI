package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class RootController extends Controller {

  public Result index() {
    ObjectNode result = Json.newObject();
    result.put("status", "API working well");
    result.put("API_doc", "http://localhost:9000/api/doc");
    return ok(result);
  }

}
