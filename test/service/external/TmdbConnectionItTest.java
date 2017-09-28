package service.external;

import models.service.external.TmdbConnection;
import models.service.external.TvdbConnection;
import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import play.Logger;
import play.api.Play;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.*;

public class TmdbConnectionItTest {

  // probamos a comprobar que se ha logueado al inicio y se puede resfrescar
  // NOTE: hacemos ambos en uno ya que es con una fake app y se borraria el estado
  @Test
  public void testCheckApiKey() {
    running(testServer(3333, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:3333");

      String apiKey = Play.current().injector().instanceOf(TmdbConnection.class).getApiKey();

      // comprobamos que no es nulo
      assertNotNull(apiKey);
      Logger.info("The Movie Database API - api key encontrada");
    });
  }

}

