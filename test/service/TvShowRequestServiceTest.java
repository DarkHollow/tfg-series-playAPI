package service;

import models.TvShow;
import models.TvShowRequest;
import models.dao.TvShowDAO;
import models.dao.TvShowRequestDAO;
import models.dao.UserDAO;
import models.service.TvShowRequestService;
import models.service.TvShowService;
import models.service.UserService;
import org.dbunit.JndiDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;
import play.db.Database;
import play.db.Databases;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;
import utils.Security.Password;

import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TvShowRequestServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static TvShowRequestService tvShowRequestService;

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
    TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    TvShowService tvShowService = new TvShowService(tvShowDAO);
    UserDAO userDAO = new UserDAO(jpa);
    Password sr = new Password();
    UserService userService = new UserService(userDAO, sr);
    TvShowRequestDAO tvShowRequestDAO = new TvShowRequestDAO(jpa);
    tvShowRequestService = new TvShowRequestService(tvShowService, userService, tvShowRequestDAO);

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

  // testeamos pedir un tv show que no tengamos en local (resultado OK)
  @Test
  public void testTvShowRequestRequestTvShowOk() {
    Boolean request = jpa.withTransaction(() -> tvShowRequestService.requestTvShow(296762, 1));
    assertTrue(request);
  }

  // testeamos pedir un tv show que SÍ tengamos en local (resultado not OK)
  @Test
  public void testTvShowRequestRequestTvShowInLocalNotOk() {
    Boolean request = jpa.withTransaction(() -> tvShowRequestService.requestTvShow(78804, 1));
    assertFalse(request);
  }

  // testeamos pedir una tv show que no tengamos en local con user inexistente (resultado not OK)
  @Test
  public void testTvShowRequestRequestTvShowInexistentUserNotOk() {
    Boolean request = jpa.withTransaction(() -> tvShowRequestService.requestTvShow(296762, 2));
    assertFalse(request);
  }

  // testeamos a borrar una peticion y que funcione
  @Test
  public void testTvShowRequestDeleteRequestByIdOk() {
    Boolean result = jpa.withTransaction(() -> tvShowRequestService.delete(1));
    assertTrue(result);
  }

  // testeamos a borrar una peticion y que no funcione
  @Test
  public void testTvShowRequestDeleteRequestByIdNotOk() {
    Boolean result = jpa.withTransaction(() -> tvShowRequestService.delete(2));
    assertFalse(result);
  }

  // testeamos a pedir las peticiones 'pending': de tipo requested y processing
  @Test
  public void testTvShowRequestGetPending() {
    // borramos las peticiones que ya hay y añadimos dos: una requested y otra processing
    jpa.withTransaction(() -> {
      List<TvShowRequest> requests = tvShowRequestService.all();

      // borramos
      for (TvShowRequest request : requests) {
        tvShowRequestService.delete(request.id);
      }

      // añadimos
      tvShowRequestService.requestTvShow(222222, 1);
      tvShowRequestService.requestTvShow(333333, 1);
      requests.clear();
      requests = tvShowRequestService.all();

      // cambiamos estado
      requests.get(requests.size() - 2).status = TvShowRequest.Status.Requested;
      requests.get(requests.size() - 1).status = TvShowRequest.Status.Processing;

      assertEquals( 2, requests.size());
    });
  }

}
