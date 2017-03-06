package service;

import models.dao.SerieDAO;
import models.dao.TvShowRequestDAO;
import models.dao.UsuarioDAO;
import models.service.SerieService;
import models.service.TvShowRequestService;
import models.service.UsuarioService;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

public class TvShowRequestServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static TvShowRequestService tvShowRequestService;

  private final static int PORT = 3333;

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

    // inicializamos tvShowRequestService
    SerieDAO serieDAO = new SerieDAO(jpa);
    SerieService serieService = new SerieService(serieDAO);
    UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);
    UsuarioService usuarioService = new UsuarioService(usuarioDAO);
    TvShowRequestDAO tvShowRequestDAO = new TvShowRequestDAO(jpa);
    tvShowRequestService = new TvShowRequestService(serieService, usuarioService, tvShowRequestDAO);

    // inicializamos base de datos de prueba
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
            FileInputStream("test/resources/tvShowRequest_dataset.xml"));
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

  // testeamos pedir una serie que no tengamos en local (resultado OK)
  @Test
  public void testTvShowRequestRequestTvShowOk() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
      browser.goTo("http://localhost:" + PORT);

      Boolean request = jpa.withTransaction(() -> tvShowRequestService.requestTvShow(296762, 1));

      assertTrue(request);
    });
  }

  // testeamos pedir una serie que SÍ tengamos en local (resultado not OK)
  @Test
  public void testTvShowRequestRequestTvShowInLocalNotOk() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
      browser.goTo("http://localhost:" + PORT);

      Boolean request = jpa.withTransaction(() -> tvShowRequestService.requestTvShow(78804, 1));

      assertFalse(request);
    });
  }

  // testeamos pedir una serie que no tengamos en local con usuario inexistente (resultado not OK)
  @Test
  public void testTvShowRequestRequestTvShowInexistentUserNotOk() {
    running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
      browser.goTo("http://localhost:" + PORT);

      Boolean request = jpa.withTransaction(() -> tvShowRequestService.requestTvShow(296762, 2));

      assertFalse(request);
    });
  }

}
