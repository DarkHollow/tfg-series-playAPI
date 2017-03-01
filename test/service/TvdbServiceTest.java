package service;

import models.Serie;
import models.dao.RequestedSeriesDAO;
import models.dao.SerieDAO;
import models.dao.UsuarioDAO;
import models.service.SerieService;
import models.service.TvdbService;
import models.service.UsuarioService;
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

  private final static int PORT = 3333;

  private static WSClient ws;
  private static SimpleDateFormat df;
  private static SerieDAO serieDAO;
  private static SerieService serieService;
  private static UsuarioDAO usuarioDAO;
  private static UsuarioService usuarioService;
  private static RequestedSeriesDAO rqDAO;
  private static TvdbService tvdbService;

  @BeforeClass
  static public void initDatabase() {
    // conectamos con la base de datos de test
    db = Databases.inMemoryWith("jndiName", "DefaultDS");
    db.getConnection();
    // activamos modo MySQL
    db.withConnection(connection -> {
      connection.createStatement().execute("SET MODE MySQL;");
    });
    jpa = JPA.createFor("memoryPersistenceUnit");

    ws = WS.newClient(PORT);
    df = mock(SimpleDateFormat.class);
    serieDAO = new SerieDAO(jpa);
    serieService = new SerieService(serieDAO);
    usuarioDAO = new UsuarioDAO(jpa);
    usuarioService = new UsuarioService(usuarioDAO);
    rqDAO = new RequestedSeriesDAO(jpa);
    tvdbService = new TvdbService(ws, df, serieService, usuarioService, rqDAO);
  }

  @Before
  public void initData() throws Exception {
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
  }

  // al final limpiamos la base de datos y la cerramos
  @AfterClass
  static public void shutdownDatabase() {
    jpa.shutdown();
    db.shutdown();
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

  // testeamos buscar una serie que exista en TVDB y no tengamos en local
  @Test
  public void TestTvdbServiceFindOnTVDBAndNotOnLocalbyTvdbIdOk() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
      browser.goTo("http://localhost:" + PORT);

      Boolean result = jpa.withTransaction(() -> tvdbService.findOnTVDBAndNotOnLocalbyTvdbId(305288));

      assertTrue(result);
    });
  }

  // testeamos buscar una serie que exista en TVDB y tengamos en local
  @Test
  public void TestTvdbServiceFindOnTVDBAndNotOnLocalbyTvdbIdLocal() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
      browser.goTo("http://localhost:" + PORT);

      Boolean result = jpa.withTransaction(() -> tvdbService.findOnTVDBAndNotOnLocalbyTvdbId(78804));

      assertFalse(result);
    });
  }

}
