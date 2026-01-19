package it.unimol.microserviceassessmentfeedback.messaging.consumers;

import it.unimol.microserviceassessmentfeedback.dto.AssessmentDto;
import it.unimol.microserviceassessmentfeedback.enums.ReferenceType;
import it.unimol.microserviceassessmentfeedback.service.AssessmentService;
import it.unimol.microserviceassessmentfeedback.service.events.NotificationService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Consumer responsabile della gestione degli eventi relativi agli esami.
 * Ascolta eventi di completamento dell’esame e registrazione del voto,
 * occupandosi della creazione e aggiornamento degli assessment,
 * dell’attivazione dei survey di feedback e dell’invio delle notifiche
 * a docenti e studenti.
 */
@Service
public class ExamConsumerService extends BaseEventConsumer {

  @Autowired
  private AssessmentService assessmentService;

  @Autowired
  private NotificationService notificationService;

  // ============ Costruttore ============

  // ============ Metodi Override ============
  @Override
  protected void handleMessage(Map<String, Object> message, String messageType) {
    switch (messageType) {
      case "EXAM_COMPLETED":
        processExamCompleted(message);
        break;
      case "EXAM_GRADE_REGISTERED":
        processExamGradeRegistered(message);
        break;
      default:
        logger.warn("Unknown message type: {}", messageType);
    }
  }

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  @RabbitListener(queues = "${rabbitmq.queue.examCompleted}")
  public void handleExamCompleted(Map<String, Object> message) {
    processMessage(message, "EXAM_COMPLETED");
  }

  @RabbitListener(queues = "${rabbitmq.queue.examGradeRegistered}")
  public void handleExamGradeRegistered(Map<String, Object> message) {
    processMessage(message, "EXAM_GRADE_REGISTERED");
  }

  private void processExamCompleted(Map<String, Object> message) {
    String examId = getStringValue(message, "examId");
    String studentId = getStringValue(message, "studentId");
    String courseId = getStringValue(message, "courseId");
    String teacherId = getStringValue(message, "teacherId");
    Long completionTime = getLongValue(message, "completionTime");
    Integer duration = getIntegerValue(message, "duration");
    String examType = getStringValue(message, "examType");

    logger.info("Processing exam completion - Exam ID: {}, Student ID: {}, Course ID: {}, Type: {}",
        examId, studentId, courseId, examType);

    try {
      // 1. Creare un assessment iniziale per tracciare l'esame completato
      AssessmentDto pendingAssessment = createPendingAssessmentFromExam(
          examId, studentId, courseId, teacherId, completionTime, examType, duration
      );
      AssessmentDto createdAssessment = assessmentService.createAssessment(pendingAssessment);
      logger.info("Created pending assessment with ID: {} for exam: {}",
          createdAssessment.getId(), examId);

      // 2. Notificare il docente che l'esame è stato completato e richiede correzione
      notificationService.notifyTeacherOfExamCompletion(teacherId, examId, studentId);

      // 3. Programmare il survey di feedback per l'esame (attivazione automatica)
      scheduleFeedbackSurvey(studentId, examId, courseId);

      // 4. Aggiornare statistiche del corso
      updateExamStatistics(courseId, examType, studentId, duration);

      logger.info("Exam completion processed successfully for exam: {} | Student: {}", examId,
          studentId);

    } catch (Exception e) {
      logger.error("Error processing exam completion for exam: {}", examId, e);
      throw e;
    }
  }

  private void processExamGradeRegistered(Map<String, Object> message) {
    String examId = getStringValue(message, "examId");
    String assessmentId = getStringValue(message, "assessmentId");
    String studentId = getStringValue(message, "studentId");
    String courseId = getStringValue(message, "courseId");
    String teacherId = getStringValue(message, "teacherId");
    Integer score = getIntegerValue(message, "score");
    Integer maxScore = getIntegerValue(message, "maxScore");
    String grade = getStringValue(message, "grade");
    String feedback = getStringValue(message, "feedback");
    Long gradeDate = getLongValue(message, "gradeDate");

    logger.info(
        "Processing exam grade registration - Exam ID: {}, Student ID: {}, Score: {}/{}, Grade: {}",
        examId, studentId, score, maxScore, grade);

    try {
      // 1. Aggiornare l'assessment esistente con il voto o crearne uno nuovo
      if (assessmentId != null) {
        updateAssessmentWithGrade(assessmentId, score, grade, feedback, gradeDate);
      } else {
        // Se non esiste assessment, crearne uno completo
        createAssessmentWithGrade(examId, studentId, courseId, teacherId,
            score, maxScore, grade, feedback, gradeDate);
      }

      // 2. Notificare lo studente del voto ricevuto
      notificationService.notifyStudentOfGrade(studentId, examId, grade, score, maxScore);

      // 3. Attivare il survey post-esame per il feedback dello studente
      notificationService.activatePostExamSurvey(studentId, examId, courseId);

      // 4. Se il voto è particolarmente alto o basso, inviare notifiche speciali
      handleSpecialGradeCases(studentId, teacherId, examId, score, maxScore, grade);

      logger.info("Exam grade registration processed successfully for exam: {} | Final grade: {}",
          examId, grade);

    } catch (Exception e) {
      logger.error("Error processing exam grade registration for exam: {}", examId, e);
      throw e;
    }
  }

