package service;

import models.User;
import models.dao.UserDAO;
import models.service.UserService;
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
import utils.SecurityPassword;

import java.io.FileInputStream;

import static org.junit.Assert.*;

public class UserServiceTest {
  private static Database db;
  private static JPAApi jpa;
  private JndiDatabaseTester databaseTester;
  private static UserService userService;

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
    UserDAO userDAO = new UserDAO(jpa);
    SecurityPassword securityPassword = new SecurityPassword();
    userService = new UserService(userDAO, securityPassword);
  }

  @Before
  public void initData() throws Exception {
    databaseTester = new JndiDatabaseTester("DefaultDS");
    IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new
      FileInputStream("test/resources/user_dataset.xml"));
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

  // testeamos crear usuario (registro) -> ok
  @Test
  public void testUserServiceCreateOk() {

    jpa.withTransaction(() -> {
      User user = null;
      try {
        user = userService.create("newEmail", "password", "name");
      } catch (Exception ex) {
        Logger.error(ex.getMessage());
        fail("No debería haber dado excepción");
      }
      assertNotNull(user);
    });
  }

  // testeamos crear usuario (registro) -> fail, email ya registrado (javax.persistence.PersistenceException)
  @Test
  public void testUserServiceCreateFail() {
    jpa.withTransaction(() -> {
      User user = null;
      try {
        user = userService.create("email1", "password", "name");
        fail("Debería haber dado javax.persistence.PersistenceException");
      } catch (Exception ex) {
        Logger.error(ex.getMessage());
        assertEquals(javax.persistence.PersistenceException.class, ex.getClass());
      }
      assertNull(user);
    });
  }

  // testeamos buscar por id -> found
  @Test
  public void testUserServiceFindFound() {
    User user = jpa.withTransaction(() -> userService.find(1));

    assertEquals(1, (int) user.id);
    assertEquals("email1", user.email);
    assertEquals("password1", user.password);
  }

  // testeamos buscar por id -> not found
  @Test
  public void testUserServiceFindNotFound() {
    User user = jpa.withTransaction(() -> userService.find(0));

    assertNull(user);
  }

  // testeamos buscar por email -> found
  @Test
  public void testUserServiceFindByEmailFound() {
    User user = jpa.withTransaction(() -> userService.findByEmail("email1"));

    assertEquals(1, (int) user.id);
    assertEquals("email1", user.email);
    assertEquals("password1", user.password);
  }

  // testeamos buscar por email -> not found
  @Test
  public void testUserServiceFindByEmailNotFound() {
    User user = jpa.withTransaction(() -> userService.findByEmail("email2"));

    assertNull(user);
  }

}
