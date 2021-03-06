package service.external;

import com.fasterxml.jackson.databind.JsonNode;
import models.Episode;
import models.Season;
import models.TvShow;
import models.service.external.ExternalUtils;
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
import java.util.List;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class TmdbServiceItTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static WSClient ws;
  private static ExternalUtils externalUtils;
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
    externalUtils = new ExternalUtils();

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

  // testeamos buscar en TMDb por tvdbId y que encuentre
  @Test
  public void testTmdbServiceFindByTmdbIdOk() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:" + PORT);

      tmdbService = new TmdbService(ws, externalUtils, Play.current().injector().instanceOf(TmdbConnection.class));

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

  // testeamos buscar en TMDb por tvdbId y que no encuentre
  @Test
  public void testTmdbServiceFindByTmdbIdNotOk() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:" + PORT);

      TmdbService tmdbService = new TmdbService(ws, externalUtils, Play.current().injector().instanceOf(TmdbConnection.class));

      jpa.withTransaction(() -> {
        try {
          TvShow tvShow = tmdbService.findByTvdbId(-1);
          assertNull(tvShow);
        } catch (Exception ex) {
          Logger.info("No se puede ejecutar el test porque TVDB no responde");
        }
      });
    });
  }

  @Test
  public void testTmdbServiceGetJsonTvShowByTmdbId() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:" + PORT);

      tmdbService = new TmdbService(ws, externalUtils, Play.current().injector().instanceOf(TmdbConnection.class));

      jpa.withTransaction(() -> {
        try {
          JsonNode tvShow = tmdbService.getJsonTvShowByTmdbId(1396);
          assertNotNull(tvShow);
          assertEquals("Breaking Bad", tvShow.get("name").asText());
        } catch (Exception ex) {
          Logger.info("No se puede ejecutar el test porque TMDb no responde");
        }
      });
    });
  }

  @Test
  public void testTmdbServiceGetCompleteSeasonByTmdbIdAndSeasonNumber() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:" + PORT);

      tmdbService = new TmdbService(ws, externalUtils, Play.current().injector().instanceOf(TmdbConnection.class));

      jpa.withTransaction(() -> {
        try {
          Season season = tmdbService.getCompleteSeasonByTmdbIdAndSeasonNumber(1396, 1);
          assertNotNull(season);
          assertEquals(1, (int) season.seasonNumber);
        } catch (Exception ex) {
          Logger.info("No se puede ejecutar el test porque TMDb no responde");
        }
      });
    });
  }

  @Test
  public void testTmdbServiceGetAllSeasonEpisodesByTmdbIdAndSeasonNumber() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), browser -> {
      browser.goTo("http://localhost:" + PORT);

      tmdbService = new TmdbService(ws, externalUtils, Play.current().injector().instanceOf(TmdbConnection.class));

      jpa.withTransaction(() -> {
        try {
          List<Episode> episodes = tmdbService.getAllSeasonEpisodesByTmdbIdAndSeasonNumber(1396, 1);
          assertNotNull(episodes);
          assertEquals(7, episodes.size());
        } catch (Exception ex) {
          Logger.info("No se puede ejecutar el test porque TMDb no responde");
        }
      });
    });
  }

}
