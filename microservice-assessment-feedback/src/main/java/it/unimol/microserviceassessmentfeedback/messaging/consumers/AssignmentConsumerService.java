package it.unimol.microserviceassessmentfeedback.messaging.consumers;

import it.unimol.microserviceassessmentfeedback.dto.AssessmentDto;
import it.unimol.microserviceassessmentfeedback.enums.ReferenceType;
import it.unimol.microserviceassessmentfeedback.service.AssessmentService;
import it.unimol.microserviceassessmentfeedback.service.events.NotificationService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service consumer per la gestione degli eventi relativi agli assignment.
 * Riceve e processa messaggi da RabbitMQ relativi a submission, creazione e aggiornamento
 * degli assignment.
 */
@Service
public class AssignmentConsumerService extends BaseEventConsumer {

  @Autowired
  private AssessmentService assessmentService;

  @Autowired
  private NotificationService notificationService;

  @RabbitListener(queues = "${rabbitmq.queue.assignmentSubmitted}")
  public void handleAssignmentSubmitted(Map<String, Object> message) {
    processMessage(message, "ASSIGNMENT_SUBMITTED");
  }

  @RabbitListener(queues = "${rabbitmq.queue.assignmentCreated}")
  public void handleAssignmentCreated(Map<String, Object> message) {
    processMessage(message, "ASSIGNMENT_CREATED");
  }

  @RabbitListener(queues = "${rabbitmq.queue.assignmentUpdated}")
  public void handleAssignmentUpdated(Map<String, Object> message) {
    processMessage(message, "ASSIGNMENT_UPDATED");
  }

  // ============ Costruttore ============

  // ============ Metodi Override ============
  @Override
  protected void handleMessage(Map<String, Object> message, String messageType) {
    switch (messageType) {
      case "ASSIGNMENT_SUBMITTED":
        processAssignmentSubmitted(message);
        break;
      case "ASSIGNMENT_CREATED":
        processAssignmentCreated(message);
        break;
      case "ASSIGNMENT_UPDATED":
        processAssignmentUpdated(message);
        break;
      default:
        logger.warn("Unknown message type: {}", messageType);
    }
  }

  // ============ Getters & Setters & Bool ============
  /**
   * Verifica se l'assignment è stato consegnato in tempo.
   */
  private boolean isSubmissionOnTime(String assignmentId, Long submissionTime) {
    logger.info("⏰ DEADLINE CHECK - Assignment: {} | Submission time: {}", assignmentId,
        submissionTime);
    return true;
  }

  // ============ Metodi di Classe ============
  private void processAssignmentSubmitted(Map<String, Object> message) {
    String assignmentId = getStringValue(message, "assignmentId");
    String studentId = getStringValue(message, "studentId");
    String courseId = getStringValue(message, "courseId");
    String teacherId = getStringValue(message, "teacherId");
    Long submissionTime = getLongValue(message, "submissionTime");
    String submissionContent = getStringValue(message, "submissionContent");
    String fileName = getStringValue(message, "fileName");
    String fileUrl = getStringValue(message, "fileUrl");

    logger.info(
        "Processing assignment submission - Assignment ID: {}, Student ID: {}, Course ID: {}",
        assignmentId, studentId, courseId);

    try {
      // 1. Creare un assessment per tracciare la consegna
      AssessmentDto assessment = createAssessmentFromAssignment(
          assignmentId, studentId, courseId, teacherId, submissionTime, fileName
      );
      AssessmentDto createdAssessment = assessmentService.createAssessment(assessment);
      logger.info("Created assessment with ID: {} for assignment: {}",
          createdAssessment.getId(), assignmentId);

      // 2. Inviare notifica al docente
      notificationService.notifyTeacherOfSubmission(teacherId, assignmentId, studentId);

      // 3. Programmare promemoria per la valutazione (dopo 48 ore)
      scheduleAssessmentReminder(teacherId, assignmentId, createdAssessment.getId());

      // 4. Aggiornare statistiche del corso (log per ora)
      updateCourseStatistics(courseId, "assignment_submitted", studentId);

      // 5. Se c'è del contenuto testuale, salvarlo nelle note dell'assessment
      if (submissionContent != null && !submissionContent.trim().isEmpty()) {
        updateAssessmentWithSubmissionDetails(createdAssessment.getId(), submissionContent,
            fileUrl);
      }

      logger.info("Assignment submission processed successfully for assignment: {}", assignmentId);

    } catch (Exception e) {
      logger.error("Error processing assignment submission for assignment: {}", assignmentId, e);
      throw e;
    }
  }

