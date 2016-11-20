import play.db.Database;
import play.db.Databases;
import play.db.jpa.*;
import play.Logger;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import models.TestPersistencia;

public class PersistenciaTest {
  Database db;
  JPAApi jpa;

  // antes de cada test, inicializamos la base de datos
  @Before
  public void initDatabase() {
    // conectamos con la base de datos de test
    db = Databases.inMemoryWith("jndiName", "DefaultDS");
    db.getConnection();
    // activamos modo MySQL
    db.withConnection(connection -> {
      connection.createStatement().execute("SET MODE MySQL;");
    });
    jpa = JPA.createFor("memoryPersistenceUnit");
  }

  // al final de cada test, limpiamos la base de datos y la cerramos
  @After
  public void shutdownDatabase() {
    db.withConnection(connection -> {
      connection.createStatement().execute("DROP TABLE TestPersistencia");
    });
    jpa.shutdown();
    db.shutdown();
  }

  // probamos la persistencia insertando algo y
  // posteriormente lo buscandolo
  @Test
  public void testInsertSelect() {

    Integer id = jpa.withTransaction(() -> {
      TestPersistencia nuevo = new TestPersistencia();
      nuevo.value = "Test 1";

      JPA.em().persist(nuevo);
      JPA.em().flush();
      JPA.em().refresh(nuevo);
      return nuevo.id;
    });

    jpa.withTransaction(() -> {
      TestPersistencia testObj = JPA.em().find(TestPersistencia.class, id);
      assertEquals("Test 1", testObj.value);
    });

    Logger.debug("Persistencia OK");

  }

}
