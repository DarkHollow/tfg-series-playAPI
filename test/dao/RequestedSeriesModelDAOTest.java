package dao;

import models.RequestedSeries;
import models.Usuario;
import models.dao.RequestedSeriesDAO;
import models.dao.UsuarioDAO;
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

public class RequestedSeriesModelDAOTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;

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
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/requestedSeries_dataset.xml"));
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

  // testeamos crear una request
  @Test
  public void testRequestedSeriesDAOCreate() {
    final UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);
    final RequestedSeriesDAO requestedSeriesDAO = new RequestedSeriesDAO(jpa);

    Usuario usuario = jpa.withTransaction(() -> usuarioDAO.find(1));

    RequestedSeries request1 = new RequestedSeries(222222, usuario);

    RequestedSeries request2 = jpa.withTransaction(() -> requestedSeriesDAO.create(request1));
    //jpa.withTransaction(() -> usuario.requestedSeries.add(request1));

    assertEquals(request1.idTVDB, request2.idTVDB);
    assertEquals(request1.usuario.id, request2.usuario.id);
    assertEquals(request1.requestDate, request2.requestDate);
  }

  // testeamos buscar una request
  @Test
  public void testRequestedSeriesDAOFind() {
    final RequestedSeriesDAO requestedSeriesDAO = new RequestedSeriesDAO(jpa);

    RequestedSeries request = jpa.withTransaction(() -> requestedSeriesDAO.find(1));

    assertEquals(1, (int) request.id);
    assertEquals(111111, (int) request.idTVDB);
    assertEquals(1, (int) request.usuario.id);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testRequestedSeriesDAOFindNotFound() {
    final RequestedSeriesDAO requestedSeriesDAO = new RequestedSeriesDAO(jpa);
    RequestedSeries request = jpa.withTransaction(() -> requestedSeriesDAO.find(0));

    assertNull(request);
  }

  // testeamos buscar por campo coincidendo exacto (usuarioId)
  @Test
  public void testRequestedSeriesDAOFindByExact() {
    final RequestedSeriesDAO requestedSeriesDAO = new RequestedSeriesDAO(jpa);
    List<RequestedSeries> requestsEncontrados = jpa.withTransaction(() -> requestedSeriesDAO.findByExact("usuarioId", "0"));

    assertEquals(0, requestsEncontrados.size());

    requestsEncontrados = jpa.withTransaction(() -> requestedSeriesDAO.findByExact("usuarioId", "1"));

    assertEquals(1, requestsEncontrados.size());
    assertEquals(1, (int) requestsEncontrados.get(0).id);
  }

  // testeamos obtener todos los requests
  @Test
  public void testRequestedSeriesDAOAll() {
    final RequestedSeriesDAO requestedSeriesDAO = new RequestedSeriesDAO(jpa);
    List<RequestedSeries> requestsEncontrados = jpa.withTransaction(requestedSeriesDAO::all);

    assertEquals(1, requestsEncontrados.size());
  }

  // testeamos delete request
  @Test
  public void testRequestedSeriesDAODelete() {
    final RequestedSeriesDAO requestedSeriesDAO = new RequestedSeriesDAO(jpa);
    RequestedSeries request = jpa.withTransaction(() -> {
      RequestedSeries s = requestedSeriesDAO.find(1);
      requestedSeriesDAO.delete(s);
      return requestedSeriesDAO.find(1);
    });

    assertNull(request);
  }

  // testeamos delete request not found
  @Test
  public void testRequestedSeriesDAODeleteNotFound() {
    final RequestedSeriesDAO requestedSeriesDAO = new RequestedSeriesDAO(jpa);
    jpa.withTransaction(() -> {
      RequestedSeries request = requestedSeriesDAO.find(0);
      try {
        requestedSeriesDAO.delete(request);
      } catch (Exception e) {
        assertNull(request);
      }
    });
  }

  // testeamos delete cascade, borrando un usuario deberian borrarse sus requests
  @Test
  public void testRequestedSeriesDAODeleteCascadeFromUsuario() {
    final RequestedSeriesDAO requestedSeriesDAO = new RequestedSeriesDAO(jpa);
    final UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);

    jpa.withTransaction(() -> {
      RequestedSeries request = requestedSeriesDAO.find(1);
      assertNotNull(request);

      usuarioDAO.delete(usuarioDAO.find(1));
      request = requestedSeriesDAO.find(1);
      assertNull(request);
    });
  }

}