  private void processAssignmentCreated(Map<String, Object> message) {
    String assignmentId = getStringValue(message, "assignmentId");
    String courseId = getStringValue(message, "courseId");
    String teacherId = getStringValue(message, "teacherId");
    String title = getStringValue(message, "title");
    String description = getStringValue(message, "description");
    Long dueDate = getLongValue(message, "dueDate");
    Integer maxScore = getIntegerValue(message, "maxScore");
    String assignmentType = getStringValue(message, "assignmentType");

    logger.info("Processing assignment creation - Assignment ID: {}, Course ID: {}, Title: {}",
        assignmentId, courseId, title);

    try {
      // 1. Configurare criteri di assessment per questo assignment
      setupAssignmentAssessmentCriteria(assignmentId, courseId, teacherId, maxScore,
          assignmentType);

      // 2. Preparare template di feedback per questo tipo di assignment
      setupAssignmentFeedbackTemplate(assignmentId, assignmentType, description);

      // 3. Programmare reminder automatici per la scadenza (agli studenti)
      scheduleAssignmentDeadlineReminders(assignmentId, courseId, dueDate);

      // 4. Notificare il sistema di analytics della creazione
      updateCourseStatistics(courseId, "assignment_created", teacherId);

      // 5. Se necessario, creare assessment template per peer review
      if ("PEER_REVIEW".equals(assignmentType)) {
        setupPeerReviewAssessmentTemplate(assignmentId, courseId, maxScore);
      }

      logger.info("Assignment creation processed successfully for assignment: {} ({})", title,
          assignmentId);

    } catch (Exception e) {
      logger.error("Error processing assignment creation for assignment: {}", assignmentId, e);
      throw e;
    }
  }

  private void processAssignmentUpdated(Map<String, Object> message) {
    String assignmentId = getStringValue(message, "assignmentId");
    String courseId = getStringValue(message, "courseId");
    String teacherId = getStringValue(message, "teacherId");
    String title = getStringValue(message, "title");
    String description = getStringValue(message, "description");
    Long dueDate = getLongValue(message, "dueDate");
    Integer maxScore = getIntegerValue(message, "maxScore");
    String updateType = getStringValue(message, "updateType");

    logger.info("Processing assignment update - Assignment ID: {}, Course ID: {}, Update Type: {}",
        assignmentId, courseId, updateType);

    try {
      // 1. Aggiornare criteri di assessment se sono cambiati punteggio o tipo
      if ("MAX_SCORE_CHANGED".equals(updateType) || "CRITERIA_UPDATED".equals(updateType)) {
        updateAssignmentAssessmentCriteria(assignmentId, maxScore, description);
      }

      // 2. Gestire cambio di scadenza
      if ("DUE_DATE_CHANGED".equals(updateType)) {
        handleDueDateChange(assignmentId, courseId, dueDate);
      }

      // 3. Notificare studenti delle modifiche significative
      if (isSignificantUpdate(updateType)) {
        notificationService.notifyStudentsOfAssignmentUpdate(assignmentId, courseId, updateType,
            title);
      }

      // 4. Aggiornare template di feedback se necessario
      if ("DESCRIPTION_UPDATED".equals(updateType)) {
        updateAssignmentFeedbackTemplate(assignmentId, description);
      }

      // 5. Log per analytics
      updateCourseStatistics(courseId, "assignment_updated_" + updateType.toLowerCase(), teacherId);

      logger.info("Assignment update processed successfully for assignment: {} | Update: {}",
          assignmentId, updateType);

    } catch (Exception e) {
      logger.error("Error processing assignment update for assignment: {} | Update type: {}",
          assignmentId, updateType, e);
      throw e;
    }
  }

  // ===================================================================
  // UTILITY METHODS
  // ===================================================================

