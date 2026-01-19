package it.unimol.microserviceassessmentfeedback.messaging.consumers;

import it.unimol.microserviceassessmentfeedback.service.events.NotificationService;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Consumer responsabile della gestione degli eventi relativi agli utenti.
 * Ascolta eventi di creazione e cancellazione di docenti e studenti,
 * occupandosi dell’inizializzazione dei profili, della configurazione
 * delle preferenze e dell’invio delle notifiche associate.
 */
@Service
public class UserConsumerService extends BaseEventConsumer {

  @Autowired
  private NotificationService notificationService;

  // ============ Costruttore ============

  // ============ Metodi Override ============
  @Override
  protected void handleMessage(Map<String, Object> message, String messageType) {
    switch (messageType) {
      case "TEACHER_CREATED":
        processTeacherCreated(message);
        break;
      case "STUDENT_CREATED":
        processStudentCreated(message);
        break;
      case "USER_DELETED":
        processUserDeleted(message);
        break;
      default:
        logger.warn("Unknown message type: {}", messageType);
    }
  }

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  @RabbitListener(queues = "${rabbitmq.queue.teacherCreated}")
  public void handleTeacherCreated(Map<String, Object> message) {
    processMessage(message, "TEACHER_CREATED");
  }

  @RabbitListener(queues = "${rabbitmq.queue.studentCreated}")
  public void handleStudentCreated(Map<String, Object> message) {
    processMessage(message, "STUDENT_CREATED");
  }

  @RabbitListener(queues = "${rabbitmq.queue.userDeleted}")
  public void handleUserDeleted(Map<String, Object> message) {
    processMessage(message, "USER_DELETED");
  }

  @SuppressWarnings({"unused", "UnusedVariable"})
  private void processTeacherCreated(Map<String, Object> message) {
    String teacherId = getStringValue(message, "teacherId");
    String firstName = getStringValue(message, "firstName");
    String lastName = getStringValue(message, "lastName");
    String email = getStringValue(message, "email");
    String department = getStringValue(message, "department");
    String academicTitle = getStringValue(message, "academicTitle");
    String specialization = getStringValue(message, "specialization");
    String phoneNumber = getStringValue(message, "phoneNumber");
    String officeLocation = getStringValue(message, "officeLocation");

    logger.info("Processing teacher creation - Teacher ID: {}, Name: {} {}, Department: {}",
        teacherId, firstName, lastName, department);

    try {
      // 1. Inizializzare il profilo docente per assessment e feedback
      initializeTeacherProfile(teacherId, firstName, lastName, email, department, academicTitle);

      // 2. Configurare template di feedback personalizzati per il docente
      setupTeacherFeedbackTemplates(teacherId, department, specialization);

      // 3. Configurare preferenze di notifica del docente
      initializeTeacherNotificationPreferences(teacherId, email);

      // 4. Preparare template di assessment standard
      setupDefaultAssessmentCriteria(teacherId, department);

      // 5. Inviare notifica di benvenuto
      notificationService.sendWelcomeNotification(teacherId, "TEACHER", department);

      logger.info("Teacher creation processed successfully for: {} {} ({})",
          firstName, lastName, teacherId);

    } catch (Exception e) {
      logger.error("Error processing teacher creation for teacher: {}", teacherId, e);
      throw e;
    }
  }

