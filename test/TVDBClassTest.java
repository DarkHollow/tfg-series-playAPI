import play.Logger;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static play.test.Helpers.*;
import static org.fluentlenium.core.filter.FilterConstructor.*;

import utils.TVDB;

public class TVDBClassTest {

  // probamos a comprobar que se ha logueado al inicio y se puede resfrescar
  // NOTE: hacemos ambos en uno ya que es con una fake app y se borraria el estado
  @Test
  public void testCheckTokenAndRefresh() {
    running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
      browser.goTo("http://localhost:3333");

      // LOGIN
      // obtenemos el token
      String token1 = TVDB.getToken();
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
      TVDB.refreshToken();
      //obtenemos el token
      String token2 = TVDB.getToken();
      // comprobamos que es distinto al de login
      assertThat(token2, is(not(token1)));
      Logger.debug("TVDB: refresh de token OK");
    });
  }

}
