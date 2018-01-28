package dao;

import models.Episode;
import models.dao.EpisodeDAO;
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

public class EpisodeModelDAOTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static EpisodeDAO episodeDAO;
  private static SeasonDAO seasonDAO;

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
    episodeDAO = new EpisodeDAO(jpa);
    seasonDAO = new SeasonDAO(jpa);
  }

  @Before
  public void initData() throws Exception {
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/episode_dataset.xml"));
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

  // testeamos crear un episode
  @Test
  public void testEpisodeDAOCreate() {
    Episode episode1 = new Episode(3, new Date(), "Episodio 3", "Resumen episodio 3",
            "ruta captura 3");
    episode1.season = jpa.withTransaction(() -> seasonDAO.find(1));

    Episode episode2 = jpa.withTransaction(() -> episodeDAO.create(episode1));

    assertEquals(episode1.name, episode2.name);
    assertEquals(episode1.overview, episode2.overview);
    assertEquals(episode1.screenshot, episode2.screenshot);
    assertEquals(episode1.firstAired, episode2.firstAired);
    assertEquals(episode1.season.id, episode1.season.id);
  }

  // testeamos buscar un episode por id -> found
  @Test
  public void testEpisodeDAOFind() {
    Episode episode = jpa.withTransaction(() -> episodeDAO.find(1));

    assertEquals(1, (int) episode.id);
    assertEquals("Resumen episodio 1", episode.overview);
    assertEquals("Episodio 1", episode.name);
  }

  // testeamos buscar un episode por id -> not found
  @Test
  public void testEpisodeDAOFindNotFound() {
    Episode episode = jpa.withTransaction(() -> episodeDAO.find(0));

    assertNull(episode);
  }

  // testeamos delete episode OK
  @Test
  public void testEpisodeDAODelete() {
    Episode episode = jpa.withTransaction(() -> {
      Episode e = episodeDAO.find(1);
      episodeDAO.delete(e);
      return episodeDAO.find(1);
    });

    assertNull(episode);
  }

  // testeamos delete episode not found
  @Test
  public void testEpisodeDAODeleteNotFound() {
    jpa.withTransaction(() -> {
      Episode episode = episodeDAO.find(0);
      try {
        episodeDAO.delete(episode);
      } catch (Exception e) {
        assertNull(episode);
      }
    });
  }

  // testeamos delete cascade, borrando una Season deberian borrarse sus Episodes
  @Test
  public void testEpisodeDAODeleteCascadeFromSeason() {
    final SeasonDAO seasonwDAO = new SeasonDAO(jpa);

    jpa.withTransaction(() -> {
      Episode episode = episodeDAO.find(1);
      assertNotNull(episode);

      seasonwDAO.delete(seasonwDAO.find(1));
      episode = episodeDAO.find(1);
      assertNull(episode);
    });
  }

  // testeamos delete cascade, borrando una TvShow deberian borrarse sus Episodes (porque se borrar también sus
  // temporadas)
  @Test
  public void testEpisodeDAODeleteCascadeFromTvShow() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);

    jpa.withTransaction(() -> {
      Episode episode = episodeDAO.find(1);
      assertNotNull(episode);

      tvShowDAO.delete(tvShowDAO.find(1));
      episode = episodeDAO.find(1);
      assertNull(episode);
    });
  }

}
