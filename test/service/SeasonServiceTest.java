package service;

import models.Season;
import models.TvShow;
import models.dao.SeasonDAO;
import models.dao.TvShowDAO;
import models.service.SeasonService;
import models.service.TvShowService;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SeasonServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static SeasonService seasonService;
  private static TvShowDAO tvShowDAO;

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
    SeasonDAO seasonDAO = new SeasonDAO(jpa);
    tvShowDAO = new TvShowDAO(jpa);
    TvdbService tvdbService = mock(TvdbService.class);
    TvShowService tvShowService = new TvShowService(tvShowDAO, tvdbService);
    seasonService = new SeasonService(seasonDAO, tvShowService);

    // inicializamos base de datos de prueba
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
    jpa.shutdown();
    db.shutdown();
  }

  // testeamos crear una season
  @Test
  public void testSeasonCreate() {
    Season season1 = new Season(2, new Date(), "resumen", "poster", "nombre");

    TvShow tvShow = jpa.withTransaction(() -> tvShowDAO.find(1));
    season1.tvShow = tvShow;
    Season season2 = jpa.withTransaction(() -> seasonService.create(season1));
    tvShow.seasons = new ArrayList<>();
    tvShow.seasons.add(season2);

    assertNotNull(season2);
  }

  // testeamos a buscar una season -> found
  @Test
  public void testSeasonFind() {
    Season season = jpa.withTransaction(() -> seasonService.find(1));
    assertEquals(1, (int) season.id);
    assertEquals("resumen temporada", season.overview);
    assertEquals("Temporada 1", season.name);
  }

  // testeamos a buscar una season -> not found
  @Test
  public void testSeasonFindNotFound() {
    Season season = jpa.withTransaction(() -> seasonService.find(0));
    assertNull(season);
  }

  // testeamos a borrar una season OK
  @Test
  public void testSeasonDeleteOk() {
    Boolean result = jpa.withTransaction(() -> seasonService.delete(1));
    assertTrue(result);
  }

  // testeamos a borrar una season not OK
  @Test
  public void testSeasonDeleteNotOk() {
    Boolean result = jpa.withTransaction(() -> seasonService.delete(0));
    assertFalse(result);
  }

}
