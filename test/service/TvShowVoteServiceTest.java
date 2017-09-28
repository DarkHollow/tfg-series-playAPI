package service;

import models.TvShow;
import models.TvShowVote;
import models.User;
import models.dao.TvShowDAO;
import models.dao.TvShowVoteDAO;
import models.dao.UserDAO;
import models.service.TvShowService;
import models.service.TvShowVoteService;
import models.service.UserService;
import models.service.external.TvdbService;
import org.dbunit.JndiDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.db.Database;
import play.db.Databases;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;
import utils.Security.Password;

import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TvShowVoteServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static TvShowVoteService tvShowVoteService;

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

    // inicializamos tvShowVoteService
    TvShowVoteDAO tvShowVoteDAO = new TvShowVoteDAO(jpa);
    UserDAO userDAO = new UserDAO(jpa);
    Password password = new Password();
    UserService userService = new UserService(userDAO, password);
    TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    TvdbService tvdbService = mock(TvdbService.class);
    TvShowService tvShowService = new TvShowService(tvShowDAO, tvdbService);
    tvShowVoteService = new TvShowVoteService(tvShowVoteDAO, userService, tvShowService);

    // inicializamos base de datos de prueba
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
    jpa.shutdown();
    db.shutdown();
  }

  // testeamos crear
  @Test
  public void testTvShowVoteServiceCreate() {
    final UserDAO userDAO = new UserDAO(jpa);
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);

    jpa.withTransaction(() -> {
      User user = userDAO.find(1);
      TvShow tvShow = tvShowDAO.find(1);

      TvShowVote tvShowVote1 = new TvShowVote(user, tvShow, 8.0f);

      TvShowVote tvShowVote2 = tvShowVoteService.create(tvShowVote1);

      assertEquals(tvShowVote1.user, tvShowVote2.user);
      assertEquals(tvShowVote1.tvShow, tvShowVote2.tvShow);
      assertEquals(tvShowVote1.score, tvShowVote2.score);
    });
  }

  // testeamos buscar por id -> found
  @Test
  public void testTvShowVoteServiceFind() {
    TvShowVote tvShowVote = jpa.withTransaction(() -> tvShowVoteService.find(1));
    assertEquals(1, (int) tvShowVote.id);
    assertEquals(1, (int) tvShowVote.user.id);
    assertEquals(1, (int) tvShowVote.tvShow.id);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testTvShowVoteServiceFindNotFound() {
    TvShowVote tvShowVote = jpa.withTransaction(() -> tvShowVoteService.find(0));
    assertNull(tvShowVote);
  }

  // testeamos obtener todas las tvShowVotes
  @Test
  public void testTvShowVoteServiceAll() {
    List<TvShowVote> tvShowVotesEncontrados = jpa.withTransaction(tvShowVoteService::all);
    assertEquals(1, tvShowVotesEncontrados.size());
  }

  // testeamos delete tvShowVote OK
  @Test
  public void testTvShowVoteServiceDelete() {
    TvShowVote tvShowVote = jpa.withTransaction(() -> {
      tvShowVoteService.delete(1);
      return tvShowVoteService.find(1);
    });
    assertNull(tvShowVote);
  }

  // testeamos delete tvShowVote not found
  @Test
  public void testTvShowVoteServiceDeleteNotFound() {
    jpa.withTransaction(() -> {
      TvShowVote tvShowVote = tvShowVoteService.find(0);
      try {
        tvShowVoteService.delete(0);
      } catch (Exception e) {
        assertNull(tvShowVote);
      }
    });
  }

  // testeamos delete cascade, borrando un user deberian borrarse sus tvShowVotes
  @Test
  public void testTvShowVoteServiceDeleteCascadeFromUser() {
    final UserDAO userDAO = new UserDAO(jpa);

    jpa.withTransaction(() -> {
      TvShowVote tvShowVote = tvShowVoteService.find(1);
      assertNotNull(tvShowVote);

      userDAO.delete(userDAO.find(1));
      tvShowVote = tvShowVoteService.find(1);
      assertNull(tvShowVote);
    });
  }

  // testeamos devolver una votación según id de tvshow e id de user -> found
  @Test
  public void testTvShowVoteServiceFindByTvShowUserOk() {
    TvShowVote tvShowVote = jpa.withTransaction(() -> tvShowVoteService.findByTvShowIdUserId(1, 1));
    assertEquals(1, (int) tvShowVote.id);
    assertEquals(1, (int) tvShowVote.user.id);
    assertEquals(1, (int) tvShowVote.tvShow.id);
  }

  // testeamos devolver una votación según id de tvshow e id de user -> found
  @Test
  public void testTvShowVoteServiceFindByTvShowUserNotFound() {
    TvShowVote tvShowVote = jpa.withTransaction(() -> tvShowVoteService.findByTvShowIdUserId(2, 2));
    assertNull(tvShowVote);
  }

}
