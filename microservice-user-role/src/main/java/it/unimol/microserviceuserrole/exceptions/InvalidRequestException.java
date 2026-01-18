package it.unimol.microserviceuserrole.exceptions;

/**
 * Eccezione lanciata quando una richiesta contiene dati non validi.
 * Viene utilizzata per segnalare errori di validazione o dati mancanti.
 */
public class InvalidRequestException extends Exception {

  /**
   * Costruisce una nuova InvalidRequestException con il messaggio specificato.
   *
   * @param message il messaggio di errore dettagliato
   */
  public InvalidRequestException(String message) {
    super(message);
  }
}