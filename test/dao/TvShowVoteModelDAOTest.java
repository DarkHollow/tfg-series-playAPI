package dao;

import models.TvShow;
import models.TvShowVote;
import models.User;
import models.dao.TvShowDAO;
import models.dao.TvShowVoteDAO;
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
import java.util.List;

import static org.junit.Assert.*;

public class TvShowVoteModelDAOTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static TvShowVoteDAO tvShowVoteDAO;

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
    tvShowVoteDAO = new TvShowVoteDAO(jpa);
  }

  @Before
  public void initData() throws Exception {
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/tvShowVote_dataset.xml"));
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

  // testeamos crear tvShowVote
  @Test
  public void testTvShowVoteDAOCreate() {
    final UserDAO userDAO = new UserDAO(jpa);
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);

    jpa.withTransaction(() -> {
      User user = userDAO.find(1);
      TvShow tvShow = tvShowDAO.find(1);

      TvShowVote tvShowVote1 = new TvShowVote(user, tvShow, 8.0f);

      TvShowVote tvShowVote2 = tvShowVoteDAO.create(tvShowVote1);

      //assertEquals(tvShowVote1.id, user2.id);
      assertEquals(tvShowVote1.user, tvShowVote2.user);
      assertEquals(tvShowVote1.tvShow, tvShowVote2.tvShow);
      assertEquals(tvShowVote1.score, tvShowVote2.score);

    });
  }

  // testeamos buscar por id -> found
  @Test
  public void testTvShowVoteDAOFind() {
    TvShowVote tvShowVote = jpa.withTransaction(() -> tvShowVoteDAO.find(1));
    assertEquals(1, (int) tvShowVote.id);
    assertEquals(1, (int) tvShowVote.user.id);
    assertEquals(1, (int) tvShowVote.tvShow.id);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testTvShowVoteDAOFindNotFound() {
    TvShowVote tvShowVote = jpa.withTransaction(() -> tvShowVoteDAO.find(0));
    assertNull(tvShowVote);
  }

  // testeamos obtener totdas las tvShowVotes
  @Test
  public void testTvShowVoteDAOAll() {
    List<TvShowVote> tvShowVotesEncontrados = jpa.withTransaction(tvShowVoteDAO::all);
    assertEquals(1, tvShowVotesEncontrados.size());
  }

  // testeamos delete tvShowVote OK
  @Test
  public void testTvShowVoteDAODelete() {
    TvShowVote tvShowVote = jpa.withTransaction(() -> {
      TvShowVote s = tvShowVoteDAO.find(1);
      tvShowVoteDAO.delete(s);
      return tvShowVoteDAO.find(1);
    });

    assertNull(tvShowVote);
  }

  // testeamos delete tvShowVote not found
  @Test
  public void testTvShowVoteDAODeleteNotFound() {
    jpa.withTransaction(() -> {
      TvShowVote tvShowVote = tvShowVoteDAO.find(0);
      try {
        tvShowVoteDAO.delete(tvShowVote);
      } catch (Exception e) {
        assertNull(tvShowVote);
      }
    });
  }

  // testeamos delete cascade, borrando un user deberian borrarse sus tvShowVotes
  @Test
  public void testTvShowVoteDAODeleteCascadeFromUser() {
    final UserDAO userDAO = new UserDAO(jpa);

    jpa.withTransaction(() -> {
      TvShowVote tvShowVote = tvShowVoteDAO.find(1);
      assertNotNull(tvShowVote);

      userDAO.delete(userDAO.find(1));
      tvShowVote = tvShowVoteDAO.find(1);
      assertNull(tvShowVote);
    });
  }

}
