package controllers;

import play.mvc.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RootController extends Controller {

  public Result index() {
    ObjectNode result = Json.newObject();
    result.put("status", "API working well");
    result.put("API_doc", "http://localhost:9000/api/doc");
    return ok(result);
  }

}
