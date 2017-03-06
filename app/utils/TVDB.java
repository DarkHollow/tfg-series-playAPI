package utils;

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
public class TVDB {

  private static String token;
  private ObjectNode tvdbaccount;
  private static WSClient ws;

  @Inject
  public TVDB(WSClient ws) { // inyectamos ws para poder usarlo antes de iniciar la app
    tvdbaccount = Json.newObject();
    tvdbaccount.put("apikey", "F24AE6CBB0964290");
    tvdbaccount.put("username", "tiruri");
    tvdbaccount.put("userkey", "7EDF3A1BE153C5E9");

    TVDB.ws = ws;

    token = "";

    // llamamos a hacer login
    loginTVDB();
  }

  // getter
  public static String getToken() {
    return token;
  }

  // log in TVDB
  private void loginTVDB() {
    Logger.info("Haciendo login en TVDB...");
    CompletionStage<JsonNode> stage = ws.url("https://api.thetvdb.com/login")
                                           .post(tvdbaccount)
                                           .thenApply(WSResponse::asJson);
    try {
      JsonNode respuesta = stage.toCompletableFuture().get(180, TimeUnit.SECONDS);
      token = respuesta.get("token").asText();
      Logger.info("Token obtenido");
    } catch (Exception ex) {
      Logger.error("Excepción: no se ha podido hacer log en TVDB");
      System.out.println(ex.getMessage());
    }
  }

  // refresh login token
  public static void refreshToken() {
    Logger.info("Refrescando token en TVDB...");
    CompletionStage<JsonNode> stage = ws.url("https://api.thetvdb.com/refresh_token")
                                        .setHeader("Authorization", "Bearer " + token)
                                        .get()
                                        .thenApply(WSResponse::asJson);
    try {
      JsonNode respuesta = stage.toCompletableFuture().get(180, TimeUnit.SECONDS);
      token = respuesta.get("token").asText();
      Logger.info("Token actualizado");
    } catch (Exception ex) {
      Logger.error("Excepción: no se ha podido resfrescar token de TVDB");
      System.out.println(ex.getMessage());
    }
  }
}
