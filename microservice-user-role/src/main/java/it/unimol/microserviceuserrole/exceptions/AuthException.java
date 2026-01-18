package it.unimol.microserviceuserrole.exceptions;

/**
 * Eccezione lanciata quando si verificano errori di autenticazione.
 * Viene utilizzata per gestire casi come credenziali non valide o token scaduti.
 */
public class AuthException extends Exception {

  /**
   * Costruisce una nuova AuthException con il messaggio specificato.
   *
   * @param message il messaggio di errore dettagliato
   */
  public AuthException(String message) {
    super(message);
  }
}