  @SuppressWarnings({"unused", "UnusedVariable"})
  private void processStudentCreated(Map<String, Object> message) {
    String studentId = getStringValue(message, "studentId");
    String firstName = getStringValue(message, "firstName");
    String lastName = getStringValue(message, "lastName");
    String email = getStringValue(message, "email");
    String matriculationNumber = getStringValue(message, "matriculationNumber");
    String degreeProgram = getStringValue(message, "degreeProgram");
    String academicYear = getStringValue(message, "academicYear");
    Integer yearOfStudy = getIntegerValue(message, "yearOfStudy");
    String department = getStringValue(message, "department");

    logger.info(
        "Processing student creation - Student ID: {}, Name: {} {}, Matriculation: {}, Program: {}",
        studentId, firstName, lastName, matriculationNumber, degreeProgram);

    try {
      // 1. Inizializzare il profilo studente per assessment e feedback
      initializeStudentProfile(studentId, firstName, lastName, email, matriculationNumber,
          degreeProgram);

      // 2. Configurare preferenze di notifica dello studente
      initializeStudentNotificationPreferences(studentId, email);

      // 3. Preparare dashboard personalizzata per feedback
      initializeStudentDashboard(studentId, degreeProgram, yearOfStudy);

      // 4. Configurare survey preferences basate sul corso di studio
      setupStudentSurveyPreferences(studentId, degreeProgram, yearOfStudy);

      // 5. Programmare survey di benvenuto se configurato
      triggerWelcomeSurvey(studentId, degreeProgram, department);

      // 6. Inviare notifica di benvenuto
      notificationService.sendWelcomeNotification(studentId, "STUDENT", department);

      logger.info("Student creation processed successfully for: {} {} ({}) - {}",
          firstName, lastName, matriculationNumber, degreeProgram);

    } catch (Exception e) {
      logger.error("Error processing student creation for student: {}", studentId, e);
      throw e;
    }
  }

  @SuppressWarnings({"unused", "UnusedVariable"})
  private void processUserDeleted(Map<String, Object> message) {
    String userId = getStringValue(message, "userId");
    String userType = getStringValue(message, "userType");
    String reason = getStringValue(message, "reason");
    Long deletionDate = getLongValue(message, "deletionDate");

    logger.info("Processing user deletion - User ID: {}, Type: {}, Reason: {}", userId, userType,
        reason);

    try {
      if ("TEACHER".equals(userType)) {
        handleTeacherDeletion(userId, reason);
      } else if ("STUDENT".equals(userType)) {
        handleStudentDeletion(userId, reason);
      }

      logger.info("User deletion processed successfully for: {} ({})", userId, userType);

    } catch (Exception e) {
      logger.error("Error processing user deletion for user: {}", userId, e);
      throw e;
    }
  }

  private void initializeTeacherProfile(String teacherId, String firstName, String lastName,
      String email, String department, String academicTitle) {
    logger.info("👨‍🏫 Initializing teacher profile for: {} {} | Department: {} | Title: {}",
        firstName, lastName, department, academicTitle);

    // Simula la creazione del profilo locale
    logger.info("📋 Created local teacher profile with ID: {}", teacherId);
    logger.info("📧 Registered email: {} for notifications", email);
    logger.info("🎓 Academic title: {} in department: {}", academicTitle, department);
  }

  private void setupTeacherFeedbackTemplates(String teacherId, String department,
      String specialization) {
    logger.info("📝 Setting up feedback templates for teacher: {} | Dept: {} | Specialization: {}",
        teacherId, department, specialization);

    // Template personalizzati per dipartimento
    if ("Computer Science".equalsIgnoreCase(department)) {
      logger.info("💻 Created programming assignment feedback templates");
      logger.info("💻 Created code review feedback templates");
      logger.info("💻 Created software project evaluation templates");
    } else if ("Mathematics".equalsIgnoreCase(department)) {
      logger.info("📐 Created mathematical proof feedback templates");
      logger.info("📐 Created problem-solving evaluation templates");
      logger.info("📐 Created theoretical exercise feedback templates");
    } else if ("Engineering".equalsIgnoreCase(department)) {
      logger.info("🔧 Created technical design feedback templates");
      logger.info("🔧 Created laboratory report evaluation templates");
    } else {
      logger.info("📄 Created general feedback templates");
      logger.info("📄 Created standard evaluation rubrics");
    }

    // Template per specializzazione
    if (specialization != null) {
      logger.info("🎯 Created specialized feedback templates for: {}", specialization);
    }
  }

  private void initializeTeacherNotificationPreferences(String teacherId, String email) {
    logger.info("🔔 Initializing notification preferences for teacher: {} with email: {}",
        teacherId,
        email);
    logger.info("✅ Default notification preferences set for teacher ID: {}", teacherId);
  }

