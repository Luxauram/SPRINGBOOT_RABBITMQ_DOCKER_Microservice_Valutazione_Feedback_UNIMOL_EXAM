package it.unimol.microserviceassessmentfeedback.common.exception;

/**
 * Lancia un'eccezione quando una survey viene chiusa.
 */
public class SurveyClosedException extends RuntimeException {
  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public SurveyClosedException(String message) {
    super(message);
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

}
