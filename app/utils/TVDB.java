package utils;

import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.inject.*;
import play.libs.ws.*;
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

    this.ws = ws;

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
    CompletionStage<JsonNode> stage = ws.url("https://api.thetvdb.com/login")
                                           .post(tvdbaccount)
                                           .thenApply(WSResponse::asJson);
    try {
      JsonNode respuesta = stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
      token = respuesta.get("token").asText();
    } catch (Exception ex) {
      System.out.println("Excepción: no se ha podido hacer log en TVDB");
      System.out.println(ex.getMessage());
    }
    if (token != null) {
      System.out.println("Token obtenido");
    }
  }

  // refresh login token
  public static void refreshToken() {
    CompletionStage<JsonNode> stage = ws.url("https://api.thetvdb.com/refresh_token")
                                        .setHeader("Authorization", "Bearer " + token)
                                        .get()
                                        .thenApply(WSResponse::asJson);
    try {
      JsonNode respuesta = stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
      token = respuesta.get("token").asText();
      System.out.println("Token actualizado");
    } catch (Exception ex) {
      System.out.println("Excepción: no se ha podido resfrescar token de TVDB");
      System.out.println(ex.getMessage());
    }
  }
}
