package service;

import models.TvShowRequest;
import models.User;
import models.dao.TvShowDAO;
import models.dao.TvShowRequestDAO;
import models.service.TvShowRequestService;
import models.service.TvShowService;
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
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TvShowRequestServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static TvShowRequestService tvShowRequestService;
  private static User user1;

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

    // inicializamos tvShowRequestService y demas
    user1 = new User();
    user1.id = 1;
    TvShowDAO tvShowDAO = new TvShowDAO(jpa);
    TvdbService tvdbService = mock(TvdbService.class);
    TmdbService tmdbService = mock(TmdbService.class);
    TvShowService tvShowService = new TvShowService(tvShowDAO, tvdbService, tmdbService);
    TvShowRequestDAO tvShowRequestDAO = new TvShowRequestDAO(jpa);
    tvShowRequestService = new TvShowRequestService(tvShowService, tvShowRequestDAO);

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
  public void testTvShowRequestCreateOk() {
    TvShowRequest request = new TvShowRequest(296762, user1);
    TvShowRequest createdRequest = jpa.withTransaction(() -> tvShowRequestService.create(request));
    assertNotNull(createdRequest);
  }

  // testeamos pedir un tv show que SÍ tengamos en local (resultado not OK)
  @Test
  public void testTvShowRequestCreateTvShowInLocalNotOk() {
    TvShowRequest request = new TvShowRequest(78804, user1);
    TvShowRequest createdRequest = jpa.withTransaction(() -> tvShowRequestService.create(request));
    assertNull(createdRequest);
  }

  // testeamos pedir una tv show que no tengamos en local con user inexistente (resultado not OK)
  @Test
  public void testTvShowRequestCreateTvShowInexistentUserNotOk() {
    user1.id = 2;
    TvShowRequest request = new TvShowRequest(296762, user1);
    jpa.withTransaction(() -> {
      TvShowRequest createdRequest = null;
      try {
        createdRequest = tvShowRequestService.create(request);
      } catch (Exception ex) {
        // excepcion
      }
      assertNull(createdRequest);
    });
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
      TvShowRequest request1 = new TvShowRequest(222222, user1);
      TvShowRequest request2 = new TvShowRequest(333333, user1);
      tvShowRequestService.create(request1);
      tvShowRequestService.create(request2);
      requests.clear();
      requests = tvShowRequestService.all();

      // cambiamos estado
      requests.get(requests.size() - 2).status = TvShowRequest.Status.Requested;
      requests.get(requests.size() - 1).status = TvShowRequest.Status.Processing;

      // obtenemos peticiones pending
      requests.clear();
      requests = tvShowRequestService.getPending();

      assertEquals( 2, requests.size());
    });
  }

  // testeamos a actualizar una petición: user null, status string
  @Test
  public void testTvShowRequestUpdateUserNullStatusString() {
    jpa.withTransaction(() -> {
      TvShowRequest request = tvShowRequestService.findById(1);
      tvShowRequestService.update(request, null, "Processing");
      assertEquals(TvShowRequest.Status.Processing, tvShowRequestService.findById(1).status);
    });
  }

  // testeamos a actualizar una petición: user not null, status Status
  @Test
  public void testTvShowRequestUpdateUserStatus() {
    jpa.withTransaction(() -> {
      TvShowRequest request = tvShowRequestService.findById(1);
      tvShowRequestService.update(request, user1, TvShowRequest.Status.Processing);
      assertEquals(TvShowRequest.Status.Processing, tvShowRequestService.findById(1).status);
    });
  }

}
