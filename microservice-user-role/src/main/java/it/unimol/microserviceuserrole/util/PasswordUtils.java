package it.unimol.microserviceuserrole.util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;

/**
 * Utility class per la gestione delle password utilizzando l'algoritmo Argon2.
 * Fornisce metodi per l'hashing e la verifica delle password.
 */
@Component
public final class PasswordUtils {

  private static final Argon2 argon2 = Argon2Factory.create();

  /**
   * Genera un hash della password utilizzando l'algoritmo Argon2.
   *
   * @param password La password in chiaro da hashare.
   * @return L'hash della password.
   */
  public static String hashPassword(String password) {
    char[] passwordChars = password.toCharArray();

    try {
      return argon2.hash(2, 1024, 1, passwordChars);
    } finally {
      argon2.wipeArray(passwordChars);
    }
  }

  /**
   * Verifica se una password corrisponde all'hash fornito.
   *
   * @param hash     L'hash della password da verificare.
   * @param password La password in chiaro da confrontare.
   * @return true se la password corrisponde all'hash, false altrimenti.
   */
  public static boolean verificaPassword(String hash, String password) {
    char[] passwordChars = password.toCharArray();

    try {
      return argon2.verify(hash, passwordChars);
    } finally {
      argon2.wipeArray(passwordChars);
    }
  }
}