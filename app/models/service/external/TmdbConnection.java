package models.service.external;

import play.Configuration;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TmdbConnection {

  private final Configuration configuration;
  private static String apiKey;

  @Inject
  public TmdbConnection(Configuration configuration) {
    this.configuration = configuration;
    apiKey = this.configuration.getString("tmdb.apiKey");

    if (apiKey != null && !apiKey.isEmpty()) {
      Logger.info("TmdbConnection - API key obtenida");
    } else {
      Logger.warn("TmdbConnection - API key no obtenida");
    }
  }

  public String getApiKey() {
    return apiKey;
  }

}
