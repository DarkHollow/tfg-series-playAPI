package service;

import models.Popular;
import models.TvShow;
import models.dao.PopularDAO;
import models.dao.TvShowDAO;
import models.service.PopularService;
import org.dbunit.JndiDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.*;
import play.Logger;
import play.db.Database;
import play.db.Databases;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;

import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.*;

public class PopularServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static PopularService popularService;

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
    PopularDAO popularDAO = new PopularDAO(jpa);
    popularService = new PopularService(popularDAO);
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
  public void testPopularServiceCreate() {
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);

    jpa.withTransaction(() -> {
      TvShow tvShow = tvShowDAO.find(3);
      Popular popular = new Popular();
      popular.tvShow = tvShow;
      Popular popular2 = popularService.create(popular);

      assertEquals(popular.tvShow.id, popular2.tvShow.id);
      assertEquals(popular.getPopularity(), popular2.getPopularity());

      popularService.delete(popular2.id);
    });
  }

  // testeamos obtener series populares, con popularidad 0 no debería salir ninguna
  @Test
  public void testPopularServiceGetPopularPopularity0(){
    jpa.withTransaction(() -> {
      List<Popular> populars = popularService.getPopular(5);
      assertEquals(0, populars.size());
    });
  }

  // testeamos obtener series populares, con 2 series con popularidad > 0 debería dar 2 de tamaño
  @Test
  public void testPopularServiceGetPopular(){
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    jpa.withTransaction(() -> {
      TvShow tvShow1 = tvShowDAO.find(1);
      TvShow tvShow2 = tvShowDAO.find(2);
      tvShow1.popular.updateDays();
      tvShow2.popular.updateDays();
      tvShow1.popular.requestsCount.set(0, 1000);
      tvShow2.popular.requestsCount.set(0, 2000);

      List<Popular> populars = popularService.getPopular(5);
      assertEquals(2, populars.size());

      tvShow1.popular.requestsCount.clear();
      tvShow2.popular.requestsCount.clear();
    });
  }

  // testeamos obtener top 10 comprobar ordenacion 1
  @Test
  public void testPopularServiceGetPopularOrder1(){
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    jpa.withTransaction(() -> {
      TvShow tvShow1 = tvShowDAO.find(1);
      TvShow tvShow2 = tvShowDAO.find(2);

      tvShow1.popular.updateDays();
      tvShow2.popular.updateDays();
      tvShow1.popular.requestsCount.set(0, 1000);
      tvShow1.popular.requestsCount.set(1, 2000);
      tvShow1.popular.requestsCount.set(2, 3000);

      List<Popular> populars = popularService.getPopular(5);

      assertEquals(1, (int) populars.get(0).id);
      assertEquals(6000, (int) populars.get(0).getPopularity());

      tvShow1.popular.requestsCount.clear();
      tvShow2.popular.requestsCount.clear();
    });
  }



  // testeamos obtener top 10 comprobar ordenacion 2
  @Test
  public void testPopularServiceGetTop10Order2(){
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    jpa.withTransaction(() -> {
      TvShow tvShow1 = tvShowDAO.find(1);
      TvShow tvShow2 = tvShowDAO.find(2);

      tvShow1.popular.updateDays();
      tvShow2.popular.updateDays();

      tvShow1.popular.requestsCount.set(0, 1000);
      tvShow1.popular.requestsCount.set(1, 2000);
      tvShow1.popular.requestsCount.set(2, 3000);

      tvShow2.popular.requestsCount.set(0, 1000);
      tvShow2.popular.requestsCount.set(1, 2000);
      tvShow2.popular.requestsCount.set(2, 3000);
      tvShow2.popular.requestsCount.set(3, 1000);

      List<Popular> populars = popularService.getPopular(5);

      assertEquals(2, (int) populars.get(0).id);
      assertEquals(7000, (int) populars.get(0).getPopularity());

      tvShow1.popular.requestsCount.clear();
      tvShow2.popular.requestsCount.clear();
    });
  }

  // testeamos obtener tendencia
  @Test
  public void testPopularServiceGetTrend(){
    final TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    jpa.withTransaction(() -> {
      TvShow tvShow = tvShowDAO.find(1);
      tvShow.popular.updateDays();
      tvShow.popular.requestsCount.set(0, 1000);
      tvShow.popular.requestsCount.set(1, 2000);
      tvShow.popular.requestsCount.set(2, 3000);

      assertEquals((Double) 2142.86, tvShow.popular.getTrend());

      tvShow.popular.requestsCount.clear();
    });
  }

  // testeamos borrar
  @Test
  public void testPopularServiceDelete() {
    final PopularDAO popularDAO = new PopularDAO(jpa);
    jpa.withTransaction(() -> {
      Popular popular = popularDAO.find(2);
      assertNotNull(popular);
      popularDAO.delete(popular);
      popular = popularDAO.find(2);
      assertNull(popular);
    });
  }

}
