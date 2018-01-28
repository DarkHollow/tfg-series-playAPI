package models.service.external;

import play.Logger;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TwitterService {
  private final twitter4j.Twitter twitter;

  @Inject
  public TwitterService() {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true)
            .setOAuthConsumerKey("MDrQshU5SdSQqG9seeQKzQ9Br")
            .setOAuthConsumerSecret("KgFBd0ryzRMLkLqXLj8mWA0q6OReNs3MyZ6e2fiKTqjQEeUaTO")
            .setOAuthAccessToken("762416619566030848-Z4aO6jgoVx7mS477FoCwdUwkiNvCD6P")
            .setOAuthAccessTokenSecret("kEBuESMDp3cWotxdhbZlA59ZUuzUB4oyBy0YEiIxb26RI");
    TwitterFactory tf = new TwitterFactory(cb.build());
    twitter = tf.getInstance();
  }

  public Double getRatio(String hashtag) {
    try {
      Query query = new Query("#" + hashtag);
      query.setCount(100);
      query.setResultType(Query.RECENT);
      query.setLocale("es");
      query.setLang("es");

      QueryResult result;

      result = twitter.search(query);
      List<Status> tweets = new ArrayList<>(result.getTweets());

      // filtrar por fecha/hora
      // filtro: menos de una hora
      if (tweets.size() > 0) {
        Date primero = tweets.get(0).getCreatedAt();
        Date ultimo = tweets.get(tweets.size() -1).getCreatedAt();

        Double diferencia = new Long(primero.getTime() - ultimo.getTime()).doubleValue();
        Double difHoras = diferencia / (60 * 60 * 1000);

        // guardar en fichero
        /*
        try (PrintStream ps = new PrintStream(new FileOutputStream("datos.txt", true))) {
          ps.println(hashtag + " " + tweetsHora);
        } catch (Exception ex) {
          System.out.println(ex.getMessage());
        }
        */
        return tweets.size() / difHoras;
      }

    } catch (TwitterException te) {
      te.printStackTrace();
    }
    return -1D;
  }

}
