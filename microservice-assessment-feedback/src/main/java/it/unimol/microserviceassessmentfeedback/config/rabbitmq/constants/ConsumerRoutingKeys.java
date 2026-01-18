package it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants;

/**
 * Costanti per le routing key dei consumer RabbitMQ. Definisce le chiavi di routing per i messaggi
 * ricevuti da altri microservizi (Assignment, Exam, Course, User).
 */
public class ConsumerRoutingKeys {

  // ===================================================================
  //  ASSIGNMENT ROUTING KEYS - Da Gestione Compiti (Vittorio)
  // ===================================================================
  public static final String ASSIGNMENT_CREATED = "assignment.created";
  public static final String ASSIGNMENT_UPDATED = "assignment.updated";
  public static final String ASSIGNMENT_SUBMITTED = "assignment.submitted";
  // ===================================================================
  //  EXAM ROUTING KEYS - Da Gestione Esami (Luca)
  // ===================================================================
  public static final String EXAM_COMPLETED = "exam.completed";
  public static final String EXAM_GRADE_REGISTERED = "exam.grade.registered";
  // ===================================================================
  //  COURSE ROUTING KEYS - Da Gestione Corsi (Marco)
  // ===================================================================
  public static final String COURSE_CREATED = "course.created";
  public static final String COURSE_DELETED = "course.deleted";
  // ===================================================================
  //  USER ROUTING KEYS - Da Gestione Utenti (Mauro)
  // ===================================================================
  public static final String USER_CREATED = "user.created";
  public static final String USER_UPDATED = "user.updated";
  public static final String USER_DELETED = "user.deleted";
  public static final String ROLE_ASSIGNED = "role.assigned";
  public static final String TEACHER_CREATED = "teacher.created";
  public static final String STUDENT_CREATED = "student.created";

  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  private ConsumerRoutingKeys() {
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============


}
