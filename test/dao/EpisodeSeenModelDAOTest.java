package dao;

import models.Episode;
import models.EpisodeSeen;
import models.User;
import models.dao.EpisodeSeenDAO;
import models.dao.EpisodeDAO;
import models.dao.UserDAO;
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

public class EpisodeSeenModelDAOTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static EpisodeSeenDAO episodeSeenDAO;

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
    episodeSeenDAO = new EpisodeSeenDAO(jpa);
  }

  @Before
  public void initData() throws Exception {
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/episodeSeen_dataset.xml"));
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

  // testeamos crear
  @Test
  public void testEpisodeSeenDAOCreate() {
    final UserDAO userDAO = new UserDAO(jpa);
    final EpisodeDAO episodeDAO = new EpisodeDAO(jpa);

    jpa.withTransaction(() -> {
      User user = userDAO.find(1);
      Episode episode = episodeDAO.find(1);

      EpisodeSeen episodeSeen1 = new EpisodeSeen(user, episode, new Date());

      EpisodeSeen episodeSeen2 = episodeSeenDAO.create(episodeSeen1);

      //assertEquals(episodeSeen1.id, user2.id);
      assertEquals(episodeSeen1.user, episodeSeen2.user);
      assertEquals(episodeSeen1.episode, episodeSeen2.episode);
    });
  }

  // testeamos buscar por id -> found
  @Test
  public void testEpisodeSeenDAOFind() {
    EpisodeSeen episodeSeen = jpa.withTransaction(() -> episodeSeenDAO.find(1));
    assertEquals(1, (int) episodeSeen.id);
    assertEquals(1, (int) episodeSeen.user.id);
    assertEquals(1, (int) episodeSeen.episode.id);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testEpisodeSeenDAOFindNotFound() {
    EpisodeSeen episodeSeen = jpa.withTransaction(() -> episodeSeenDAO.find(0));
    assertNull(episodeSeen);
  }

  // testeamos obtener todos los episodeSeen
  @Test
  public void testEpisodeSeenDAOAll() {
    List<EpisodeSeen> episodesSeenEncontrados = jpa.withTransaction(episodeSeenDAO::all);
    assertEquals(1, episodesSeenEncontrados.size());
  }

  // testeamos delete episodeSeen OK
  @Test
  public void testEpisodeSeenDAODelete() {
    EpisodeSeen episodeSeen = jpa.withTransaction(() -> {
      EpisodeSeen s = episodeSeenDAO.find(1);
      episodeSeenDAO.delete(s);
      return episodeSeenDAO.find(1);
    });

    assertNull(episodeSeen);
  }

  // testeamos delete episodeSeen not found
  @Test
  public void testEpisodeSeenDAODeleteNotFound() {
    jpa.withTransaction(() -> {
      EpisodeSeen episodeSeen = episodeSeenDAO.find(0);
      try {
        episodeSeenDAO.delete(episodeSeen);
      } catch (Exception e) {
        assertNull(episodeSeen);
      }
    });
  }

  // testeamos delete cascade, borrando un user deberian borrarse sus episodeSeen
  @Test
  public void testEpisodeSeenDAODeleteCascadeFromUser() {
    final UserDAO userDAO = new UserDAO(jpa);

    jpa.withTransaction(() -> {
      EpisodeSeen episodeSeen = episodeSeenDAO.find(1);
      assertNotNull(episodeSeen);

      userDAO.delete(userDAO.find(1));
      episodeSeen = episodeSeenDAO.find(1);
      assertNull(episodeSeen);
    });
  }

}
