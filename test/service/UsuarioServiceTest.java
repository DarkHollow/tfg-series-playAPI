package service;

import models.Usuario;
import models.dao.UsuarioDAO;
import models.service.UsuarioService;
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

public class UsuarioServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static UsuarioService usuarioService;

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
    UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);
    usuarioService = new UsuarioService(usuarioDAO);
  }

  @Before
  public void initData() throws Exception {
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/usuarios_dataset.xml"));
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

  // testeamos buscar por id -> found
  @Test
  public void testUsuarioServiceFindFound() {
    Usuario usuario = jpa.withTransaction(() -> usuarioService.find(1));

    assertEquals(1, (int) usuario.id);
    assertEquals("email1", usuario.email);
    assertEquals("password1", usuario.password);
    assertEquals("salt1", usuario.salt);
  }

  // testeamos buscar por id -> found
  @Test
  public void testUsuarioServiceFindNotFound() {
    Usuario usuario = jpa.withTransaction(() -> usuarioService.find(0));

    assertNull(usuario);
  }

}
