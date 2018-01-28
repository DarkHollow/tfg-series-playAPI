package dao;

import models.Popular;
import models.TvShow;
import models.dao.PopularDAO;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PopularModelDAOTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static PopularDAO popularDAO;

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
    popularDAO = new PopularDAO(jpa);
  }

  @Before
  public void initData() throws Exception {
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/popular_dataset.xml"));
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

  // testeamos crear Popular
  @Test
  public void testPopularDAOCreate() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);

    jpa.withTransaction(() -> {
      TvShow tvShow = tvShowDAO.find(3);
      Popular popular = new Popular();
      popular.tvShow = tvShow;
      Popular popular2 = popularDAO.create(popular);

      assertEquals(popular.tvShow.id, popular2.tvShow.id);
      assertEquals(popular.getPopularity(), popular2.getPopularity());

      popularDAO.delete(popular2);
    });
  }

  // testeamos find found
  @Test
  public void testPopularDAOFind() {
    Popular popular = jpa.withTransaction(() -> popularDAO.find(1));
    assertEquals(1, (int) popular.id);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testTvShowDAOFindNotFound() {
    Popular popular = jpa.withTransaction(() -> popularDAO.find(0));
    assertNull(popular);
  }

  // testeamos obtener todos
  @Test
  public void testPopularDAOAll() {
    jpa.withTransaction(() -> {
      List<Popular> populars = popularDAO.all();
      assertEquals(2, populars.size());
    });
  }

  @Test
  public void testPopularDAODelete() {
    jpa.withTransaction(() -> {
      Popular popular = popularDAO.find(2);
      popularDAO.delete(popular);
      popular = popularDAO.find(2);
      assertNull(popular);
    });
  }

  // testeamos delete tvShowVote not found
  @Test
  public void testPopularDAODeleteNotFound() {
    jpa.withTransaction(() -> {
      Popular popular = popularDAO.find(3);
      try {
        popularDAO.delete(popular);
      } catch (Exception e) {
        assertNull(popular);
      }
    });
  }

  // testeamos delete cascade, borrando una serie deberia borrarse su popular
  @Test
  public void testPopularDAODeleteCascadeFromTvShow() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);

    jpa.withTransaction(() -> {
      Popular popular = popularDAO.find(1);
      assertNotNull(popular);

      tvShowDAO.delete(tvShowDAO.find(1));
      popular = popularDAO.find(1);
      assertNull(popular);
    });
  }

}
