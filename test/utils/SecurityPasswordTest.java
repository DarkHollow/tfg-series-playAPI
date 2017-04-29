package utils;

import org.junit.Test;
import play.Logger;

import static org.junit.Assert.*;

public class SecurityPasswordTest {
  private final String GOOD_PASSWORD = "123456";
  private final String GOOD_PASSWORD_HASH = "sha1:64000:18:4lQrF5gzo3z+l6tsmdAuODQRmo6hGt0N:1wGzUmfWiwh2DUrPUhactIUJ";
  private final String INVALID_SECTIONS_HASH = "sha1:64000:18:4lQrF5gzo3z+l6tsmdAuODQRmo6hGt0N";
  private final String INVALID_TYPE_HASH = "sha2:64000:18:4lQrF5gzo3z+l6tsmdAuODQRmo6hGt0N:1wGzUmfWiwh2DUrPUhactIUJ";
  private final String INVALID_ITERATION_HASH = "sha1:hola:18:4lQrF5gzo3z+l6tsmdAuODQRmo6hGt0N:1wGzUmfWiwh2DUrPUhactIUJ";
  private final String INVALID_ITERATION_NUMBER_HASH = "sha1:0:18:4lQrF5gzo3z+l6tsmdAuODQRmo6hGt0N:1wGzUmfWiwh2DUrPUhactIUJ";
  private final String INVALID_SECTION_SIZE_HASH = "sha1:64000:hola:4lQrF5gzo3z+l6tsmdAuODQRmo6hGt0N:1wGzUmfWiwh2DUrPUhactIUJ";
  private final String INVALID_SIZE_HASH = "sha1:64000:18:4lQrF5gzo3z+l6tsmdAuODQRmo6hGt0N:1wGzUmfWiwhASDFASDFASDF2DUrPUhactIUJ";
  private final String BAD_PASSWORD = "111111";

  // testeamos verify -> contraseña correcta
  @Test
  public void SecurityPasswordVerifyPasswordOkTest() {
    Boolean verifyResult = false;

    try {
      verifyResult = SecurityPassword.verifyPassword(GOOD_PASSWORD, GOOD_PASSWORD_HASH);
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
      verifyResult = SecurityPassword.verifyPassword(BAD_PASSWORD, GOOD_PASSWORD_HASH);
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
      SecurityPassword.verifyPassword(GOOD_PASSWORD, INVALID_SECTIONS_HASH);
      fail("Debería haber lanzado InvalidHashException");
    } catch (Exception ex) {
      assertEquals(SecurityPassword.InvalidHashException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // testeamos verify -> CannotPerformOperationException: tipo de hash inválido (debería ser SHA1)
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashTypeTest() {
    String EX_MESSAGE = "Unsupported hash type.";

    try {
      SecurityPassword.verifyPassword(GOOD_PASSWORD, INVALID_TYPE_HASH);
      fail("Debería haber lanzado CannotPerformOperationException");
    } catch (Exception ex) {
      assertEquals(SecurityPassword.CannotPerformOperationException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // testeamos verify -> InvalidHashException: numero de iteraciones de hash inválido (no es un numero)
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashIterationNotNumberTest() {
    String EX_MESSAGE = "Could not parse the iteration count as an integer.";

    try {
      SecurityPassword.verifyPassword(GOOD_PASSWORD, INVALID_ITERATION_HASH);
      fail("Debería haber lanzado InvalidHashException");
    } catch (Exception ex) {
      assertEquals(SecurityPassword.InvalidHashException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // testeamos verify -> InvalidHashException: numero de iteraciones de hash inválido (menor que 1)
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashIterationNumberTest() {
    String EX_MESSAGE = "Invalid number of iterations. Must be >= 1.";

    try {
      SecurityPassword.verifyPassword(GOOD_PASSWORD, INVALID_ITERATION_NUMBER_HASH);
      fail("Debería haber lanzado InvalidHashException");
    } catch (Exception ex) {
      assertEquals(SecurityPassword.InvalidHashException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // testeamos verify -> InvalidHashException: tamaño del hash debe ser integer
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashSizeTest() {
    String EX_MESSAGE = "Could not parse the hash size as an integer.";

    try {
      SecurityPassword.verifyPassword(GOOD_PASSWORD, INVALID_SECTION_SIZE_HASH);
      fail("Debería haber lanzado InvalidHashException");
    } catch (Exception ex) {
      assertEquals(SecurityPassword.InvalidHashException.class, ex.getClass());
      assertEquals(EX_MESSAGE, ex.getMessage());
    }

  }

  // testeamos verify -> InvalidHashException: tamaño del hash debe coincidir
  @Test
  public void SecurityPasswordVerifyPasswordInvalidHashSectionSizeTest() {
    String EX_MESSAGE = "Hash length doesn't match stored hash length.";

    try {
      SecurityPassword.verifyPassword(GOOD_PASSWORD, INVALID_SIZE_HASH);
      fail("Debería haber lanzado InvalidHashException");
    } catch (Exception ex) {
      assertEquals(SecurityPassword.InvalidHashException.class, ex.getClass());
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
      hash1 = SecurityPassword.createHash(GOOD_PASSWORD);
      hash2 = SecurityPassword.createHash(GOOD_PASSWORD);
    } catch (Exception ex) {
      Logger.error(ex.getMessage());
      fail("Excepción no esperada. Algo falla en la creación del hash");
    }

    assertNotEquals(hash1, hash2);
  }

}