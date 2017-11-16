package dao;

import models.Season;
import models.TvShow;
import models.dao.SeasonDAO;
import models.dao.TvShowDAO;
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

import static org.junit.Assert.*;

public class SeasonModelDAOTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static SeasonDAO seasonDAO;
  private static TvShowDAO tvShowDAO;

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
    seasonDAO = new SeasonDAO(jpa);
    tvShowDAO = new TvShowDAO(jpa);
  }

  @Before
  public void initData() throws Exception {
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/season_dataset.xml"));
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

  // testeamos crear una season
  @Test
  public void testSeasonDAOCreate() {
    Season season1 = new Season(2, new Date(), "resumen", "poster", "nombre");
    TvShow tvShow = tvShowDAO.find(1);
    season1.tvShow = tvShow;
    Season season2 = jpa.withTransaction(() -> seasonDAO.create(season1));
    tvShow.seasons.add(season2);

    assertEquals(season1.name, season2.name);
    assertEquals(season1.overview, season2.overview);
    assertEquals(season1.poster, season2.poster);
    assertEquals(season1.firstAired, season2.firstAired);
  }

  // testeamos buscar una season por id -> found
  @Test
  public void testSeasonDAOFind() {
    Season season = jpa.withTransaction(() -> seasonDAO.find(1));

    assertEquals(1, (int) season.id);
    assertEquals("resumen temporada", season.overview);
    assertEquals("Temporada 1", season.name);
  }

  // testeamos buscar una season por id -> not found
  @Test
  public void testSeasonDAOFindNotFound() {
    Season season = jpa.withTransaction(() -> seasonDAO.find(0));

    assertNull(season);
  }

  // testeamos delete season OK
  @Test
  public void testSeasonDAODelete() {
    Season season = jpa.withTransaction(() -> {
      Season s = seasonDAO.find(1);
      seasonDAO.delete(s);
      return seasonDAO.find(1);
    });

    assertNull(season);
  }

  // testeamos delete season not found
  @Test
  public void testSeasonDAODeleteNotFound() {
    jpa.withTransaction(() -> {
      Season season = seasonDAO.find(0);
      try {
        seasonDAO.delete(season);
      } catch (Exception e) {
        assertNull(season);
      }
    });
  }

  // testeamos delete cascade, borrando una TvShow deberian borrarse sus Season
  @Test
  public void testSeasonDAODeleteCascadeFromUser() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);

    jpa.withTransaction(() -> {
      Season season = seasonDAO.find(1);
      assertNotNull(season);

      tvShowDAO.delete(tvShowDAO.find(1));
      season = seasonDAO.find(1);
      assertNull(season);
    });
  }

}
