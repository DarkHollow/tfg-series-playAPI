package utils;

import org.junit.Test;
import play.Logger;
import utils.Security.Password;

import static org.junit.Assert.*;

public class SecurityPasswordTest {
  private final String GOOD_PASSWORD = "BA3253876AED6BC22D4A6FF53D8406C6AD864195ED144AB5C87621B6C233B548BAEAE6956DF346EC8C17F5EA10F35EE3CBC514797ED7DDD3145464E2A0BAB413".toLowerCase();
  private final String GOOD_PASSWORD_HASH = "sha512:64000:18:qJVfoLXprJGWy+Fmi2QqCqTmptxWWLT3:MtMGf4sQHQ7hqfQbX3iW5klV";
  private final String INVALID_SECTIONS_HASH = "sha512:64000:18:qJVfoLXprJGWy+Fmi2QqCqTmptxWWLT3";
  private final String INVALID_TYPE_HASH = "sha99:64000:18:qJVfoLXprJGWy+Fmi2QqCqTmptxWWLT3:MtMGf4sQHQ7hqfQbX3iW5klV";
  private final String INVALID_ITERATION_HASH = "sha512:hola:18:qJVfoLXprJGWy+Fmi2QqCqTmptxWWLT3:MtMGf4sQHQ7hqfQbX3iW5klV";
  private final String INVALID_ITERATION_NUMBER_HASH = "sha512:0:18:qJVfoLXprJGWy+Fmi2QqCqTmptxWWLT3:MtMGf4sQHQ7hqfQbX3iW5klV";
  private final String INVALID_SECTION_SIZE_HASH = "sha512:64000:hola:qJVfoLXprJGWy+Fmi2QqCqTmptxWWLT3:MtMGf4sQHQ7hqfQbX3iW5klV";
  private final String INVALID_SIZE_HASH = "sha512:64000:18:qJVfoLXprJGWy+Fmi2QqCqTmptxWWLT3:MtMGf4s_INVALIDO_QHQ7hqfQbX3iW5klV";
  private final String BAD_PASSWORD = "111111";

  private Password securityPassword = new Password();

  // testeamos verify -> contraseña correcta
  @Test
  public void SecurityPasswordVerifyPasswordOkTest() {
    Boolean verifyResult = false;

    try {
      verifyResult = securityPassword.verifyPassword(GOOD_PASSWORD, GOOD_PASSWORD_HASH);
    } catch (Exception ex) {
      Logger.error(ex.getMessage());
      fail("Excepción no esperada. Algo falla en la verificación del hash");
    }

    assertTrue(verifyResult);
  }

  // testeamos verify -> contraseña incorrecta
  @Test
  public void SecurityPasswordVerifyPasswordNotOkTest() {
    Boolean verifyResult = true;

    try {
      verifyResult = securityPassword.verifyPassword(BAD_PASSWORD, GOOD_PASSWORD_HASH);
    } catch (Exception ex) {
      Logger.error(ex.getMessage());
      fail("Excepción no esperada. Algo falla en la verificación del hash");
    }

    assertFalse(verifyResult);
  }

  // testeamos verify -> InvalidHashException: número de secciones del hash inválido
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashExceptionSectionsTest() {
    String EX_MESSAGE = "Fields are missing from the password hash.";

    try {
      securityPassword.verifyPassword(GOOD_PASSWORD, INVALID_SECTIONS_HASH);
      fail("Debería haber lanzado InvalidHashException");
    } catch (Exception ex) {
      assertEquals(Password.InvalidHashException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // testeamos verify -> CannotPerformOperationException: tipo de hash inválido (debería ser SHA1)
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashTypeTest() {
    String EX_MESSAGE = "Unsupported hash type.";

    try {
      securityPassword.verifyPassword(GOOD_PASSWORD, INVALID_TYPE_HASH);
      fail("Debería haber lanzado CannotPerformOperationException");
    } catch (Exception ex) {
      assertEquals(Password.CannotPerformOperationException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // testeamos verify -> InvalidHashException: numero de iteraciones de hash inválido (no es un numero)
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashIterationNotNumberTest() {
    String EX_MESSAGE = "Could not parse the iteration count as an integer.";

    try {
      securityPassword.verifyPassword(GOOD_PASSWORD, INVALID_ITERATION_HASH);
      fail("Debería haber lanzado InvalidHashException");
    } catch (Exception ex) {
      assertEquals(Password.InvalidHashException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // testeamos verify -> InvalidHashException: numero de iteraciones de hash inválido (menor que 1)
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashIterationNumberTest() {
    String EX_MESSAGE = "Invalid number of iterations. Must be >= 1.";

    try {
      securityPassword.verifyPassword(GOOD_PASSWORD, INVALID_ITERATION_NUMBER_HASH);
      fail("Debería haber lanzado InvalidHashException");
    } catch (Exception ex) {
      assertEquals(Password.InvalidHashException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // testeamos verify -> InvalidHashException: tamaño del hash debe ser integer
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashSizeTest() {
    String EX_MESSAGE = "Could not parse the hash size as an integer.";

    try {
      securityPassword.verifyPassword(GOOD_PASSWORD, INVALID_SECTION_SIZE_HASH);
      fail("Debería haber lanzado InvalidHashException");
    } catch (Exception ex) {
      assertEquals(Password.InvalidHashException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // testeamos verify -> InvalidHashException: tamaño del hash debe coincidir
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashSectionSizeTest() {
    String EX_MESSAGE = "Hash length doesn't match stored hash length.";

    try {
      securityPassword.verifyPassword(GOOD_PASSWORD, INVALID_SIZE_HASH);
      fail("Debería haber lanzado InvalidHashException");
    } catch (Exception ex) {
      assertEquals(Password.InvalidHashException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // comprobaremos que el hash que genera es distinto usando la misma contraseña
  @Test
  public void SecurityPasswordCreateHashIsDifTest() {
    // hashes a comparar
    String hash1, hash2;
    hash1 = hash2 = "1";

    try {
      hash1 = securityPassword.createHash(GOOD_PASSWORD);
      hash2 = securityPassword.createHash(GOOD_PASSWORD);
    } catch (Exception ex) {
      Logger.error(ex.getMessage());
      fail("Excepción no esperada. Algo falla en la creación del hash");
    }

    assertNotEquals(hash1, hash2);
  }

}