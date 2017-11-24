package utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.libs.Json;

public class Utils {

  // convierte un objeto en utils.json con una vista utils.json en concreto
  public JsonNode jsonParseObject(Object object, Class view) throws JsonProcessingException {
    if (object != null) {
      return Json.parse(new ObjectMapper()
              .writerWithView(view)
              .writeValueAsString(object));
    } else {
      return null;
    }
  }

}
