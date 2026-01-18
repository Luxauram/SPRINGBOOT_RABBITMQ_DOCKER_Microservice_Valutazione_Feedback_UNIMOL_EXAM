package it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants;

/**
 * Costanti per l'infrastruttura RabbitMQ. Definisce nomi degli exchange, tipi, pattern delle
 * routing key, prefissi delle code e configurazioni di durabilità e persistenza.
 */
public final class RabbitMqInfrastructureConstants {

  // ===================================================================
  //  EXCHANGE NAMES (DEFAULT VALUES)
  // ===================================================================
  public static final String DEFAULT_ASSESSMENTS_EXCHANGE = "unimol";
  public static final String DEFAULT_DEAD_LETTER_EXCHANGE = "unimol.dlx";
  // ===================================================================
  //  EXCHANGE TYPES
  // ===================================================================
  public static final String TOPIC_EXCHANGE_TYPE = "topic";
  public static final String DIRECT_EXCHANGE_TYPE = "direct";
  public static final String FANOUT_EXCHANGE_TYPE = "fanout";
  public static final String HEADERS_EXCHANGE_TYPE = "headers";
  // ===================================================================
  //  ROUTING KEY PATTERNS
  // ===================================================================
  public static final String ASSESSMENT_PATTERN = "assessment.*";
  public static final String FEEDBACK_PATTERN = "feedback.*";
  public static final String SURVEY_PATTERN = "survey.*";
  public static final String USER_PATTERN = "user.*";
  public static final String COURSE_PATTERN = "course.*";
  public static final String EXAM_PATTERN = "exam.*";
  public static final String ASSIGNMENT_PATTERN = "assignment.*";
  // ===================================================================
  //  QUEUE NAME PREFIXES
  // ===================================================================
  public static final String ASSESSMENT_QUEUE_PREFIX = "assessment";
  public static final String FEEDBACK_QUEUE_PREFIX = "feedback";
  public static final String SURVEY_QUEUE_PREFIX = "survey";
  public static final String DLQ_SUFFIX = ".dlq";
  // ===================================================================
  //  DURABILITY AND PERSISTENCE
  // ===================================================================
  public static final boolean DEFAULT_DURABLE = true;
  public static final boolean DEFAULT_EXCLUSIVE = false;
  public static final boolean DEFAULT_AUTO_DELETE = false;
  public static final boolean DEFAULT_PERSISTENT = true;

  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  private RabbitMqInfrastructureConstants() {
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

}