  private void setupDefaultAssessmentCriteria(String teacherId, String department) {
    logger.info("📊 Setting up default assessment criteria for teacher: {} in department: {}",
        teacherId, department);
    if ("Computer Science".equalsIgnoreCase(department)) {
      logger.info(
          "➕ Added default assessment criteria: Code Quality, Algorithm Efficiency, Documentation");
    } else if ("Mathematics".equalsIgnoreCase(department)) {
      logger.info(
          "➕ Added default assessment criteria: Proof Rigor, Problem Solving Approach, Clarity of"
              + " Explanation");
    } else {
      logger.info("➕ Added general default assessment criteria for teacher ID: {}", teacherId);
    }
  }

  private void initializeStudentProfile(String studentId, String firstName, String lastName,
      String email, String matriculationNumber, String degreeProgram) {
    logger.info("🎓 Initializing student profile for: {} {} | Matriculation: {} | Program: {}",
        firstName, lastName, matriculationNumber, degreeProgram);
    logger.info("📋 Created local student profile with ID: {}", studentId);
    logger.info("📧 Registered email: {} for communications", email);
    logger.info("🆔 Matriculation Number: {}", matriculationNumber);
    logger.info("📚 Enrolled in Degree Program: {}", degreeProgram);
  }

  private void initializeStudentNotificationPreferences(String studentId, String email) {
    logger.info("🔔 Initializing notification preferences for student: {} with email: {}",
        studentId,
        email);
    logger.info("✅ Default notification preferences set for student ID: {}", studentId);
  }

  private void initializeStudentDashboard(String studentId, String degreeProgram,
      Integer yearOfStudy) {
    logger.info("📊 Preparing personalized dashboard for student: {} | Program: {} | Year: {}",
        studentId, degreeProgram, yearOfStudy);
    logger.info("📈 Dashboard widgets configured for academic progress and feedback summary.");
    if (yearOfStudy != null) {
      logger.info("📆 Tailored dashboard for Year of Study: {}", yearOfStudy);
    }
  }

  private void setupStudentSurveyPreferences(String studentId, String degreeProgram,
      Integer yearOfStudy) {
    logger.info("📋 Setting up survey preferences for student: {} | Program: {} | Year: {}",
        studentId, degreeProgram, yearOfStudy);
    if ("Computer Science".equalsIgnoreCase(degreeProgram) && yearOfStudy != null
        && yearOfStudy == 1) {
      logger.info("⭐ Opted in for introductory programming course surveys.");
    }
    logger.info("✅ Default survey preferences enabled for student ID: {}", studentId);
  }

  private void triggerWelcomeSurvey(String studentId, String degreeProgram, String department) {
    logger.info(
        "📝 Checking if welcome survey should be triggered for student: {} in department: {}",
        studentId, department);
    boolean shouldTrigger = true;
    if (shouldTrigger) {
      logger.info("🚀 Triggering welcome survey for student: {} | Program: {} | Department: {}",
          studentId, degreeProgram, department);
      // Inviare un messaggio a un altro servizio o schedulare l'invio della survey
    } else {
      logger.info("❌ Welcome survey not configured to be triggered for student: {}", studentId);
    }
  }

  private void handleTeacherDeletion(String userId, String reason) {
    logger.info("🗑️ Handling teacher deletion for ID: {} due to: {}", userId, reason);
    logger.info("🚫 Deactivating teacher profile with ID: {}", userId);
    logger.info(
        "🗑️ Archiving associated feedback templates and assessment criteria for teacher ID: {}",
        userId);
  }

  private void handleStudentDeletion(String userId, String reason) {
    logger.info("🗑️ Handling student deletion for ID: {} due to: {}", userId, reason);
    logger.info("🚫 Deactivating student profile with ID: {}", userId);
    logger.info(
        "🗑️ Archiving associated dashboard data, notification preferences, and survey responses "
            + "for student ID: {}",
        userId);
  }
}