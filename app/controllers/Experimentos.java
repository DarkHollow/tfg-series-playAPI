package controllers;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import play.mvc.*;
import play.libs.Json;
import play.libs.ws.*;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.data.DynamicForm;
import views.html.*;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.api.*;
import twitter4j.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.TimeUnit;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class Experimentos extends Controller {

  @Inject WSClient ws;
  @Inject FormFactory formFactory;

    /**
     * Envia un tweet de prueba

    public Result experimento1() {
        try {
          ConfigurationBuilder cb = new ConfigurationBuilder();
          cb.setDebugEnabled(true)
            .setOAuthConsumerKey("MDrQshU5SdSQqG9seeQKzQ9Br")
            .setOAuthConsumerSecret("KgFBd0ryzRMLkLqXLj8mWA0q6OReNs3MyZ6e2fiKTqjQEeUaTO")
            .setOAuthAccessToken("762416619566030848-Z4aO6jgoVx7mS477FoCwdUwkiNvCD6P")
            .setOAuthAccessTokenSecret("kEBuESMDp3cWotxdhbZlA59ZUuzUB4oyBy0YEiIxb26RI");

          TwitterFactory tf = new TwitterFactory(cb.build());
          Twitter twitter = tf.getInstance();
          twitter.updateStatus("Probando la API de Twitter desde Playframework!");

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return ok(index.render("Probando la API de Twitter"));
    }
    */

    private boolean pruebasTVDB_API() {
      boolean resul = false;

      ObjectNode tvdbaccount = Json.newObject();
      tvdbaccount.put("apikey", "F24AE6CBB0964290");
      tvdbaccount.put("username", "tiruri");
      tvdbaccount.put("userkey", "7EDF3A1BE153C5E9");

      //WSRequest request = WS.url("https://api.thetvdb.com/login");
      //WSRequest complex = request.post(tvdbaccount);
      //CompletionStage<WSResponse> response = complex.get();

      //WSResponse response = WS.url("https://api.thetvdb.com/login").post(tvdbaccount).get(10000);
/*
      String token = "";
      CompletionStage<JsonNode> response = WS.url("https://api.thetvdb.com/login").post(tvdbaccount).thenApply(WSResponse::asJson);
      System.out.println(response);
      try {
        JsonNode respuesta = response.toCompletableFuture().get(5, TimeUnit.SECONDS);
        System.out.println(respuesta);
        System.out.println(respuesta.get("token"));
        token = respuesta.get("token").toString();
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }


*/
      // buscar series 'stranger things'
/*
      try {
        CompletionStage<WSResponse> response1 = WS.url("https://api.thetvdb.com/search/series").setHeader("Authorization", "Bearer " + "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0NzU4NDQ0OTIsImlkIjoiVEZHIiwib3JpZ19pYXQiOjE0NzU3NTgwOTIsInVzZXJpZCI6NDQ5MDcwLCJ1c2VybmFtZSI6InRpcnVyaSJ9.W_V6BdqmRfSoNKAlEXmVsBLW8ZEsMSzN1OE_aa-G8I-4aEyPacBdimncxJoPb9xbymbMb83Qp-YEMVtdA6shlHnTu06sA3wI7NmzRsFsRWqJogS3Nmza58BWadtM_IuZSTAhMmdJuwnTjzUwgL2NM8mRh9pN86V1R3PIrp6yCttTppfUI6alA3peBXD2-dNACHo5MOiUOPXJ5kmuqsFgn6SZ3-Wdqn7FebUbpDgcZVplMJE-DfAZIT1TFOh7YSHVIZ3LDrksBiNkpb8xbgsm98tJsld2asp7fg5djo3gvWABzz7Mqn5PEbmxbsdR2iQWQ8sfiamIQ9WkNCW3At3XMg").setHeader("Accept-Language", "es").setQueryParameter("name", "stranger things").get();
        WSResponse respuesta1 = response1.toCompletableFuture().get(5, TimeUnit.SECONDS);
        System.out.println(respuesta1.getBody());
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }
*/
      // pedir serie en concreto 'stranger things'
      try {
        CompletionStage<WSResponse> response1 = WS.url("https://api.thetvdb.com/series/305288").setHeader("Authorization", "Bearer " + "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0NzU4NDQ0OTIsImlkIjoiVEZHIiwib3JpZ19pYXQiOjE0NzU3NTgwOTIsInVzZXJpZCI6NDQ5MDcwLCJ1c2VybmFtZSI6InRpcnVyaSJ9.W_V6BdqmRfSoNKAlEXmVsBLW8ZEsMSzN1OE_aa-G8I-4aEyPacBdimncxJoPb9xbymbMb83Qp-YEMVtdA6shlHnTu06sA3wI7NmzRsFsRWqJogS3Nmza58BWadtM_IuZSTAhMmdJuwnTjzUwgL2NM8mRh9pN86V1R3PIrp6yCttTppfUI6alA3peBXD2-dNACHo5MOiUOPXJ5kmuqsFgn6SZ3-Wdqn7FebUbpDgcZVplMJE-DfAZIT1TFOh7YSHVIZ3LDrksBiNkpb8xbgsm98tJsld2asp7fg5djo3gvWABzz7Mqn5PEbmxbsdR2iQWQ8sfiamIQ9WkNCW3At3XMg").setHeader("Accept-Language", "es").get();
        WSResponse respuesta1 = response1.toCompletableFuture().get(5, TimeUnit.SECONDS);
        System.out.println(respuesta1.getBody());
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }


      //JsonNode responseJson = response.asJson();
      //System.out.println(responseJson);

/*
      WSRequest request = ws.url("https://api.thetvdb.com/login");
      CompletionStage<WSResponse> responsePromise = request.get();
      try {
        WSResponse respuesta = responsePromise.toCompletableFuture().get(5, TimeUnit.SECONDS);
        System.out.println(respuesta.getBody());
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }
      */
      //WSResponse respuesta = responsePromise.toCompletableFuture().get(5, TimeUnit.SECONDS);



      //responsePromise.thenApply(x -> System.out.print(x.getBody()));

      return resul;
    }

    private String enviarPeticion() {
      return "hola";
    }

    public Result listaExperimentos() {
      return ok(listaExperimentos.render());
    }

    public Result experimento1() {
        // hacer cosas
        String mensaje = "No se han encontrado más tweets recientes.";
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("MDrQshU5SdSQqG9seeQKzQ9Br")
          .setOAuthConsumerSecret("KgFBd0ryzRMLkLqXLj8mWA0q6OReNs3MyZ6e2fiKTqjQEeUaTO")
          .setOAuthAccessToken("762416619566030848-Z4aO6jgoVx7mS477FoCwdUwkiNvCD6P")
          .setOAuthAccessTokenSecret("kEBuESMDp3cWotxdhbZlA59ZUuzUB4oyBy0YEiIxb26RI");

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        List<Status> tweets = new ArrayList<Status>();

        try {
          Query query = new Query("#strangerthings");
          query.setCount(100);
          query.setResultType(Query.RECENT);
          query.setLocale("es");
          query.setLang("es");

          QueryResult result;

          result = twitter.search(query);
          tweets.addAll(result.getTweets());

          if (result.hasNext()) {
            mensaje = "Se han encontrado más de 100...";
          }

          System.out.println(tweets.size());
        } catch (TwitterException te) {
          te.printStackTrace();
        }

        // devolver
        return ok(experimento.render(tweets, mensaje));
    }

    public Result experimento2() {
        // hacer cosas
        String mensaje = "";
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("MDrQshU5SdSQqG9seeQKzQ9Br")
          .setOAuthConsumerSecret("KgFBd0ryzRMLkLqXLj8mWA0q6OReNs3MyZ6e2fiKTqjQEeUaTO")
          .setOAuthAccessToken("762416619566030848-Z4aO6jgoVx7mS477FoCwdUwkiNvCD6P")
          .setOAuthAccessTokenSecret("kEBuESMDp3cWotxdhbZlA59ZUuzUB4oyBy0YEiIxb26RI");

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        List<Status> tweets = new ArrayList<Status>();

        try {
          Query query = new Query("#strangerthings");
          query.setCount(100);
          query.setResultType(Query.POPULAR);
          query.setLocale("es");
          query.setLang("es");

          QueryResult result;
          result = twitter.search(query);
          tweets.addAll(result.getTweets());

          mensaje = "Se han encontrado " + tweets.size() + " tweets populares.";

          System.out.println(tweets.size());
        } catch (TwitterException te) {
          te.printStackTrace();
        }

        // devolver
        return ok(experimento.render(tweets, mensaje));
    }

    public Result experimento3() {
        // hacer cosas
        String mensaje = "";
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("MDrQshU5SdSQqG9seeQKzQ9Br")
          .setOAuthConsumerSecret("KgFBd0ryzRMLkLqXLj8mWA0q6OReNs3MyZ6e2fiKTqjQEeUaTO")
          .setOAuthAccessToken("762416619566030848-Z4aO6jgoVx7mS477FoCwdUwkiNvCD6P")
          .setOAuthAccessTokenSecret("kEBuESMDp3cWotxdhbZlA59ZUuzUB4oyBy0YEiIxb26RI");

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        List<Status> tweets = new ArrayList<Status>();
        List<Status> tweetsFiltrados = new ArrayList<Status>();

        try {
          Query query = new Query("#strangerthings");
          query.setCount(100);
          query.setResultType(Query.RECENT);
          query.setLocale("es");
          query.setLang("es");

          QueryResult result;

          result = twitter.search(query);
          tweets.addAll(result.getTweets());

          // filtrar por fecha/hora
          // filtro: menos de una hora
          Date fechaHace1Hora = new Date(System.currentTimeMillis() - (1 * 60 * 60 * 1000));

          for(Status tweet: tweets) {
            if(fechaHace1Hora.compareTo(tweet.getCreatedAt()) < 0) {
              tweetsFiltrados.add(tweet);
            }
          }

          mensaje = "De " + tweets.size() + " tweets encontrados, " + tweetsFiltrados.size() + " son de hace menos de una hora.";

          System.out.println(tweetsFiltrados.size());
        } catch (TwitterException te) {
          te.printStackTrace();
        }

        // devolver
        return ok(experimento.render(tweetsFiltrados, mensaje));
    }

    public Result experimento4() {
        // hacer cosas
        String mensaje = "";
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("MDrQshU5SdSQqG9seeQKzQ9Br")
          .setOAuthConsumerSecret("KgFBd0ryzRMLkLqXLj8mWA0q6OReNs3MyZ6e2fiKTqjQEeUaTO")
          .setOAuthAccessToken("762416619566030848-Z4aO6jgoVx7mS477FoCwdUwkiNvCD6P")
          .setOAuthAccessTokenSecret("kEBuESMDp3cWotxdhbZlA59ZUuzUB4oyBy0YEiIxb26RI");

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        List<Status> tweets = new ArrayList<Status>();

        try {
          Query query = new Query("#strangerthings");
          query.setCount(100);
          query.setResultType(Query.POPULAR);
          query.setLocale("es");
          query.setLang("es");

          QueryResult result;
          result = twitter.search(query);
          tweets.addAll(result.getTweets());

          // puntuacion
          double puntuacion = 0;
          for(Status tweet: tweets) {
            puntuacion++;
            puntuacion += (tweet.getRetweetCount() / 2);
            puntuacion += (tweet.getFavoriteCount() / 4);
          }

          System.out.println("Puntuación de la serie: " + puntuacion);

          mensaje = String.valueOf(puntuacion) + " puntos.";

          System.out.println(tweets.size());
        } catch (TwitterException te) {
          te.printStackTrace();
        }

        // devolver
        return ok(experimento.render(tweets, mensaje));
    }

    public Result experimento5() {
        // hacer cosas
        String mensaje = "";
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("MDrQshU5SdSQqG9seeQKzQ9Br")
          .setOAuthConsumerSecret("KgFBd0ryzRMLkLqXLj8mWA0q6OReNs3MyZ6e2fiKTqjQEeUaTO")
          .setOAuthAccessToken("762416619566030848-Z4aO6jgoVx7mS477FoCwdUwkiNvCD6P")
          .setOAuthAccessTokenSecret("kEBuESMDp3cWotxdhbZlA59ZUuzUB4oyBy0YEiIxb26RI");

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        List<Status> tweets = new ArrayList<Status>();

        try {
          Query query = new Query("#strangerthings");
          query.setCount(100);
          query.setResultType(Query.RECENT);
          query.setLocale("es");
          query.setLang("es");

          QueryResult result;

          result = twitter.search(query);
          tweets.addAll(result.getTweets());

          // filtrar por fecha/hora
          // filtro: menos de una hora
          if (tweets.size() > 0) {
            Date primero = tweets.get(0).getCreatedAt();
            Date ultimo = tweets.get(tweets.size() -1).getCreatedAt();

            float diferencia = primero.getTime() - ultimo.getTime();
            float difHoras = diferencia / (60 * 60 * 1000);
            float tweetsHora = tweets.size() / difHoras;
            System.out.println("Tweets por hora: " + tweetsHora);
          }

        } catch (TwitterException te) {
          te.printStackTrace();
        }

        // devolver
        return ok(experimento.render(tweets, mensaje));
    }

    public Result formBuscaTVDB() {
      return ok(TVDBbuscarSerie.render("", null));
    }

    public Result buscaTVDB() {
      DynamicForm datos = formFactory.form().bindFromRequest();
      String nombre = datos.get("seriesName");
      System.out.println(nombre);

      // buscar en TVDB la serie

      // logueamos en TVDB api

      // creamos objeto json con datos de login
      ObjectNode tvdbaccount = Json.newObject();
      tvdbaccount.put("apikey", "F24AE6CBB0964290");
      tvdbaccount.put("username", "tiruri");
      tvdbaccount.put("userkey", "7EDF3A1BE153C5E9");

      // intentamos hacer login
      String token = "";
      JsonNode respuesta1 = null;
      CompletionStage<JsonNode> response = WS.url("https://api.thetvdb.com/login").post(tvdbaccount).thenApply(WSResponse::asJson);
      System.out.println(response);
      try {
        JsonNode respuesta = response.toCompletableFuture().get(5, TimeUnit.SECONDS);
        token = respuesta.get("token").asText();
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }

      if (token == "") {
        System.out.println("Token vacío?");
      } else {
        System.out.println("Token: " + token);

        // buscamos la serie en tvdb

        try {
          CompletionStage<JsonNode> response1 = WS.url("https://api.thetvdb.com/search/series")
                                                    .setHeader("Authorization", "Bearer " + token)
                                                    .setHeader("Accept-Language", "es")
                                                    .setQueryParameter("name", nombre)
                                                    .get()
                                                    .thenApply(WSResponse::asJson);

          respuesta1 = response1.toCompletableFuture().get(5, TimeUnit.SECONDS);

          System.out.println(respuesta1);
        } catch (Exception ex) {
          System.out.println(ex.getMessage());
        }
      }

      return ok(TVDBbuscarSerie.render(nombre, respuesta1));
    }

}
