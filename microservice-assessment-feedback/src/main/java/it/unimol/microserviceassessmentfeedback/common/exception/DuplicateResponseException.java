package it.unimol.microserviceassessmentfeedback.common.exception;

/**
 * Lancia un'eccezione quando viene trovato una risposta duplicata.
 */
public class DuplicateResponseException extends RuntimeException {

  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public DuplicateResponseException(String message) {
    super(message);
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

}
