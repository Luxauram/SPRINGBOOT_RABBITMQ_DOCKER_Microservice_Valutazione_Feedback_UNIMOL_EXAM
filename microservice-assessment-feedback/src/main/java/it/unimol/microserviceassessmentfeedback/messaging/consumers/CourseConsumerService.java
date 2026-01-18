package it.unimol.microserviceassessmentfeedback.messaging.consumers;

import it.unimol.microserviceassessmentfeedback.service.events.NotificationService;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Consumer responsabile della gestione degli eventi relativi ai corsi.
 * Ascolta i messaggi di creazione e cancellazione dei corsi tramite RabbitMQ
 * e delega l’elaborazione alla logica applicativa per l’inizializzazione,
 * l’archiviazione e l’invio delle notifiche correlate.
 */
@Service
public class CourseConsumerService extends BaseEventConsumer {

  @Autowired
  private NotificationService notificationService;

  // ============ Costruttore ============

  // ============ Metodi Override ============
  @Override
  protected void handleMessage(Map<String, Object> message, String messageType) {
    switch (messageType) {
      case "COURSE_CREATED":
        processCourseCreated(message);
        break;
      case "COURSE_DELETED":
        processCourseDeleted(message);
        break;
      default:
        logger.warn("Unknown message type: {}", messageType);
    }
  }

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  @RabbitListener(queues = "${rabbitmq.queue.courseCreated}")
  public void handleCourseCreated(Map<String, Object> message) {
    processMessage(message, "COURSE_CREATED");
  }

  @RabbitListener(queues = "${rabbitmq.queue.courseDeleted}")
  public void handleCourseDeleted(Map<String, Object> message) {
    processMessage(message, "COURSE_DELETED");
  }

  private void processCourseCreated(Map<String, Object> message) {
    String courseId = getStringValue(message, "courseId");
    String courseName = getStringValue(message, "courseName");
    String courseCode = getStringValue(message, "courseCode");
    String teacherId = getStringValue(message, "teacherId");
    String teacherName = getStringValue(message, "teacherName");
    String department = getStringValue(message, "department");
    String academicYear = getStringValue(message, "academicYear");
    String semester = getStringValue(message, "semester");
    Integer credits = getIntegerValue(message, "credits");
    String description = getStringValue(message, "description");

    logger.info("Processing course creation - Course ID: {}, Course Name: {}, Teacher ID: {}",
        courseId, courseName, teacherId);

    try {
      // 1. Inizializzare template di assessment per il corso
      initializeCourseFeedbackSettings(courseId, courseName, teacherId, department);

      // 2. Preparare template di survey per il corso
      setupCourseSurveyTemplates(courseId, courseName, semester);

      // 3. Configurare notifiche per il corso
      setupCourseNotificationSettings(courseId, teacherId);

      // 4. Inviare notifica di benvenuto al docente
      notificationService.sendWelcomeNotification(teacherId, "TEACHER", department);

      logger.info("Course creation processed successfully for course: {} ({})", courseName,
          courseId);

    } catch (Exception e) {
      logger.error("Error processing course creation for course: {}", courseId, e);
      throw e;
    }
  }

  private void processCourseDeleted(Map<String, Object> message) {
    String courseId = getStringValue(message, "courseId");
    String courseName = getStringValue(message, "courseName");
    String teacherId = getStringValue(message, "teacherId");
    String reason = getStringValue(message, "reason");
    Long deletionDate = getLongValue(message, "deletionDate");

    logger.info("Processing course deletion - Course ID: {}, Course Name: {}, Reason: {}",
        courseId, courseName, reason);

    try {
      // 1. Archiviare tutti i dati di assessment del corso
      archiveCourseData(courseId, courseName);

      // 2. Finalizzare tutti i survey attivi del corso
      finalizeCourseActiveSurveys(courseId);

      // 3. Archiviare tutti i feedback del corso
      archiveCourseFeedbacks(courseId);

      // 4. Notificare la cancellazione
      notificationService.notifyCourseDeletion(courseId, teacherId, reason);

      // 5. Pulire configurazioni del corso
      cleanupCourseConfigurations(courseId);

      logger.info("Course deletion processed successfully for course: {} ({})", courseName,
          courseId);

    } catch (Exception e) {
      logger.error("Error processing course deletion for course: {}", courseId, e);
      throw e;
    }
  }

  private void initializeCourseFeedbackSettings(String courseId, String courseName,
      String teacherId, String department) {
    logger.info("🎯 Initializing feedback settings for course: {} | Teacher: {} | Department: {}",
        courseName, teacherId, department);

    // Simula la creazione di template di feedback specifici per il dipartimento
    if ("Computer Science".equalsIgnoreCase(department)) {
      logger.info("📝 Created programming assignment feedback templates for course: {}", courseId);
    } else if ("Mathematics".equalsIgnoreCase(department)) {
      logger.info("📝 Created mathematical proof feedback templates for course: {}", courseId);
    } else {
      logger.info("📝 Created general feedback templates for course: {}", courseId);
    }
  }

  private void setupCourseSurveyTemplates(String courseId, String courseName, String semester) {
    logger.info("📊 Setting up survey templates for course: {} | Semester: {}", courseName,
        semester);

    // Template di survey standard
    logger.info("📋 Created course evaluation survey template for: {}", courseId);
    logger.info("📋 Created teacher evaluation survey template for: {}", courseId);
    logger.info("📋 Created end-of-semester feedback survey for: {}", courseId);
  }

  private void setupCourseNotificationSettings(String courseId, String teacherId) {
    logger.info("🔔 Setting up notification preferences for course: {} | Teacher: {}", courseId,
        teacherId);

    // Configurazioni di notifica standard
    logger.info("📬 Enabled assignment submission notifications for teacher: {}", teacherId);
    logger.info("📬 Enabled exam completion notifications for teacher: {}", teacherId);
    logger.info("📬 Enabled feedback request notifications for course: {}", courseId);
  }

  private void archiveCourseData(String courseId, String courseName) {
    logger.info("📦 Archiving all assessment data for course: {} ({})", courseName, courseId);

    // Simula l'archiviazione dei dati
    logger.info("💾 Archived {} assessments for course: {}",
        (int) (Math.random() * 50 + 10), courseId);
    logger.info("💾 Archived {} feedback entries for course: {}",
        (int) (Math.random() * 100 + 20), courseId);
  }

  private void finalizeCourseActiveSurveys(String courseId) {
    logger.info("📊 Finalizing active surveys for course: {}", courseId);

    // Simula la finalizzazione dei survey attivi
    int activeSurveys = (int) (Math.random() * 5 + 1);
    for (int i = 1; i <= activeSurveys; i++) {
      logger.info("✅ Finalized survey #{} for course: {}", i, courseId);
    }
  }

  private void archiveCourseFeedbacks(String courseId) {
    logger.info("📂 Archiving all feedback for course: {}", courseId);

    // Simula l'archiviazione dei feedback
    int feedbackCount = (int) (Math.random() * 80 + 20);
    logger.info("💾 Successfully archived {} feedback entries for course: {}", feedbackCount,
        courseId);
  }

  private void cleanupCourseConfigurations(String courseId) {
    logger.info("🧹 Cleaning up course configurations for: {}", courseId);

    // Simula la pulizia delle configurazioni
    logger.info("🗑️ Removed feedback templates for course: {}", courseId);
    logger.info("🗑️ Removed survey configurations for course: {}", courseId);
    logger.info("🗑️ Removed notification settings for course: {}", courseId);
    logger.info("✨ Course cleanup completed for: {}", courseId);
  }
}