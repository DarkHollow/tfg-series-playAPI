package dao;

import models.Serie;
import models.service.SerieService;
import play.db.Database;
import play.db.Databases;
import play.db.jpa.*;
import play.Logger;
import org.junit.*;
import static org.junit.Assert.*;
import org.dbunit.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.*;
import org.dbunit.operation.*;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

public class SerieServiceTest {
  static Database db;
  static JPAApi jpa;
  JndiDatabaseTester databaseTester;

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
    Serie serie1 = new Serie(3, "Stranger Things", new Date(), "Descripción",
      "banner.jpg", "poster.jpg", "fanart.jpg", "Network", 45, null, "TV-14",
      Serie.Status.Continuing);

    Serie serie2 = jpa.withTransaction(() -> {
      return SerieService.create(serie1);
    });

    assertEquals(serie1, serie2);
  }

  // testeamos buscar por id -> found
  @Test
  public void testSerieServiceFind() {
    Serie serie = jpa.withTransaction(() -> {
      return SerieService.find(1);
    });

    assertEquals(1, (int) serie.id);
    assertEquals(78804, (int) serie.idTVDB);
    assertEquals("Doctor Who (2005)", serie.seriesName);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testSerieServiceFindNotFound() {
    Serie serie = jpa.withTransaction(() -> {
      return SerieService.find(0);
    });

    assertNull(serie);
  }

  // testeamos buscar por campo coincidendo exacto
  @Test
  public void testSerieServiceFindByExact() {
    List<Serie> seriesEncontradas = jpa.withTransaction(() -> {
      return SerieService.findBy("seriesName", "who", true);
    });

    assertEquals(0, seriesEncontradas.size());

    seriesEncontradas = jpa.withTransaction(() -> {
      return SerieService.findBy("seriesName", "Doctor Who (2005)", true);
    });

    assertEquals(1, seriesEncontradas.size());
    assertEquals("Doctor Who (2005)", seriesEncontradas.get(0).seriesName);
  }

  // testeamos buscar por campo coincidiendo LIKE
  @Test
  public void testSerieServiceFindByLike() {
    List<Serie> seriesEncontradas = jpa.withTransaction(() -> {
      return SerieService.findBy("seriesName", "Who", false);
    });

    assertEquals(1, seriesEncontradas.size());
    assertEquals("Doctor Who (2005)", seriesEncontradas.get(0).seriesName);
  }

  // testeamos obtener todas las series
  @Test
  public void testSerieServiceAll() {
    List<Serie> seriesEncontradas = jpa.withTransaction(() -> {
      return SerieService.all();
    });

    assertEquals(2, seriesEncontradas.size());
  }

  // NOTE: update deprecated

  // testeamos delete serie
  @Test
  public void testSerieServiceDelete() {
    Boolean borrado = jpa.withTransaction(() -> {
      return SerieService.delete(1);
    });

    assertTrue(borrado);
  }

  // testeamos delete serie not found
  @Test
  public void testSerieServiceDeleteNotFound() {
    Boolean borrado = jpa.withTransaction(() -> {
      return SerieService.delete(0);
    });

    assertFalse(borrado);
  }

}
