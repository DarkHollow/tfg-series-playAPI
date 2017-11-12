package models.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonUtils {

  // parsear fecha
  public Date parseDate(JsonNode jsonDate) {
    int i = 0, tries = 100;
    Date result = null;
    SimpleDateFormat df = new SimpleDateFormat();

    if (!jsonDate.isNull() && !jsonDate.asText().equals("")) {

      while (i < tries) {
        try {
          df.applyPattern("yyyy-MM-dd");
          result = df.parse(jsonDate.asText()); // fecha estreno
          break;
        } catch (Exception e) {
          i++;
          if (i != 100) {
            Logger.info("Reintentando parsear fecha de estreno");
          } else {
            Logger.error("No se ha podido parsear la fecha");
          }
        }
      }
    }

    return result;
  }
}
