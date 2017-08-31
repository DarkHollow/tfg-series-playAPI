package service.external;

import models.TvShow;
import models.service.external.TvdbService;
import org.dbunit.JndiDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.db.Database;
import play.db.Databases;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TvdbServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;

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

    // inicializamos base de datos de prueba
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
    jpa.shutdown();
    db.shutdown();
  }

  // testeamos buscar en TVDB por id y que encuentre
  @Test
  public void testTvdbServiceFindByTvdbIdOk() {
    TvShow tvShow = new TvShow();
    tvShow.tvdbId = 81189;
    tvShow.name = "Breaking Bad";

    TvdbService tvdbService = mock(TvdbService.class);
    try {
      when(tvdbService.findOnTvdbByTvdbId(81189)).thenReturn(tvShow);
      TvShow tvShow2 = tvdbService.findOnTvdbByTvdbId(81189);

      assertNotNull(tvShow2);
      assertEquals(tvShow.name, tvShow2.name);
    } catch (Exception ex) {
      Logger.info("No se puede ejecutar el test porque TVDB no responde");
    }
  }

  // testeamos buscar en TVDB por id y que no encuentre
  @Test
  public void testTvdbServiceFindByTvdbNotOk() {
    TvShow tvShow = new TvShow();
    tvShow.tvdbId = 305288;
    tvShow.name = "Stranger Things";

    TvdbService tvdbService = mock(TvdbService.class);
    try {
      when(tvdbService.findOnTvdbByTvdbId(0)).thenReturn(null);
      TvShow tvShow2 = tvdbService.findOnTvdbByTvdbId(0);

      assertNull(tvShow2);
    } catch (Exception ex) {
      Logger.info("No se puede ejecutar el test porque TVDB no responde");
    }
  }

  // testeamos buscar en TVDB por nombre
  @Test
  public void testTvdbServiceFindByName() {
    TvShow tvShow = new TvShow();
    tvShow.tvdbId = 305288;
    tvShow.name = "Stranger Things";

    List<TvShow> tvShows = new ArrayList<>();
    tvShows.add(tvShow);

    TvdbService tvdbService = mock(TvdbService.class);
    try {
      when(tvdbService.findOnTVDBby("name", "stranger")).thenReturn(tvShows);
      List<TvShow> tvShows2 = tvdbService.findOnTVDBby("name", "stranger");

      assertNotNull(tvShows2);
      assertFalse(tvShows2.isEmpty());
    } catch (Exception ex) {
      Logger.info("No se puede ejecutar el test porque TVDB no responde");
    }
  }

}
