package it.unimol.microserviceassessmentfeedback.common.exception;

/**
 * Lancia un'eccezione quando una risorsa non viene trovata.
 */
public class ResourceNotFoundException extends RuntimeException {
  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

}
