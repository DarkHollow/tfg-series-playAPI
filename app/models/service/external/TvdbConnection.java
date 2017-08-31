package models.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Singleton
public class TvdbConnection {

  private static String token;
  private ObjectNode tvdbaccount;
  private static WSClient ws;

  @Inject
  public TvdbConnection(WSClient ws) { // inyectamos ws para poder usarlo antes de iniciar la app
    tvdbaccount = Json.newObject();
    tvdbaccount.put("apikey", "F24AE6CBB0964290");
    tvdbaccount.put("username", "tiruri");
    tvdbaccount.put("userkey", "7EDF3A1BE153C5E9");

    TvdbConnection.ws = ws;

    token = "";

    // llamamos a hacer login
    loginTVDB();
  }

  // getter
  public String getToken() {
    return token;
  }

  // log in TvdbConnection
  private void loginTVDB() {
    Logger.info("TvdbConnection - haciendo login en The TVDB API...");
    CompletionStage<JsonNode> stage = ws.url("https://api.thetvdb.com/login")
                                           .post(tvdbaccount)
                                           .thenApply(WSResponse::asJson);
    try {
      JsonNode respuesta = stage.toCompletableFuture().get(15, TimeUnit.SECONDS);
      token = respuesta.get("token").asText();
      Logger.info("TvdbConnection - token obtenido");
    } catch (Exception ex) {
      Logger.error("TvdbConnection - Excepción: no se ha podido hacer log en TvdbConnection");
      System.out.println(ex.getMessage());
    }
  }

  // refresh login token
  public void refreshToken() {
    Logger.info("TvdbConnection - refrescando token de The TVDB API...");
    CompletionStage<JsonNode> stage = ws.url("https://api.thetvdb.com/refresh_token")
                                        .setHeader("Authorization", "Bearer " + token)
                                        .get()
                                        .thenApply(WSResponse::asJson);
    try {
      JsonNode respuesta = stage.toCompletableFuture().get(15, TimeUnit.SECONDS);
      token = respuesta.get("token").asText();
      if (token == null || token.equals("")) {
        // si no nos deja intentamos hacer login de nuevo
        loginTVDB();
      }
      if (token == null || token.equals("")) {
        Logger.info("TvdbConnection - token actualizado");
      } else {
        Logger.error("TvdbConnection - no se ha podido resfrescar token de TvdbConnection");
      }
    } catch (Exception ex) {
      Logger.error("TvdbConnection - Excepción: no se ha podido resfrescar token de TvdbConnection");
      System.out.println(ex.getMessage());
    }
  }
}
