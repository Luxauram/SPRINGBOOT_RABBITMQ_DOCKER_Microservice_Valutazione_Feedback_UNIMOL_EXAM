package it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants;

/**
 * Costanti per la configurazione delle code RabbitMQ. Include argomenti delle code, configurazioni
 * dead letter e valori di default per TTL, retry e delay dei messaggi.
 */
public final class QueueConfigurationConstants {

  // ===================================================================
  //  QUEUE ARGUMENTS
  // ===================================================================
  public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
  public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
  public static final String X_MESSAGE_TTL = "x-message-ttl";
  public static final String X_MAX_RETRIES = "x-max-retries";
  public static final String X_RETRY_DELAY = "x-retry-delay";
  // ===================================================================
  //  DEAD LETTER CONFIGURATION
  // ===================================================================
  public static final String DEAD_LETTER_ROUTING_KEY = "dlq";
  // ===================================================================
  //  DEFAULT VALUES
  // ===================================================================
  public static final int DEFAULT_MESSAGE_TTL = 86400000;
  public static final int DEFAULT_MAX_RETRIES = 3;
  public static final int DEFAULT_RETRY_DELAY = 5000;

  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  private QueueConfigurationConstants() {
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

}
