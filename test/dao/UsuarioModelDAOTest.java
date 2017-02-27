package dao;

import models.Usuario;
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
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UsuarioModelDAOTest {
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

  // testeamos crear una usuario
  @Test
  public void testUsuarioDAOCreate() {
    final UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);

    Usuario usuario1 = new Usuario("usuario1@email.com", "contraseña1", "salt", "nombre1");

    Usuario usuario2 = jpa.withTransaction(() -> usuarioDAO.create(usuario1));

    //assertEquals(usuario1.id, usuario2.id);
    assertEquals(usuario1.email, usuario2.email);
    assertEquals(usuario1.password, usuario2.password);
    assertEquals(usuario1.salt, usuario2.salt);
    assertEquals(usuario1.name, usuario2.name);
    assertEquals(usuario1.registrationDate, usuario2.registrationDate);
  }

  // testeamos buscar por id -> found
  @Test
  public void testUsuarioDAOFind() {
    final UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);
    Usuario usuario = jpa.withTransaction(() -> usuarioDAO.find(1));

    assertEquals(1, (int) usuario.id);
    assertEquals("email1", usuario.email);
    assertEquals("password1", usuario.password);
    assertEquals("salt1", usuario.salt);

  }

  // testeamos buscar por id -> not found
  @Test
  public void testUsuarioDAOFindNotFound() {
    final UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);
    Usuario usuario = jpa.withTransaction(() -> {
      return usuarioDAO.find(0);
    });

    assertNull(usuario);
  }

  // testeamos buscar por campo coincidendo exacto (email)
  @Test
  public void testUsuarioDAOFindByExact() {
    final UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);
    List<Usuario> usuariosEncontrados = jpa.withTransaction(() -> {
      return usuarioDAO.findByExact("email", "email2");
    });

    assertEquals(0, usuariosEncontrados.size());

    usuariosEncontrados = jpa.withTransaction(() -> {
      return usuarioDAO.findByExact("email", "email1");
    });

    assertEquals(1, usuariosEncontrados.size());
    assertEquals("nombre1", usuariosEncontrados.get(0).name);
  }

  // testeamos buscar por campo coincidiendo LIKE (nombre)
  // NOTE: case sensitive
  @Test
  public void testUsuarioDAOFindByLike() {
    final UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);
    List<Usuario> usuariosEncontrados = jpa.withTransaction(() -> {
      return usuarioDAO.findByLike("name", "nombre");
    });

    assertEquals(1, usuariosEncontrados.size());
    assertEquals("nombre1", usuariosEncontrados.get(0).name);
  }

  // testeamos obtener todos los usuarios
  @Test
  public void testUsuarioDAOAll() {
    final UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);
    List<Usuario> usuariosEncontrados = jpa.withTransaction(() -> {
      return usuarioDAO.all();
    });

    assertEquals(1, usuariosEncontrados.size());
  }

  // testeamos delete usuario
  @Test
  public void testUsuarioDAODelete() {
    final UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);
    Usuario usuario = jpa.withTransaction(() -> {
      Usuario s = usuarioDAO.find(1);
      usuarioDAO.delete(s);
      return usuarioDAO.find(1);
    });

    assertNull(usuario);
  }

  // testeamos delete usuario not found
  @Test
  public void testUsuarioDAODeleteNotFound() {
    final UsuarioDAO usuarioDAO = new UsuarioDAO(jpa);
    jpa.withTransaction(() -> {
      Usuario usuario = usuarioDAO.find(0);
      try {
        usuarioDAO.delete(usuario);
      } catch (Exception e) {
        assertNull(usuario);
      }
    });
  }

}
