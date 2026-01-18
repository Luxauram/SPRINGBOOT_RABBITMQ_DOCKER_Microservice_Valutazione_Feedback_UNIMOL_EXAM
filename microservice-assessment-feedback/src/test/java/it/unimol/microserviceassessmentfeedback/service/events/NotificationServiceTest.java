package it.unimol.microserviceassessmentfeedback.service.events;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class NotificationServiceTest {

  private NotificationService notificationService;
  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @BeforeEach
  void setUp() {
    notificationService = new NotificationService();

    // Configura il logger per catturare i log
    logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @AfterEach
  void tearDown() {
    listAppender.stop();
    logger.detachAppender(listAppender);
  }

  // ===================================================================
  // TEST NOTIFICHE DOCENTI
  // ===================================================================

  @Test
  void testNotifyTeacherOfSubmission() {
    String teacherId = "teacher123";
    String assignmentId = "assignment456";
    String studentId = "student789";

    notificationService.notifyTeacherOfSubmission(teacherId, assignmentId, studentId);

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("NOTIFICATION [TEACHER]"));
    assertTrue(logMessage.contains("NUOVA CONSEGNA"));
    assertTrue(logMessage.contains(teacherId));
    assertTrue(logMessage.contains(assignmentId));
    assertTrue(logMessage.contains(studentId));
    assertTrue(logMessage.contains("Valutazione"));
  }

  @Test
  void testNotifyTeacherOfExamCompletion() {
    String teacherId = "teacher123";
    String examId = "exam456";
    String studentId = "student789";

    notificationService.notifyTeacherOfExamCompletion(teacherId, examId, studentId);

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("NOTIFICATION [TEACHER]"));
    assertTrue(logMessage.contains("ESAME COMPLETATO"));
    assertTrue(logMessage.contains(teacherId));
    assertTrue(logMessage.contains(examId));
    assertTrue(logMessage.contains(studentId));
    assertTrue(logMessage.contains("Correzione"));
  }

  @Test
  void testSendAssessmentReminder() {
    String teacherId = "teacher123";
    String assignmentId = "assignment456";

    notificationService.sendAssessmentReminder(teacherId, assignmentId);

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("REMINDER [TEACHER]"));
    assertTrue(logMessage.contains("PROMEMORIA"));
    assertTrue(logMessage.contains(teacherId));
    assertTrue(logMessage.contains(assignmentId));
    assertTrue(logMessage.contains("48h"));
  }

  // ===================================================================
  // TEST NOTIFICHE STUDENTI
  // ===================================================================

  @Test
  void testNotifyStudentOfFeedback() {
    String studentId = "student123";
    String assessmentId = "assessment456";
    String courseId = "course789";

    notificationService.notifyStudentOfFeedback(studentId, assessmentId, courseId);

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("NOTIFICATION [STUDENT]"));
    assertTrue(logMessage.contains("NUOVO FEEDBACK"));
    assertTrue(logMessage.contains(studentId));
    assertTrue(logMessage.contains(assessmentId));
    assertTrue(logMessage.contains(courseId));
  }

  @Test
  void testNotifyStudentOfGrade() {
    String studentId = "student123";
    String examId = "exam456";
    String grade = "A";
    Integer score = 28;
    Integer maxScore = 30;

    notificationService.notifyStudentOfGrade(studentId, examId, grade, score, maxScore);

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("NOTIFICATION [STUDENT]"));
    assertTrue(logMessage.contains("NUOVO VOTO"));
    assertTrue(logMessage.contains(studentId));
    assertTrue(logMessage.contains(examId));
    assertTrue(logMessage.contains(grade));
    assertTrue(logMessage.contains("28/30"));
  }

  @Test
  void testNotifyStudentOfSurvey() {
    String studentId = "student123";
    String surveyId = "survey456";
    String courseId = "course789";
    String surveyType = "POST_EXAM";

    notificationService.notifyStudentOfSurvey(studentId, surveyId, courseId, surveyType);

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("NOTIFICATION [STUDENT]"));
    assertTrue(logMessage.contains("NUOVO SURVEY"));
    assertTrue(logMessage.contains(studentId));
    assertTrue(logMessage.contains(surveyId));
    assertTrue(logMessage.contains(courseId));
    assertTrue(logMessage.contains(surveyType));
    assertTrue(logMessage.contains("7 giorni"));
  }

  @Test
  void testNotifyStudentsOfAssignmentUpdate_DueDateChanged() {
    String assignmentId = "assignment123";
    String courseId = "course456";
    String updateType = "DUE_DATE_CHANGED";
    String title = "Assignment Matematica";

    notificationService.notifyStudentsOfAssignmentUpdate(assignmentId, courseId, updateType, title);

    // Verifica che ci siano 2 log (il messaggio principale + il warning specifico)
    assertTrue(listAppender.list.size() >= 2);

    String firstLog = listAppender.list.get(0).getFormattedMessage();
    assertTrue(firstLog.contains("NOTIFICATION [ASSIGNMENT_UPDATE]"));
    assertTrue(firstLog.contains("ASSIGNMENT MODIFICATO"));
    assertTrue(firstLog.contains(assignmentId));
    assertTrue(firstLog.contains(courseId));
    assertTrue(firstLog.contains(title));
    assertTrue(firstLog.contains("Scadenza modificata"));

    String secondLog = listAppender.list.get(1).getFormattedMessage();
    assertTrue(secondLog.contains("SCADENZA MODIFICATA"));
  }

  @Test
  void testNotifyStudentsOfAssignmentUpdate_MaxScoreChanged() {
    String assignmentId = "assignment123";
    String courseId = "course456";
    String updateType = "MAX_SCORE_CHANGED";
    String title = "Assignment Fisica";

    notificationService.notifyStudentsOfAssignmentUpdate(assignmentId, courseId, updateType, title);

    assertTrue(listAppender.list.size() >= 2);

    String firstLog = listAppender.list.get(0).getFormattedMessage();
    assertTrue(firstLog.contains("Punteggio massimo modificato"));

    String secondLog = listAppender.list.get(1).getFormattedMessage();
    assertTrue(secondLog.contains("PUNTEGGIO MODIFICATO"));
  }

  @Test
  void testNotifyStudentsOfAssignmentUpdate_RequirementsChanged() {
    String assignmentId = "assignment123";
    String courseId = "course456";
    String updateType = "REQUIREMENTS_CHANGED";
    String title = "Assignment Chimica";

    notificationService.notifyStudentsOfAssignmentUpdate(assignmentId, courseId, updateType, title);

    assertTrue(listAppender.list.size() >= 2);

    String firstLog = listAppender.list.get(0).getFormattedMessage();
    assertTrue(firstLog.contains("Requisiti aggiornati"));

    String secondLog = listAppender.list.get(1).getFormattedMessage();
    assertTrue(secondLog.contains("REQUISITI MODIFICATI"));
  }

  @Test
  void testNotifyStudentsOfAssignmentUpdate_DescriptionUpdated() {
    String assignmentId = "assignment123";
    String courseId = "course456";
    String updateType = "DESCRIPTION_UPDATED";
    String title = "Assignment Storia";

    notificationService.notifyStudentsOfAssignmentUpdate(assignmentId, courseId, updateType, title);

    assertTrue(listAppender.list.size() >= 2);

    String firstLog = listAppender.list.get(0).getFormattedMessage();
    assertTrue(firstLog.contains("Descrizione aggiornata"));
  }

  @Test
  void testNotifyStudentsOfAssignmentUpdate_CriteriaUpdated() {
    String assignmentId = "assignment123";
    String courseId = "course456";
    String updateType = "CRITERIA_UPDATED";
    String title = "Assignment Filosofia";

    notificationService.notifyStudentsOfAssignmentUpdate(assignmentId, courseId, updateType, title);

    assertTrue(listAppender.list.size() >= 2);

    String firstLog = listAppender.list.get(0).getFormattedMessage();
    assertTrue(firstLog.contains("Criteri di valutazione aggiornati"));
  }

  @Test
  void testNotifyStudentsOfAssignmentUpdate_GenericUpdate() {
    String assignmentId = "assignment123";
    String courseId = "course456";
    String updateType = "OTHER_UPDATE";
    String title = "Assignment Generale";

    notificationService.notifyStudentsOfAssignmentUpdate(assignmentId, courseId, updateType, title);

    assertTrue(listAppender.list.size() >= 2);

    String firstLog = listAppender.list.get(0).getFormattedMessage();
    assertTrue(firstLog.contains("other update"));

    String secondLog = listAppender.list.get(1).getFormattedMessage();
    assertTrue(secondLog.contains("MODIFICA GENERICA"));
  }

  // ===================================================================
  // TEST NOTIFICHE GENERICHE
  // ===================================================================

  @Test
  void testNotifyCourseDeletion() {
    String courseId = "course123";
    String teacherId = "teacher456";
    String reason = "Corso non più attivo";

    notificationService.notifyCourseDeletion(courseId, teacherId, reason);

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("NOTIFICATION [COURSE_DELETION]"));
    assertTrue(logMessage.contains("CORSO CANCELLATO"));
    assertTrue(logMessage.contains(courseId));
    assertTrue(logMessage.contains(teacherId));
    assertTrue(logMessage.contains(reason));
    assertTrue(logMessage.contains("archiviati"));
  }

  @Test
  void testSendWelcomeNotification() {
    String userId = "user123";
    String userType = "STUDENT";
    String department = "Informatica";

    notificationService.sendWelcomeNotification(userId, userType, department);

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("NOTIFICATION [WELCOME]"));
    assertTrue(logMessage.contains("BENVENUTO"));
    assertTrue(logMessage.contains(userId));
    assertTrue(logMessage.contains(userType));
    assertTrue(logMessage.contains(department));
  }

  // ===================================================================
  // TEST SURVEY NOTIFICATIONS
  // ===================================================================

  @Test
  void testActivatePostExamSurvey() {
    String studentId = "student123";
    String examId = "exam456";
    String courseId = "course789";

    notificationService.activatePostExamSurvey(studentId, examId, courseId);

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("NOTIFICATION [SURVEY]"));
    assertTrue(logMessage.contains("SURVEY POST-ESAME"));
    assertTrue(logMessage.contains(studentId));
    assertTrue(logMessage.contains(examId));
    assertTrue(logMessage.contains(courseId));
  }

  @Test
  void testScheduleFeedbackSurvey() {
    String studentId = "student123";
    String examId = "exam456";
    String courseId = "course789";

    notificationService.scheduleFeedbackSurvey(studentId, examId, courseId);

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("NOTIFICATION [SURVEY_SCHEDULED]"));
    assertTrue(logMessage.contains("SURVEY FEEDBACK"));
    assertTrue(logMessage.contains(studentId));
    assertTrue(logMessage.contains(examId));
    assertTrue(logMessage.contains(courseId));
    assertTrue(logMessage.contains("Programmato"));
  }

  // ===================================================================
  // TEST UTILITY METHODS
  // ===================================================================

  @Test
  void testLogNotificationStats() {
    notificationService.logNotificationStats();

    assertEquals(2, listAppender.list.size());
    String firstLog = listAppender.list.get(0).getFormattedMessage();
    String secondLog = listAppender.list.get(1).getFormattedMessage();

    assertTrue(firstLog.contains("NOTIFICATION STATS"));
    assertTrue(firstLog.contains("NotificationService attivo"));
    assertTrue(secondLog.contains("tracking delle notifiche"));
  }

  // ===================================================================
  // TEST EDGE CASES
  // ===================================================================

  @Test
  void testNotificationWithNullValues() {
    // Verifica che il servizio gestisca valori null senza crashare
    assertDoesNotThrow(() ->
        notificationService.notifyTeacherOfSubmission(null, null, null));

    assertEquals(1, listAppender.list.size());
    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("NOTIFICATION [TEACHER]"));
  }

  @Test
  void testNotificationWithEmptyStrings() {
    assertDoesNotThrow(() ->
        notificationService.notifyStudentOfFeedback("", "", ""));

    assertEquals(1, listAppender.list.size());
  }

  @Test
  void testMultipleNotificationsInSequence() {
    notificationService.notifyTeacherOfSubmission("t1", "a1", "s1");
    notificationService.notifyStudentOfFeedback("s1", "a1", "c1");
    notificationService.sendWelcomeNotification("u1", "STUDENT", "CS");

    assertEquals(3, listAppender.list.size());
  }

  @Test
  void testTimestampFormat() {
    notificationService.notifyTeacherOfSubmission("teacher1", "assign1", "student1");

    String logMessage = listAppender.list.get(0).getFormattedMessage();
    // Verifica che contenga un timestamp nel formato corretto (yyyy-MM-dd HH:mm:ss)
    assertTrue(logMessage.matches(".*\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\].*"));
  }

  @Test
  void testNotifyStudentOfGrade_WithZeroScore() {
    notificationService.notifyStudentOfGrade("student1", "exam1", "F", 0, 30);

    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("0/30"));
  }

  @Test
  void testNotifyStudentOfGrade_WithPerfectScore() {
    notificationService.notifyStudentOfGrade("student1", "exam1", "A+", 30, 30);

    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("30/30"));
  }

  @Test
  void testFormatUpdateType_WithUnderscores() {
    // Testa indirettamente il metodo privato formatUpdateType
    notificationService.notifyStudentsOfAssignmentUpdate("a1", "c1", "CUSTOM_UPDATE_TYPE", "Test");

    String logMessage = listAppender.list.get(0).getFormattedMessage();
    assertTrue(logMessage.contains("custom update type"));
  }
}