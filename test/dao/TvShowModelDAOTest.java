package dao;

import models.TvShow;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TvShowModelDAOTest {
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
  }

  // al final limpiamos la base de datos y la cerramos
  @AfterClass
  static public void shutdownDatabase() {
    jpa.shutdown();
    db.shutdown();
  }

  // testeamos crear un tv show
  @Test
  public void testTvShowDAOCreate() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);

    TvShow tvShow1 = new TvShow(3, "imdbId", "Stranger Things", new Date(), "Descripción",
      "banner.jpg", "poster.jpg", "fanart.jpg", "Network", "45", null, "TV-14",
      TvShow.Status.Continuing, (float)9.0, 10);

    TvShow tvShow2 = jpa.withTransaction(() -> tvShowDAO.create(tvShow1));

    //assertEquals(tvShow1.id, tvShow2.id);
    assertEquals(tvShow1.tvdbId, tvShow2.tvdbId);
    assertEquals(tvShow1.imdbId, tvShow2.imdbId);
    assertEquals(tvShow1.name, tvShow2.name);
    assertEquals(tvShow1.firstAired, tvShow2.firstAired);
    assertEquals(tvShow1.overview, tvShow2.overview);
    assertEquals(tvShow1.banner, tvShow2.banner);
    assertEquals(tvShow1.poster, tvShow2.poster);
    assertEquals(tvShow1.fanart, tvShow2.fanart);
    assertEquals(tvShow1.network, tvShow2.network);
    assertEquals(tvShow1.runtime, tvShow2.runtime);
    assertEquals(tvShow1.rating, tvShow2.rating);
    assertEquals(tvShow1.status, tvShow2.status);
    assertEquals(tvShow1.score, tvShow2.score);
    assertEquals(tvShow1.voteCount, tvShow2.voteCount);
  }

  // testeamos buscar por id -> found
  @Test
  public void testTvShowDAOFind() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    TvShow tvShow = jpa.withTransaction(() -> tvShowDAO.find(1));

    assertEquals(1, (int) tvShow.id);
    assertEquals(78804, (int) tvShow.tvdbId);
    assertEquals("Doctor Who (2005)", tvShow.name);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testTvShowDAOFindNotFound() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    TvShow tvShow = jpa.withTransaction(() -> {
      return tvShowDAO.find(0);
    });

    assertNull(tvShow);
  }

  // testeamos buscar por tvdbId
  @Test
  public void testTvShowDAOFindByTvdbId() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    TvShow tvShow = jpa.withTransaction(() -> {
      return tvShowDAO.findByTvdbId(78804);
    });

    assertEquals(1, (int) tvShow.id);
    assertEquals(78804, (int) tvShow.tvdbId);
    assertEquals("Doctor Who (2005)", tvShow.name);
  }

  // testeamos buscar por tvdbId not found
  @Test
  public void testTvShowDAOFindByTvdbIdNotFound() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    TvShow tvShow = jpa.withTransaction(() -> {
      return tvShowDAO.findByTvdbId(0);
    });

    assertNull(tvShow);
  }

  // testeamos buscar por campo coincidendo exacto
  @Test
  public void testTvShowDAOFindByExact() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    List<TvShow> tvShowsEncontrados = jpa.withTransaction(() -> {
      return tvShowDAO.findByExact("name", "who");
    });

    assertEquals(0, tvShowsEncontrados.size());

    tvShowsEncontrados = jpa.withTransaction(() -> {
      return tvShowDAO.findByExact("name", "Doctor Who (2005)");
    });

    assertEquals(1, tvShowsEncontrados.size());
    assertEquals("Doctor Who (2005)", tvShowsEncontrados.get(0).name);
  }

  // testeamos buscar por campo coincidiendo LIKE
  // por culpa de probar mediante dbunit es case sensitive aunque
  // le pongamos LOWER en el DAO, por lo que ponemos 'Who' con mayúscula
  @Test
  public void testTvShowDAOFindByLike() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    List<TvShow> tvShowsEncontrados = jpa.withTransaction(() -> {
      return tvShowDAO.findByLike("name", "Who");
    });

    assertEquals(1, tvShowsEncontrados.size());
    assertEquals("Doctor Who (2005)", tvShowsEncontrados.get(0).name);
  }

  // testeamos obtener todas los tv shows
  @Test
  public void testTvShowDAOAll() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    List<TvShow> tvShowsEncontrados = jpa.withTransaction(() -> {
      return tvShowDAO.all();
    });

    assertEquals(2, tvShowsEncontrados.size());
  }

  // NOTE: update deprecated

  // testeamos delete tv show
  @Test
  public void testTvShowDAODelete() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    TvShow tvShow = jpa.withTransaction(() -> {
      TvShow t = tvShowDAO.find(1);
      tvShowDAO.delete(t);
      return tvShowDAO.find(1);
    });

    assertNull(tvShow);
  }

  // testeamos delete tv show not found
  @Test
  public void testTvShowDAODeleteNotFound() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    jpa.withTransaction(() -> {
      TvShow tvShow = tvShowDAO.find(0);
      try {
        tvShowDAO.delete(tvShow);
      } catch (Exception e) {
        assertNull(tvShow);
      }
    });
  }

}