  private AssessmentDto createAssessmentFromAssignment(String assignmentId, String studentId,
      String courseId, String teacherId,
      Long submissionTime, String fileName) {
    AssessmentDto assessment = new AssessmentDto();
    assessment.setReferenceId(assignmentId);
    assessment.setReferenceType(ReferenceType.ASSIGNMENT);
    assessment.setStudentId(studentId);
    assessment.setCourseId(courseId);
    assessment.setTeacherId(teacherId);

    if (submissionTime != null) {
      assessment.setAssessmentDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(submissionTime),
          java.time.ZoneId.systemDefault()));
    } else {
      assessment.setAssessmentDate(LocalDateTime.now());
    }

    String notes = "Assignment submitted - awaiting evaluation";
    if (fileName != null) {
      notes += " | File: " + fileName;
    }
    assessment.setNotes(notes);
    assessment.setScore(0.0);

    return assessment;
  }

  private void scheduleAssessmentReminder(String teacherId, String assignmentId,
      String assessmentId) {
    logger.info(
        "📅 REMINDER SCHEDULED - Teacher: {} | Assignment: {} | Assessment: {} | Reminder in 48h",
        teacherId, assignmentId, assessmentId);

    // TODO: Implementare con @Scheduled o sistema di code delayed
    // scheduler.scheduleReminder(teacherId, assignmentId, Duration.ofHours(48));
  }

  private void updateCourseStatistics(String courseId, String eventType, String studentId) {
    logger.info("📊 STATS UPDATE - Course: {} | Event: {} | Student: {} | Timestamp: {}",
        courseId, eventType, studentId, LocalDateTime.now());

    // TODO
  }

  private void updateAssessmentWithSubmissionDetails(String assessmentId, String submissionContent,
      String fileUrl) {
    try {
      AssessmentDto assessment = assessmentService.getAssessmentById(assessmentId);
      if (assessment != null) {
        String updatedNotes = assessment.getNotes();

        if (submissionContent != null && submissionContent.length() > 50) {
          updatedNotes += " | Content preview: " + submissionContent.substring(0, 50) + "...";
        } else if (submissionContent != null) {
          updatedNotes += " | Content: " + submissionContent;
        }

        if (fileUrl != null) {
          updatedNotes += " | File URL: " + fileUrl;
        }

        assessment.setNotes(updatedNotes);
        assessmentService.updateAssessment(assessmentId, assessment);

        logger.info("Updated assessment {} with submission details", assessmentId);
      }
    } catch (Exception e) {
      logger.warn("Could not update assessment {} with submission details: {}", assessmentId,
          e.getMessage());
    }
  }

  private void setupAssignmentAssessmentCriteria(String assignmentId, String courseId,
      String teacherId,
      Integer maxScore, String assignmentType) {
    logger.info("🎯 SETUP ASSESSMENT CRITERIA - Assignment: {} | Type: {} | Max Score: {}",
        assignmentId, assignmentType, maxScore);

    // TODO
  }

  private void setupAssignmentFeedbackTemplate(String assignmentId, String assignmentType,
      String description) {
    logger.info("📝 SETUP FEEDBACK TEMPLATE - Assignment: {} | Type: {}",
        assignmentId, assignmentType);

    // TODO
  }

  private void scheduleAssignmentDeadlineReminders(String assignmentId, String courseId,
      Long dueDate) {
    if (dueDate != null) {
      logger.info("⏰ DEADLINE REMINDERS SCHEDULED - Assignment: {} | Due: {}",
          assignmentId,
          LocalDateTime.ofInstant(Instant.ofEpochMilli(dueDate), java.time.ZoneId.systemDefault()));

      // TODO
    }
  }

  private void setupPeerReviewAssessmentTemplate(String assignmentId, String courseId,
      Integer maxScore) {
    logger.info("👥 PEER REVIEW SETUP - Assignment: {} | Course: {} | Score: {}",
        assignmentId, courseId, maxScore);

    // TODO
  }

  private void updateAssignmentAssessmentCriteria(String assignmentId, Integer maxScore,
      String description) {
    logger.info("🔄 UPDATE ASSESSMENT CRITERIA - Assignment: {} | New Max Score: {}",
        assignmentId, maxScore);
  }

  private void handleDueDateChange(String assignmentId, String courseId, Long newDueDate) {
    logger.info("📅 DUE DATE CHANGED - Assignment: {} | New Due Date: {}",
        assignmentId, LocalDateTime.ofInstant(Instant.ofEpochMilli(newDueDate),
            java.time.ZoneId.systemDefault()));
    // TODO
  }

  private boolean isSignificantUpdate(String updateType) {
    return "DUE_DATE_CHANGED".equals(updateType)
        || "MAX_SCORE_CHANGED".equals(updateType)
        || "REQUIREMENTS_CHANGED".equals(updateType);
  }

  private void updateAssignmentFeedbackTemplate(String assignmentId, String description) {
    logger.info("📝 UPDATE FEEDBACK TEMPLATE - Assignment: {}", assignmentId);
  }

  // ===================================================================
  // METODI PER GESTIONE AVANZATA (future implementazioni)
  // ===================================================================

  /**
   * Analizza il contenuto della submission per estrarre metadata.
   */
  private void analyzeSubmissionContent(String assignmentId, String content, String fileName) {
    logger.info("🔍 CONTENT ANALYSIS - Assignment: {} | File: {} | Content length: {}",
        assignmentId, fileName, content != null ? content.length() : 0);
    // TODO
  }

  /**
   * Gestisce submission multiple dello stesso assignment.
   */
  private void handleDuplicateSubmission(String assignmentId, String studentId) {
    logger.warn("🔄 DUPLICATE SUBMISSION - Assignment: {} | Student: {} | Policy: Keep latest",
        assignmentId, studentId);
    // TODO
  }
}