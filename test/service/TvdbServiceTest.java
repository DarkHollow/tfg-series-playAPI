package service;

import models.Serie;
import models.dao.SerieDAO;
import models.service.SerieService;
import models.service.TvdbService;
import org.dbunit.JndiDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.*;
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

public class TvdbServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static TvdbService tvdbService;

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

    // inicializamos tvdbService
    WSClient ws = WS.newClient(PORT);
    SimpleDateFormat df = mock(SimpleDateFormat.class);
    SerieDAO serieDAO = new SerieDAO(jpa);
    SerieService serieService = new SerieService(serieDAO);
    tvdbService = new TvdbService(ws, df, serieService);

    // inicializamos base de datos de prueba
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/series_dataset.xml"));
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

  // testeamos buscar en TVDB por id y que esté en local (fakeapp para obtener login tvdb)
  @Test
  public void testTvdbServiceFindByTvdbIdIsInLocal() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
      browser.goTo("http://localhost:" + PORT);

      Serie serie = jpa.withTransaction(() -> tvdbService.findOnTvdbByTvdbId(81189));

      assertNotNull(serie);
      assertEquals("Breaking Bad", serie.seriesName);
      assertTrue(serie.local);
    });
  }

  // testeamos buscar en TVDB por id y que no esté en local (fakeapp para obtener login tvdb)
  @Test
  public void testTvdbServiceFindByTvdbIdIsNotInLocal() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
      browser.goTo("http://localhost:" + PORT);

      Serie serie = jpa.withTransaction(() -> tvdbService.findOnTvdbByTvdbId(305288));

      assertNotNull(serie);
      assertEquals("Stranger Things", serie.seriesName);
      assertFalse(serie.local);
    });
  }

  // testeamos buscar en TVDB por nombre (fakeapp para obtener login tvdb)
  @Test
  public void testTvdbServiceFindByName() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
      browser.goTo("http://localhost:" + PORT);

      List<Serie> series = jpa.withTransaction(() -> tvdbService.findOnTVDBby("name", "stranger"));

      assertNotNull(series);
      assertFalse(series.isEmpty());
    });
  }

}
