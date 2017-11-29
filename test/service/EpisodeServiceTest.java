package service;

import models.Episode;
import models.Season;
import models.dao.EpisodeDAO;
import models.dao.SeasonDAO;
import models.dao.TvShowDAO;
import models.service.EpisodeService;
import models.service.SeasonService;
import models.service.TvShowService;
import models.service.external.ExternalUtils;
import models.service.external.TmdbService;
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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class EpisodeServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static EpisodeService episodeService;

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

    // inicializamos seasonService y demas
    EpisodeDAO episodeDAO = new EpisodeDAO(jpa);
    SeasonDAO seasonDAO = new SeasonDAO(jpa);
    TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    TvdbService tvdbService = mock(TvdbService.class);
    TmdbService tmdbService = mock(TmdbService.class);
    TvShowService tvShowService = new TvShowService(tvShowDAO, tvdbService, tmdbService);
    ExternalUtils externalUtils = new ExternalUtils();
    SeasonService seasonService = new SeasonService(seasonDAO, tvShowService, tmdbService, externalUtils);
    episodeService = new EpisodeService(episodeDAO, seasonService, tvShowService, tmdbService, externalUtils);

    // inicializamos base de datos de prueba
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
    jpa.shutdown();
    db.shutdown();
  }

  // testeamos crear un episode
  @Test
  public void testEpisodeServiceCreate() {
    Episode episode1 = new Episode(3, new Date(), "Episodio 3", "ruta a poster 3", "screenshot 3");
    SeasonDAO seasonDAO = new SeasonDAO(jpa);
    episode1.season = jpa.withTransaction(() -> seasonDAO.find(1));

    Episode episode2 = jpa.withTransaction(() -> episodeService.create(episode1));
    assertNotNull(episode2);
  }

  // testeamos a buscar un episode -> found
  @Test
  public void testEpisodeServiceFind() {
    Episode episode = jpa.withTransaction(() -> episodeService.find(1));
    assertEquals(1, (int) episode.id);
    assertEquals("Resumen episodio 1", episode.overview);
    assertEquals("Episodio 1", episode.name);
  }

  // testeamos a buscar un episode -> not found
  @Test
  public void testEpisodeServiceFindNotFound() {
    Episode episode = jpa.withTransaction(() -> episodeService.find(0));
    assertNull(episode);
  }

  // testeamos a borrar un episode OK
  @Test
  public void testEpisodeServiceDeleteOk() {
    Boolean result = jpa.withTransaction(() -> episodeService.delete(1));
    assertTrue(result);
  }

  // testeamos a borrar un episode not OK
  @Test
  public void testEpisodeServiceDeleteNotOk() {
    Boolean result = jpa.withTransaction(() -> episodeService.delete(0));
    assertFalse(result);
  }

  // testeamos asignar temporadas
  @Test
  public void TestEpisodeServiceSetEpisodes() {
    jpa.withTransaction(() -> {
      SeasonDAO seasonDAO = new SeasonDAO(jpa);
      Season season = seasonDAO.find(1);
      Episode episode = new Episode(2, new Date(), "Episodio 3", "Resumen 3", "captura 3");
      List<Episode> episodes = new ArrayList<>();
      episodes.add(episode);
      Boolean result = episodeService.setEpisodes(season, episodes);

      assertTrue(result);
      assertEquals(3, season.episodes.size());
    });
  }

}
