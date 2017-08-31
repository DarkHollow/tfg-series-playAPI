package service.external;

import models.TvShow;
import models.service.external.TmdbConnection;
import models.service.external.TmdbService;
import org.dbunit.JndiDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import play.Logger;
import play.api.Play;
import play.db.Database;
import play.db.Databases;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;
import play.libs.ws.WS;
import play.libs.ws.WSClient;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static play.test.Helpers.*;

public class TmdbServiceItTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static WSClient ws;
  private static SimpleDateFormat df;
  private static TmdbService tmdbService;

  private final static int PORT = 3333;

  @Before
  public void initData() throws Exception {
    // conectamos con la base de datos de test
    db = Databases.inMemoryWith("jndiName", "DefaultDS");
    db.getConnection();
    // activamos modo MySQL
    db.withConnection(connection -> {
      connection.createStatement().execute("SET MODE MySQL;");
    });
    jpa = JPA.createFor("memoryPersistenceUnit");

    // inicializamos mocks y servicios
    ws = WS.newClient(PORT);
    df = mock(SimpleDateFormat.class);

    // inicializamos base de datos de prueba
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/tvShow_dataset.xml"));
    databaseTester.setDataSet(initialDataSet);

    // Set up - CLEAN_INSERT: primero delete all y despues insert
    databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
    // On tear down - DELETE_ALL: primero borrar contenido del dataset
    databaseTester.setTearDownOperation(DatabaseOperation.DELETE_ALL);

    databaseTester.onSetup();
  }

  @After
  public void clearData() throws Exception {
    databaseTester.onTearDown();
    jpa.shutdown();
    db.shutdown();
  }

  // testeamos buscar en TVDB por id y que encuentre
  @Test
  public void testTmdbServiceFindByTmdbIdOk() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:" + PORT);

      tmdbService = new TmdbService(ws, df, Play.current().injector().instanceOf(TmdbConnection.class));

      jpa.withTransaction(() -> {
        try {
          TvShow tvShow = tmdbService.findByTvdbId(81189);
          assertNotNull(tvShow);
          assertEquals("Breaking Bad", tvShow.name);
        } catch (Exception ex) {
          Logger.info("No se puede ejecutar el test porque TMDb no responde");
        }
      });
    });
  }

  // testeamos buscar en TVDB por id y que no encuentre
  @Test
  public void testTmdbServiceFindByTmdbIdNotOk() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:" + PORT);

      TmdbService tmdbService = new TmdbService(ws, df, Play.current().injector().instanceOf(TmdbConnection.class));

      jpa.withTransaction(() -> {
        try {
          TvShow tvShow = tmdbService.findByTvdbId(0);
          assertNull(tvShow);
        } catch (Exception ex) {
          Logger.info("No se puede ejecutar el test porque TVDB no responde");
        }
      });
    });
  }

}
