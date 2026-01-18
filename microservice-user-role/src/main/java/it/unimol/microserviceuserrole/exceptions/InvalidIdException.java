package it.unimol.microserviceuserrole.exceptions;

/**
 * Eccezione lanciata quando viene fornito un ID non valido.
 * Viene utilizzata per segnalare che un identificativo non rispetta
 * il formato o i vincoli richiesti.
 */
public class InvalidIdException extends Exception {

  /**
   * Costruisce una nuova InvalidIdException con il messaggio specificato.
   *
   * @param message il messaggio di errore dettagliato
   */
  public InvalidIdException(String message) {
    super(message);
  }
}