  private AssessmentDto createPendingAssessmentFromExam(String examId, String studentId,
      String courseId,
      String teacherId, Long completionTime, String examType, Integer duration) {
    AssessmentDto assessment = new AssessmentDto();
    assessment.setReferenceId(examId);
    assessment.setReferenceType(ReferenceType.EXAM);
    assessment.setStudentId(studentId);
    assessment.setCourseId(courseId);
    assessment.setTeacherId(teacherId);

    // Imposta la data di completamento dell'esame
    if (completionTime != null) {
      assessment.setAssessmentDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(completionTime),
          java.time.ZoneId.systemDefault()));
    } else {
      assessment.setAssessmentDate(LocalDateTime.now(ZoneId.systemDefault()));
    }

    // Note iniziali con informazioni sull'esame
    String notes = String.format("Exam completed (%s) - awaiting grade",
        examType != null ? examType : "standard");
    if (duration != null) {
      notes += String.format(" | Duration: %d minutes", duration);
    }
    assessment.setNotes(notes);

    // Score temporaneo che indica "non ancora valutato"
    assessment.setScore(0.0);

    return assessment;
  }

  private void updateAssessmentWithGrade(String assessmentId, Integer score, String grade,
      String feedback, Long gradeDate) {
    try {
      AssessmentDto assessment = assessmentService.getAssessmentById(assessmentId);
      if (assessment != null) {
        // Aggiorna con il voto finale
        assessment.setScore(score != null ? score.doubleValue() : 0.0);

        // Aggiorna le note con il feedback
        String updatedNotes = assessment.getNotes().replace("awaiting grade", "graded");
        updatedNotes += String.format(" | Grade: %s", grade);

        if (feedback != null && !feedback.trim().isEmpty()) {
          if (feedback.length() > 100) {
            updatedNotes += " | Feedback: " + feedback.substring(0, 100) + "...";
          } else {
            updatedNotes += " | Feedback: " + feedback;
          }
        }

        assessment.setNotes(updatedNotes);

        // Aggiorna la data se fornita
        if (gradeDate != null) {
          assessment.setAssessmentDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(gradeDate),
              java.time.ZoneId.systemDefault()));
        }

        assessmentService.updateAssessment(assessmentId, assessment);
        logger.info("Updated assessment {} with grade: {} and score: {}", assessmentId, grade,
            score);
      }
    } catch (Exception e) {
      logger.error("Could not update assessment {} with grade: {}", assessmentId, e.getMessage());
    }
  }

  private void createAssessmentWithGrade(String examId, String studentId, String courseId,
      String teacherId,
      Integer score, Integer maxScore, String grade, String feedback, Long gradeDate) {
    AssessmentDto assessment = new AssessmentDto();
    assessment.setReferenceId(examId);
    assessment.setReferenceType(ReferenceType.EXAM);
    assessment.setStudentId(studentId);
    assessment.setCourseId(courseId);
    assessment.setTeacherId(teacherId);
    assessment.setScore(score != null ? score.doubleValue() : 0.0);

    // Data del voto
    if (gradeDate != null) {
      assessment.setAssessmentDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(gradeDate),
          java.time.ZoneId.systemDefault()));
    } else {
      assessment.setAssessmentDate(LocalDateTime.now(ZoneId.systemDefault()));
    }

    // Note complete con voto e feedback
    String notes = String.format("Exam graded | Grade: %s | Score: %d/%d",
        grade, score, maxScore != null ? maxScore : 100);

    if (feedback != null && !feedback.trim().isEmpty()) {
      if (feedback.length() > 100) {
        notes += " | Feedback: " + feedback.substring(0, 100) + "...";
      } else {
        notes += " | Feedback: " + feedback;
      }
    }

    assessment.setNotes(notes);

    assessmentService.createAssessment(assessment);
    logger.info("Created new assessment with grade for exam: {} | Grade: {}", examId, grade);
  }

  private void scheduleFeedbackSurvey(String studentId, String examId, String courseId) {
    logger.info("📋 Scheduling feedback survey for student: {} | Exam: {} | Course: {}",
        studentId, examId, courseId);

    // Simula la programmazione del survey
    notificationService.scheduleFeedbackSurvey(studentId, examId, courseId);
  }

  private void updateExamStatistics(String courseId, String examType, String studentId,
      Integer duration) {
    logger.info(
        "📊 EXAM STATS UPDATE - Course: {} | Type: {} | Student: {} | Duration: {} min | "
            + "Timestamp: {}",
        courseId, examType, studentId, duration, LocalDateTime.now(ZoneId.systemDefault()));

    // Simula aggiornamento statistiche
    logger.info("📈 Updated course completion rate for: {}", courseId);
    logger.info("📈 Updated average exam duration for type: {}", examType);
  }

  @SuppressWarnings("UnusedVariable")
  private void handleSpecialGradeCases(String studentId, String teacherId, String examId,
      Integer score, Integer maxScore, String grade) {
    if (score != null && maxScore != null) {
      double percentage = (double) score / maxScore * 100;

      if (percentage >= 95) {
        logger.info("🏆 EXCELLENT PERFORMANCE - Student: {} | Exam: {} | Score: {}%",
            studentId, examId, String.format("%.1f", percentage));
      } else if (percentage < 60) {
        logger.info("⚠️ LOW PERFORMANCE - Student: {} | Exam: {} | Score: {}%",
            studentId, examId, String.format("%.1f", percentage));
      }
    }
  }
}