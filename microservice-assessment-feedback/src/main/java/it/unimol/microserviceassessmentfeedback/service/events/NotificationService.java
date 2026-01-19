package it.unimol.microserviceassessmentfeedback.service.events;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service responsabile della gestione e simulazione delle notifiche
 * del sistema Assessment & Feedback.
 *
 * <p>Il servizio produce messaggi di notifica per docenti, studenti
 * e stakeholder, utilizzando il logging come meccanismo di output.</p>
 */
@Service
public class NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
  private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd HH:mm:ss");

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  // ===================================================================
  // NOTIFICHE PER DOCENTI
  // ===================================================================

  /**
   * Notifica al docente la ricezione di una nuova consegna.
   */
  public void notifyTeacherOfSubmission(String teacherId, String assignmentId, String studentId) {
    String message = String.format(
        "[%s] NUOVA CONSEGNA - Docente: %s | Assignment: %s | Studente: %s | Azione richiesta: "
            + "Valutazione",
        getCurrentTimestamp(), teacherId, assignmentId, studentId
    );

    logger.info("NOTIFICATION [TEACHER]: {}", message);
  }

  /**
   * Notifica al docente il completamento di un esame.
   */
  public void notifyTeacherOfExamCompletion(String teacherId, String examId, String studentId) {
    String message = String.format(
        "[%s] ESAME COMPLETATO - Docente: %s | Esame: %s | Studente: %s | Azione richiesta: "
            + "Correzione",
        getCurrentTimestamp(), teacherId, examId, studentId
    );

    logger.info("NOTIFICATION [TEACHER]: {}", message);
  }

  /**
   * Promemoria al docente per valutare assignment in sospeso.
   */
  public void sendAssessmentReminder(String teacherId, String assignmentId) {
    String message = String.format(
        "[%s] PROMEMORIA - Docente: %s | Assignment: %s | Azione: Valutazione in sospeso da più "
            + "di 48h",
        getCurrentTimestamp(), teacherId, assignmentId
    );

    logger.warn("REMINDER [TEACHER]: {}", message);
  }

  // ===================================================================
  // NOTIFICHE PER STUDENTI
  // ===================================================================

  /**
   * Notifica allo studente la ricezione di un feedback.
   */
  public void notifyStudentOfFeedback(String studentId, String assessmentId, String courseId) {
    String message = String.format(
        "[%s] NUOVO FEEDBACK - Studente: %s | Assessment: %s | Corso: %s | Visualizza il tuo "
            + "feedback",
        getCurrentTimestamp(), studentId, assessmentId, courseId
    );

    logger.info("NOTIFICATION [STUDENT]: {}", message);
  }

  /**
   * Notifica allo studente la ricezione di un voto.
   */
  public void notifyStudentOfGrade(String studentId, String examId, String grade, Integer score,
      Integer maxScore) {
    String message = String.format(
        "[%s] NUOVO VOTO - Studente: %s | Esame: %s | Voto: %s | Punteggio: %d/%d",
        getCurrentTimestamp(), studentId, examId, grade, score, maxScore
    );

    logger.info("NOTIFICATION [STUDENT]: {}", message);
  }

  /**
   * Notifica allo studente di un nuovo survey da compilare.
   */
  public void notifyStudentOfSurvey(String studentId, String surveyId, String courseId,
      String surveyType) {
    String message = String.format(
        "[%s] NUOVO SURVEY - Studente: %s | Survey: %s | Corso: %s | Tipo: %s | Compila entro 7 "
            + "giorni",
        getCurrentTimestamp(), studentId, surveyId, courseId, surveyType
    );

    logger.info("NOTIFICATION [STUDENT]: {}", message);
  }

  /**
   * Notifica agli studenti le modifiche significative di un assignment.
   */
  public void notifyStudentsOfAssignmentUpdate(String assignmentId, String courseId,
      String updateType, String title) {
    String message = String.format(
        "[%s] ASSIGNMENT MODIFICATO - Assignment: %s | Corso: %s | Titolo: %s | Modifica: %s | "
            + "Controlla i dettagli aggiornati",
        getCurrentTimestamp(), assignmentId, courseId, title, formatUpdateType(updateType)
    );

    logger.info("NOTIFICATION [ASSIGNMENT_UPDATE]: {}", message);

    switch (updateType) {
      case "DUE_DATE_CHANGED":
        logger.warn(
            "SCADENZA MODIFICATA - Assignment: {} | Corso: {} | Notifica inviata agli studenti",
            assignmentId, courseId);
        break;
      case "MAX_SCORE_CHANGED":
        logger.info(
            "PUNTEGGIO MODIFICATO - Assignment: {} | Corso: {} | Notifica inviata agli studenti",
            assignmentId, courseId);
        break;
      case "REQUIREMENTS_CHANGED":
        logger.warn(
            "REQUISITI MODIFICATI - Assignment: {} | Corso: {} | Notifica inviata agli studenti",
            assignmentId, courseId);
        break;
      default:
        logger.info("MODIFICA GENERICA - Assignment: {} | Corso: {} | Tipo: {} | Notifica inviata",
            assignmentId, courseId, updateType);
    }
  }

  /**
   * Formatta il tipo di aggiornamento in modo user-friendly.
   */
  private String formatUpdateType(String updateType) {
    return switch (updateType) {
      case "DUE_DATE_CHANGED" -> "Scadenza modificata";
      case "MAX_SCORE_CHANGED" -> "Punteggio massimo modificato";
      case "REQUIREMENTS_CHANGED" -> "Requisiti aggiornati";
      case "DESCRIPTION_UPDATED" -> "Descrizione aggiornata";
      case "CRITERIA_UPDATED" -> "Criteri di valutazione aggiornati";
      default -> updateType.replace("_", " ").toLowerCase(Locale.ROOT);
    };
  }

  // ===================================================================
  // NOTIFICHE GENERICHE
  // ===================================================================

  /**
   * Notifica di cancellazione corso a tutti gli stakeholder.
   */
  public void notifyCourseDeletion(String courseId, String teacherId, String reason) {
    String message = String.format(
        "[%s] CORSO CANCELLATO - Corso: %s | Docente: %s | Motivo: %s | Tutti i dati sono stati "
            + "archiviati",
        getCurrentTimestamp(), courseId, teacherId, reason
    );

    logger.warn("NOTIFICATION [COURSE_DELETION]: {}", message);
  }

  /**
   * Notifica di benvenuto per nuovi utenti.
   */
  public void sendWelcomeNotification(String userId, String userType, String department) {
    String message = String.format(
        "[%s] BENVENUTO - Utente: %s | Tipo: %s | Dipartimento: %s | Accesso al sistema "
            + "Assessment & Feedback attivato",
        getCurrentTimestamp(), userId, userType, department
    );

    logger.info("NOTIFICATION [WELCOME]: {}", message);
  }

  // ===================================================================
  // SURVEY NOTIFICATIONS
  // ===================================================================

  /**
   * Attiva notifica per survey post-esame.
   */
  public void activatePostExamSurvey(String studentId, String examId, String courseId) {
    String message = String.format(
        "[%s] SURVEY POST-ESAME - Studente: %s | Esame: %s | Corso: %s | La tua opinione è "
            + "importante",
        getCurrentTimestamp(), studentId, examId, courseId
    );

    logger.info("NOTIFICATION [SURVEY]: {}", message);
  }

  /**
   * Survey di feedback per il corso attivato.
   */
  public void scheduleFeedbackSurvey(String studentId, String examId, String courseId) {
    String message = String.format(
        "[%s] SURVEY FEEDBACK - Studente: %s | Esame: %s | Corso: %s | Programmato per invio "
            + "automatico",
        getCurrentTimestamp(), studentId, examId, courseId
    );

    logger.info("📧 NOTIFICATION [SURVEY_SCHEDULED]: {}", message);
  }

  // ===================================================================
  // UTILITY METHODS
  // ===================================================================

  private String getCurrentTimestamp() {
    return LocalDateTime.now(ZoneId.systemDefault()).format(TIMESTAMP_FORMAT);
  }

  /**
   * Simula l'ottenimento di statistiche di notifica.
   */
  public void logNotificationStats() {
    logger.info("NOTIFICATION STATS - Servizio NotificationService attivo e funzionante");
    logger.info("Implementare qui logiche per tracking delle notifiche inviate/ricevute");
  }
}
