package service;

import models.Serie;
import models.dao.SerieDAO;
import models.service.SerieService;
import org.dbunit.JndiDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.*;
import play.db.Database;
import play.db.Databases;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SerieServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;

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


  // testeamos crear una serie
  @Test
  public void testSerieServiceCreate() {
    SerieDAO serieDAO = new SerieDAO(jpa);
    SerieService serieService = new SerieService(serieDAO);
    Serie serie1 = new Serie(3, "Stranger Things", new Date(), "Descripción",
      "banner.jpg", "poster.jpg", "fanart.jpg", "Network", 45, null, "TV-14",
      Serie.Status.Continuing, "Guionista", "Actor 1, Actor2", (float)9.0, "url_trailer");

    Serie serie2 = jpa.withTransaction(() -> {
      return serieService.create(serie1);
    });

    assertEquals(serie1, serie2);
  }

  // testeamos buscar por id -> found
  @Test
  public void testSerieServiceFind() {
    SerieDAO serieDAO = new SerieDAO(jpa);
    SerieService serieService = new SerieService(serieDAO);
    Serie serie = jpa.withTransaction(() -> {
      return serieService.find(1);
    });

    assertEquals(1, (int) serie.id);
    assertEquals(78804, (int) serie.tvdbId);
    assertEquals("Doctor Who (2005)", serie.seriesName);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testSerieServiceFindNotFound() {
    SerieDAO serieDAO = new SerieDAO(jpa);
    SerieService serieService = new SerieService(serieDAO);
    Serie serie = jpa.withTransaction(() -> {
      return serieService.find(0);
    });

    assertNull(serie);
  }

  // testeamos buscar por tvdbId
  @Test
  public void testSerieServiceFindByTvdbId() {
    SerieDAO serieDAO = new SerieDAO(jpa);
    SerieService serieService = new SerieService(serieDAO);
    Serie serie = jpa.withTransaction(() -> {
      return serieService.findByTvdbId(78804);
    });

    assertEquals(1, (int) serie.id);
    assertEquals(78804, (int) serie.tvdbId);
    assertEquals("Doctor Who (2005)", serie.seriesName);
  }

  // testeamos buscar por tvdbId not found
  @Test
  public void testSerieServiceFindByTvdbIdNotFound() {
    SerieDAO serieDAO = new SerieDAO(jpa);
    SerieService serieService = new SerieService(serieDAO);
    Serie serie = jpa.withTransaction(() -> {
      return serieService.findByTvdbId(0);
    });

    assertNull(serie);
  }

  // testeamos buscar por campo coincidendo exacto
  @Test
  public void testSerieServiceFindByExact() {
    List<Serie> seriesEncontradas = jpa.withTransaction(() -> {
      SerieDAO serieDAO = new SerieDAO(jpa);
      SerieService serieService = new SerieService(serieDAO);
      return serieService.findBy("seriesName", "who", true);
    });

    assertEquals(0, seriesEncontradas.size());

    seriesEncontradas = jpa.withTransaction(() -> {
      SerieDAO serieDAO = new SerieDAO(jpa);
      SerieService serieService = new SerieService(serieDAO);
      return serieService.findBy("seriesName", "Doctor Who (2005)", true);
    });

    assertEquals(1, seriesEncontradas.size());
    assertEquals("Doctor Who (2005)", seriesEncontradas.get(0).seriesName);
  }

  // testeamos buscar por campo coincidiendo LIKE
  @Test
  public void testSerieServiceFindByLike() {
    List<Serie> seriesEncontradas = jpa.withTransaction(() -> {
      SerieDAO serieDAO = new SerieDAO(jpa);
      SerieService serieService = new SerieService(serieDAO);
      return serieService.findBy("seriesName", "Who", false);
    });

    assertEquals(1, seriesEncontradas.size());
    assertEquals("Doctor Who (2005)", seriesEncontradas.get(0).seriesName);
  }

  // testeamos obtener todas las series
  @Test
  public void testSerieServiceAll() {
    SerieDAO serieDAO = new SerieDAO(jpa);
    SerieService serieService = new SerieService(serieDAO);
    List<Serie> seriesEncontradas = jpa.withTransaction(() -> {
      return serieService.all();
    });

    assertEquals(2, seriesEncontradas.size());
  }

  // NOTE: update deprecated

  // testeamos delete serie
  @Test
  public void testSerieServiceDelete() {
    SerieDAO serieDAO = new SerieDAO(jpa);
    SerieService serieService = new SerieService(serieDAO);
    Boolean borrado = jpa.withTransaction(() -> {
      return serieService.delete(1);
    });

    assertTrue(borrado);
  }

  // testeamos delete serie not found
  @Test
  public void testSerieServiceDeleteNotFound() {
    SerieDAO serieDAO = new SerieDAO(jpa);
    SerieService serieService = new SerieService(serieDAO);
    Boolean borrado = jpa.withTransaction(() -> {
      return serieService.delete(0);
    });

    assertFalse(borrado);
  }

}
