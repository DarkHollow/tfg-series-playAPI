package dao;

import models.Serie;
import models.dao.SerieDAO;
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

public class SerieModelDAOTest {
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
  public void testSerieDAOCreate() {
    Serie serie1 = new Serie(3, "Stranger Things", new Date(), "Descripción",
      "banner.jpg", "poster.jpg", "fanart.jpg", "Network", 45, null, "TV-14",
      Serie.Status.Continuing, "Guionista", "Actor 1, Actor2", (float)9.0, "url_trailer");

    Serie serie2 = jpa.withTransaction(() -> {
      return SerieDAO.create(serie1);
    });

    //assertEquals(serie1.id, serie2.id);
    assertEquals(serie1.idTVDB, serie2.idTVDB);
    assertEquals(serie1.seriesName, serie2.seriesName);
    assertEquals(serie1.firstAired, serie2.firstAired);
    assertEquals(serie1.overview, serie2.overview);
    assertEquals(serie1.banner, serie2.banner);
    assertEquals(serie1.poster, serie2.poster);
    assertEquals(serie1.fanart, serie2.fanart);
    assertEquals(serie1.network, serie2.network);
    assertEquals(serie1.runtime, serie2.runtime);
    assertEquals(serie1.rating, serie2.rating);
    assertEquals(serie1.status, serie2.status);
    assertEquals(serie1.writer, serie2.writer);
    assertEquals(serie1.actors, serie2.actors);
    assertEquals(serie1.imdbRating, serie2.imdbRating);
    assertEquals(serie1.trailer, serie2.trailer);
  }

  // testeamos buscar por id -> found
  @Test
  public void testSerieDAOFind() {
    Serie serie = jpa.withTransaction(() -> {
      return SerieDAO.find(1);
    });

    assertEquals(1, (int) serie.id);
    assertEquals(78804, (int) serie.idTVDB);
    assertEquals("Doctor Who (2005)", serie.seriesName);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testSerieDAOFindNotFound() {
    Serie serie = jpa.withTransaction(() -> {
      return SerieDAO.find(0);
    });

    assertNull(serie);
  }

  // testeamos buscar por idTVDB
  @Test
  public void testSerieDAOFindByIdTvdb() {
    Serie serie = jpa.withTransaction(() -> {
      return SerieDAO.findByIdTvdb(78804);
    });

    assertEquals(1, (int) serie.id);
    assertEquals(78804, (int) serie.idTVDB);
    assertEquals("Doctor Who (2005)", serie.seriesName);
  }

  // testeamos buscar por idTVDB not found
  @Test
  public void testSerieDAOFindByIdTvdbNotFound() {
    Serie serie = jpa.withTransaction(() -> {
      return SerieDAO.findByIdTvdb(000);
    });

    assertNull(serie);
  }

  // testeamos buscar por campo coincidendo exacto
  @Test
  public void testSerieDAOFindByExact() {
    List<Serie> seriesEncontradas = jpa.withTransaction(() -> {
      return SerieDAO.findByExact("seriesName", "who");
    });

    assertEquals(0, seriesEncontradas.size());

    seriesEncontradas = jpa.withTransaction(() -> {
      return SerieDAO.findByExact("seriesName", "Doctor Who (2005)");
    });

    assertEquals(1, seriesEncontradas.size());
    assertEquals("Doctor Who (2005)", seriesEncontradas.get(0).seriesName);
  }

  // testeamos buscar por campo coincidiendo LIKE
  // por culpa de probar mediante dbunit es case sensitive aunque
  // le pongamos LOWER en el DAO, por lo que ponemos 'Who' con mayúscula
  @Test
  public void testSerieDAOFindByLike() {
    List<Serie> seriesEncontradas = jpa.withTransaction(() -> {
      return SerieDAO.findByLike("seriesName", "Who");
    });

    assertEquals(1, seriesEncontradas.size());
    assertEquals("Doctor Who (2005)", seriesEncontradas.get(0).seriesName);
  }

  // testeamos obtener todas las series
  @Test
  public void testSerieDAOAll() {
    List<Serie> seriesEncontradas = jpa.withTransaction(() -> {
      return SerieDAO.all();
    });

    assertEquals(2, seriesEncontradas.size());
  }

  // NOTE: update deprecated

  // testeamos delete serie
  @Test
  public void testSerieDAODelete() {
    Serie serie = jpa.withTransaction(() -> {
      Serie s = SerieDAO.find(1);
      SerieDAO.delete(s);
      return SerieDAO.find(1);
    });

    assertNull(serie);
  }

  // testeamos delete serie not found
  @Test
  public void testSerieDAODeleteNotFound() {

    jpa.withTransaction(() -> {
      Serie serie = SerieDAO.find(0);
      try {
        SerieDAO.delete(serie);
      } catch (Exception e) {
        assertNull(serie);
      }
    });
  }

}
