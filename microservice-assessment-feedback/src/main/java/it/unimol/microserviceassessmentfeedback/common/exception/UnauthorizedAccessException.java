package it.unimol.microserviceassessmentfeedback.common.exception;

/**
 * Lancia un'eccezione quando viene trovato un accesso non autorizzato.
 */
public class UnauthorizedAccessException extends RuntimeException {
  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public UnauthorizedAccessException(String message) {
    super(message);
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

}
