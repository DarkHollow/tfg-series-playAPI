package controllers;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import play.mvc.*;
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

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class Experimentos extends Controller {

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
}
