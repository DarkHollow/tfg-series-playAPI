package utils.Security;

import org.apache.commons.codec.binary.Base64;
import play.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class Password {

  static public class InvalidHashException extends Exception {
    InvalidHashException(String message) {
      super(message);
    }
    InvalidHashException(String message, Throwable source) {
      super(message, source);
    }
  }

  static public class CannotPerformOperationException extends Exception {
    CannotPerformOperationException(String message) {
      super(message);
    }
    CannotPerformOperationException(String message, Throwable source) {
      super(message, source);
    }
  }

  // java soporta hasta SHA512 (futuro SHA-3?)
  private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA512";
  private static final String ALGORITHM = "SHA512";

  // constantes que pueden ser cambiadas
  private static final int SALT_BYTE_SIZE = 24;
  private static final int HASH_BYTE_SIZE = 18;
  private static final int PBKDF2_ITERATIONS = 64000;

  // constantes que no pueden ser cambiadas !
  private static final int HASH_SECTIONS = 5;
  private static final int HASH_ALGORITHM_INDEX = 0;
  private static final int ITERATION_INDEX = 1;
  private static final int HASH_SIZE_INDEX = 2;
  private static final int SALT_INDEX = 3;
  private static final int PBKDF2_INDEX = 4;

  public Password() {}

  public String createHash(String password) throws CannotPerformOperationException {
    return createHash(password.toCharArray());
  }

  private String createHash(char[] password) throws CannotPerformOperationException {
    // generar una salt aleatoria
    SecureRandom sr = new SecureRandom();
    byte[] salt = new byte[SALT_BYTE_SIZE];
    sr.nextBytes(salt);

    // hash del password
    byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
    int hashSize = hash.length;

    // formato: algoritmo:iteraciones:tamañoHash:salt:hash
    return ALGORITHM.toLowerCase() +
            ":" +
            PBKDF2_ITERATIONS +
            ":" +
            hashSize +
            ":" +
            Base64.encodeBase64String(salt) +
            ":" +
            Base64.encodeBase64String(hash);
  }

  public boolean verifyPassword(String password, String correctHash)
          throws CannotPerformOperationException, InvalidHashException {
    return verifyPassword(password.toCharArray(), correctHash);
  }

  private boolean verifyPassword(char[] password, String correctHash)
          throws CannotPerformOperationException, InvalidHashException {

    // Decode el hash en sus partes
    String[] params = correctHash.split(":");
    if (params.length != HASH_SECTIONS) {
      throw new InvalidHashException("Fields are missing from the password hash.");
    }

    // java solo soporta SHA512 de momento (a falta de SHA3)
    if (!params[HASH_ALGORITHM_INDEX].equals(ALGORITHM.toLowerCase())) {
      throw new CannotPerformOperationException("Unsupported hash type.");
    }

    int iterations;
    try {
      iterations = Integer.parseInt(params[ITERATION_INDEX]);
    } catch (NumberFormatException ex) {
      throw new InvalidHashException("Could not parse the iteration count as an integer.", ex);
    }

    if (iterations < 1) {
      throw new InvalidHashException("Invalid number of iterations. Must be >= 1.");
    }

    byte[] salt;
    try {
      salt = Base64.decodeBase64(params[SALT_INDEX]);
      //salt = fromBase64(params[SALT_INDEX]);
    } catch (IllegalArgumentException ex) {
      throw new InvalidHashException("Base64 decoding of salt failed.", ex);
    }

    byte[] hash;
    try {
      hash = Base64.decodeBase64(params[PBKDF2_INDEX]);
      //hash = fromBase64(params[PBKDF2_INDEX]);
    } catch (IllegalArgumentException ex) {
      throw new InvalidHashException("Base64 decoding of pbkdf2 output failed.", ex);
    }

    int storedHashSize;
    try {
      storedHashSize = Integer.parseInt(params[HASH_SIZE_INDEX]);
    } catch (NumberFormatException ex) {
      throw new InvalidHashException("Could not parse the hash size as an integer.", ex);
    }

    if (storedHashSize != hash.length) {
      throw new InvalidHashException("Hash length doesn't match stored hash length.");
    }

    // calcula el hash del password proporcionado, usando la misma salt,
    // las mismas iteraciones y la misma longitud de la salt
    byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
    // comparamos los hashes en tiempo constante, el password es correcto si
    // los hashes coinciden
    return slowEquals(hash, testHash);
  }

  // compara dos arrays en length constant time
  private boolean slowEquals(byte[] a, byte[] b) {
    int diff = a.length ^ b.length;
    for (int i = 0; i < a.length && i < b.length; i++) {
      diff |= a[i] ^ b[i];
    }
    return diff == 0;
  }

  private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
          throws CannotPerformOperationException {
    try {
      PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
      SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
      return skf.generateSecret(spec).getEncoded();
    } catch (NoSuchAlgorithmException ex) {
      throw new CannotPerformOperationException("Hash algorithm not supported", ex);
    } catch (InvalidKeySpecException ex) {
      throw new CannotPerformOperationException("Invalid key spec", ex);
    }
  }

}
