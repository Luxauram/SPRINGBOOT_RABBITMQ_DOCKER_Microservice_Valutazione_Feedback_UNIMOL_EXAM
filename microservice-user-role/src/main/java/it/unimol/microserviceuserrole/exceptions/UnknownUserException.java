package it.unimol.microserviceuserrole.exceptions;

/**
 * Eccezione lanciata quando un utente richiesto non viene trovato nel sistema.
 * Viene utilizzata per segnalare che l'utente cercato non esiste nel database.
 */
public class UnknownUserException extends Exception {

  /**
   * Costruisce una nuova UnknownUserException con il messaggio specificato.
   *
   * @param message il messaggio di errore dettagliato
   */
  public UnknownUserException(String message) {
    super(message);
  }
}