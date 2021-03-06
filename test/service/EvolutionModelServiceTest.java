package service;

import models.Evolution;
import models.dao.EvolutionDAO;
import models.service.EpisodeService;
import models.service.EvolutionService;
import models.service.SeasonService;
import models.service.TvShowService;
import models.service.external.TmdbService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class EvolutionModelServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static EvolutionService evolutionService;

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
    EvolutionDAO evolutionDAO = new EvolutionDAO(jpa);
    TvShowService tvShowService = mock(TvShowService.class);
    SeasonService seasonService = mock(SeasonService.class);
    EpisodeService episodeService = mock(EpisodeService.class);
    evolutionService = new EvolutionService(evolutionDAO, tvShowService, seasonService, episodeService);

    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/evolution_dataset.xml"));
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

  // testeamos crear una evolution
  @Test
  public void testEvolutionServiceCreate() {
    Evolution evolution1 = new Evolution(2, "estado 2");

    Evolution evolution2 = jpa.withTransaction(() -> evolutionService.createEvolution(evolution1));

    assertEquals(evolution1.version, evolution2.version);
    assertEquals(evolution1.state, evolution2.state);
  }

  // testeamos buscar por id -> found
  @Test
  public void testEvolutionServiceFind() {
    Evolution evolution = jpa.withTransaction(() -> evolutionService.find(1));
    assertEquals(1, (int) evolution.id);
    assertEquals(1, (int) evolution.version);
    assertEquals("estado 1", evolution.state);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testEvolutionServiceFindNotFound() {
    Evolution evolution = jpa.withTransaction(() -> evolutionService.find(0));
    assertNull(evolution);
  }

}
