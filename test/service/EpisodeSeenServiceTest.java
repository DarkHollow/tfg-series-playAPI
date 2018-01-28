package service;

import models.EpisodeSeen;
import models.Episode;
import models.Season;
import models.User;
import models.dao.EpisodeSeenDAO;
import models.dao.EpisodeDAO;
import models.dao.UserDAO;
import models.service.*;
import models.service.external.ExternalUtils;
import models.service.external.TmdbService;
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
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class EpisodeSeenServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static EpisodeSeenService episodeSeenService;

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

    // inicializamos episodeSeenService
    EpisodeSeenDAO episodeSeenDAO = new EpisodeSeenDAO(jpa);
    UserDAO userDAO = new UserDAO(jpa);
    Password password = new Password();
    UserService userService = new UserService(userDAO, password);

    EpisodeDAO episodeDAO = new EpisodeDAO(jpa);
    SeasonService seasonService = mock(SeasonService.class);
    TvShowService tvShowService = mock(TvShowService.class);
    TmdbService tmdbService = mock(TmdbService.class);
    ExternalUtils externalUtils = mock(ExternalUtils.class);
    EpisodeService episodeService = new EpisodeService(episodeDAO, seasonService, tvShowService, tmdbService, externalUtils);
    episodeSeenService = new EpisodeSeenService(episodeSeenDAO, userService, episodeService);

    // inicializamos base de datos de prueba
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
    jpa.shutdown();
    db.shutdown();
  }

  // testeamos crear
  @Test
  public void testEpisodeSeenServiceCreate() {
    final UserDAO userDAO = new UserDAO(jpa);
    final EpisodeDAO episodeDAO = new EpisodeDAO(jpa);

    jpa.withTransaction(() -> {
      User user = userDAO.find(1);
      Episode episode = episodeDAO.find(1);

      EpisodeSeen episodeSeen1 = new EpisodeSeen(user, episode, new Date());
      EpisodeSeen episodeSeen2 = episodeSeenService.create(episodeSeen1);

      assertEquals(episodeSeen1.user, episodeSeen2.user);
      assertEquals(episodeSeen1.episode, episodeSeen2.episode);
    });
  }

  // testeamos buscar por id -> found
  @Test
  public void testEpisodeSeenServiceFind() {
    EpisodeSeen episodeSeen = jpa.withTransaction(() -> episodeSeenService.find(1));
    assertEquals(1, (int) episodeSeen.id);
    assertEquals(1, (int) episodeSeen.user.id);
    assertEquals(1, (int) episodeSeen.episode.id);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testEpisodeSeenServiceFindNotFound() {
    EpisodeSeen episodeSeen = jpa.withTransaction(() -> episodeSeenService.find(0));
    assertNull(episodeSeen);
  }

  // testeamos obtener todas las episodeSeens
  @Test
  public void testEpisodeSeenServiceAll() {
    List<EpisodeSeen> episodeSeensEncontrados = jpa.withTransaction(episodeSeenService::all);
    assertEquals(1, episodeSeensEncontrados.size());
  }

  // testeamos delete episodeSeen OK
  @Test
  public void testEpisodeSeenServiceDelete() {
    EpisodeSeen episodeSeen = jpa.withTransaction(() -> {
      episodeSeenService.delete(1);
      return episodeSeenService.find(1);
    });
    assertNull(episodeSeen);
  }

  // testeamos delete episodeSeen not found
  @Test
  public void testEpisodeSeenServiceFindByEpisodeUserOk() {
    EpisodeSeen episodeSeen = jpa.withTransaction(() -> episodeSeenService.findByEpisodeIdUserId(1, 1));
    assertEquals(1, (int) episodeSeen.id);
    assertEquals(1, (int) episodeSeen.user.id);
    assertEquals(1, (int) episodeSeen.episode.id);
  }

  // testeamos devolver un episode seen según id de episode e id de user -> found
  @Test
  public void testEpisodeSeenServiceFindByEpisodeUserFound() {
    EpisodeSeen episodeSeen = jpa.withTransaction(() -> episodeSeenService.findByEpisodeIdUserId(1, 1));
    assertEquals((int) episodeSeen.id, 1);
  }

  // testeamos devolver un episode seen según id de episode e id de user -> not found
  @Test
  public void testEpisodeSeenServiceFindByEpisodeUserNotFound() {
    EpisodeSeen episodeSeen = jpa.withTransaction(() -> episodeSeenService.findByEpisodeIdUserId(2, 2));
    assertNull(episodeSeen);
  }

}
