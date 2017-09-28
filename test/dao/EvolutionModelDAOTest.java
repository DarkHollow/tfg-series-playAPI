package dao;

import models.Evolution;
import models.dao.EvolutionDAO;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EvolutionModelDAOTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static EvolutionDAO evolutionDAO;

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
    evolutionDAO = new EvolutionDAO(jpa);

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
  public void testEvolutionDAOCreate() {
    Evolution evolution1 = new Evolution(2, "estado 2");

    Evolution evolution2 = jpa.withTransaction(() -> evolutionDAO.create(evolution1));

    assertEquals(evolution1.version, evolution2.version);
    assertEquals(evolution1.state, evolution2.state);
  }

  // testeamos buscar por id -> found
  @Test
  public void testEvolutionDAOFind() {
    Evolution evolution = jpa.withTransaction(() -> evolutionDAO.find(1));

    assertEquals(1, (int) evolution.id);
    assertEquals(1, (int) evolution.version);
    assertEquals("estado 1", evolution.state);

  }

  // testeamos buscar por id -> not found
  @Test
  public void testEvolutionDAOFindNotFound() {
    Evolution evolution = jpa.withTransaction(() -> evolutionDAO.find(0));
    assertNull(evolution);
  }

  // testeamos obtener todas las evolutions
  @Test
  public void testEvolutionDAOAll() {
    List<Evolution> evolutionsEncontradas = jpa.withTransaction(evolutionDAO::all);
    assertEquals(1, evolutionsEncontradas.size());
  }

  // testeamos delete evolution
  @Test
  public void testEvolutionDAODelete() {
    Evolution evolution = jpa.withTransaction(() -> {
      Evolution s = evolutionDAO.find(1);
      evolutionDAO.delete(s);
      return evolutionDAO.find(1);
    });

    assertNull(evolution);
  }

  // testeamos delete evolution not found
  @Test
  public void testEvolutionDAODeleteNotFound() {
    jpa.withTransaction(() -> {
      Evolution evolution = evolutionDAO.find(0);
      try {
        evolutionDAO.delete(evolution);
      } catch (Exception e) {
        assertNull(evolution);
      }
    });
  }

}
