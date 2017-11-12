package service.external;

import models.TvShow;
import models.service.external.JsonUtils;
import models.service.external.TvdbConnection;
import models.service.external.TvdbService;
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
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static play.test.Helpers.*;

public class TvdbServiceItTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static WSClient ws;
  private static JsonUtils jsonUtils;

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
    jsonUtils = mock(JsonUtils.class);

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
  public void testTvdbServiceFindByTvdbIdOk() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:" + PORT);

      TvdbService tvdbService = new TvdbService(ws, jsonUtils, Play.current().injector().instanceOf(TvdbConnection.class));

      jpa.withTransaction(() -> {
        try {
          TvShow tvShow = tvdbService.findOnTvdbByTvdbId(81189);
          assertNotNull(tvShow);
          assertEquals("Breaking Bad", tvShow.name);
        } catch (Exception ex) {
          Logger.info("No se puede ejecutar el test porque TVDB no responde");
        }
      });
    });
  }

  // testeamos buscar en TVDB por id y que no encuentre
  @Test
  public void testTvdbServiceFindByTvdbIdNotOk() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:" + PORT);

      TvdbService tvdbService = new TvdbService(ws, jsonUtils, Play.current().injector().instanceOf(TvdbConnection.class));

      jpa.withTransaction(() -> {
        try {
          TvShow tvShow = tvdbService.findOnTvdbByTvdbId(0);
          assertNull(tvShow);
        } catch (Exception ex) {
          Logger.info("No se puede ejecutar el test porque TVDB no responde");
        }
      });
    });
  }

  // testeamos buscar en TVDB por nombre
  @Test
  public void testTvdbServiceFindByName() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:" + PORT);

      TvdbService tvdbService = new TvdbService(ws, jsonUtils, Play.current().injector().instanceOf(TvdbConnection.class));

      jpa.withTransaction(() -> {
        try {
          List<TvShow> tvShows = tvdbService.findOnTVDBby("name", "stranger");
          assertNotNull(tvShows);
          assertFalse(tvShows.isEmpty());
        } catch (Exception ex) {
          Logger.info("No se puede ejecutar el test porque TVDB no responde");
        }
      });
    });
  }

}
