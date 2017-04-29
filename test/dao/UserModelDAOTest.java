package dao;

import models.User;
import models.dao.UserDAO;
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

public class UserModelDAOTest {
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

  // testeamos crear una user
  @Test
  public void testUserDAOCreate() {
    final UserDAO userDAO = new UserDAO(jpa);

    User user1 = new User("user1@email.com", "contraseña1", "nombre1");

    User user2 = jpa.withTransaction(() -> userDAO.create(user1));

    //assertEquals(user1.id, user2.id);
    assertEquals(user1.email, user2.email);
    assertEquals(user1.password, user2.password);
    assertEquals(user1.name, user2.name);
    assertEquals(user1.registrationDate, user2.registrationDate);
  }

  // testeamos buscar por id -> found
  @Test
  public void testUserDAOFind() {
    final UserDAO userDAO = new UserDAO(jpa);
    User user = jpa.withTransaction(() -> userDAO.find(1));

    assertEquals(1, (int) user.id);
    assertEquals("email1", user.email);
    assertEquals("password1", user.password);

  }

  // testeamos buscar por id -> not found
  @Test
  public void testUserDAOFindNotFound() {
    final UserDAO userDAO = new UserDAO(jpa);
    User user = jpa.withTransaction(() -> userDAO.find(0));

    assertNull(user);
  }

  // testeamos buscar email -> found
  @Test
  public void testUserDAOFindByEmailFound() {
    final UserDAO userDAO = new UserDAO(jpa);
    User userEncontrado = jpa.withTransaction(() -> userDAO.findByEmail("email1"));

    assertNotNull(userEncontrado);
    assertEquals("email1", userEncontrado.email);
  }

  // testeamos buscar email -> not found
  @Test
  public void testUserDAOFindByEmailNotFound() {
    final UserDAO userDAO = new UserDAO(jpa);
    User userEncontrado = jpa.withTransaction(() -> userDAO.findByEmail("email2"));

    assertNull(userEncontrado);
  }

  // testeamos buscar por campo coincidendo exacto (email)
  @Test
  public void testUserDAOFindByExact() {
    final UserDAO userDAO = new UserDAO(jpa);
    List<User> usersEncontrados = jpa.withTransaction(() -> userDAO.findByExact("email", "email2"));

    assertEquals(0, usersEncontrados.size());

    usersEncontrados = jpa.withTransaction(() -> userDAO.findByExact("email", "email1"));

    assertEquals(1, usersEncontrados.size());
    assertEquals("nombre1", usersEncontrados.get(0).name);
  }

  // testeamos buscar por campo coincidiendo LIKE (nombre)
  // NOTE: case sensitive
  @Test
  public void testUserDAOFindByLike() {
    final UserDAO userDAO = new UserDAO(jpa);
    List<User> usersEncontrados = jpa.withTransaction(() -> userDAO.findByLike("name", "nombre"));

    assertEquals(1, usersEncontrados.size());
    assertEquals("nombre1", usersEncontrados.get(0).name);
  }

  // testeamos obtener todos los users
  @Test
  public void testUserDAOAll() {
    final UserDAO userDAO = new UserDAO(jpa);
    List<User> usersEncontrados = jpa.withTransaction(userDAO::all);

    assertEquals(1, usersEncontrados.size());
  }

  // testeamos delete user
  @Test
  public void testUserDAODelete() {
    final UserDAO userDAO = new UserDAO(jpa);
    User user = jpa.withTransaction(() -> {
      User s = userDAO.find(1);
      userDAO.delete(s);
      return userDAO.find(1);
    });

    assertNull(user);
  }

  // testeamos delete user not found
  @Test
  public void testUserDAODeleteNotFound() {
    final UserDAO userDAO = new UserDAO(jpa);
    jpa.withTransaction(() -> {
      User user = userDAO.find(0);
      try {
        userDAO.delete(user);
      } catch (Exception e) {
        assertNull(user);
      }
    });
  }

}
