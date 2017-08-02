package service.tvdb;

import models.service.tvdb.TvdbConnection;
import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import play.Logger;
import play.api.Play;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.*;

public class TvdbConnectionItTest {

  // probamos a comprobar que se ha logueado al inicio y se puede resfrescar
  // NOTE: hacemos ambos en uno ya que es con una fake app y se borraria el estado
  @Test
  public void testCheckTokenAndRefresh() {
    running(testServer(3333, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:3333");

      // LOGIN
      // obtenemos el token
      String token1 = Play.current().injector().instanceOf(TvdbConnection.class).getToken();

      // comprobamos que no es nulo
      assertNotNull(token1);
      Logger.debug("TVDB: login al inicio OK");

      // esperar un segundo por probable fallo
      try {
        Thread.sleep(1000);
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }

      // REFRESH
      // llamamos a refrescar
      Play.current().injector().instanceOf(TvdbConnection.class).refreshToken();
      //obtenemos el token
      String token2 = Play.current().injector().instanceOf(TvdbConnection.class).getToken();
      // comprobamos que es distinto al de login
      assertThat(token2, is(not(token1)));
      Logger.debug("TVDB: refresh de token OK");
    });
  }

}

