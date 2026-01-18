package it.unimol.microserviceassessmentfeedback.messaging.consumers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import it.unimol.microserviceassessmentfeedback.service.events.NotificationService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseConsumerServiceTest {

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private CourseConsumerService courseConsumerService;

  private Map<String, Object> testMessage;

  @BeforeEach
  void setUp() {
    testMessage = new HashMap<>();
  }

  // ===================================================================
  // TEST COURSE CREATED
  // ===================================================================

  @Test
  void testHandleCourseCreated() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Introduction to Java");
    testMessage.put("courseCode", "CS101");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("teacherName", "Prof. Smith");
    testMessage.put("department", "Computer Science");
    testMessage.put("academicYear", "2023-2024");
    testMessage.put("semester", "1");
    testMessage.put("credits", 6);
    testMessage.put("description", "An introductory course");

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher001", "TEACHER", "Computer Science");
  }

  @Test
  void testHandleCourseCreated_ComputerScienceDepartment() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Data Structures");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("department", "Computer Science");

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher001", "TEACHER", "Computer Science");
  }

  @Test
  void testHandleCourseCreated_MathematicsDepartment() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Calculus I");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("department", "Mathematics");

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher001", "TEACHER", "Mathematics");
  }

  @Test
  void testHandleCourseCreated_OtherDepartment() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "History 101");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("department", "History");

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher001", "TEACHER", "History");
  }

  @Test
  void testHandleCourseCreated_NullDepartment() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Test Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("department", null);

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher001", "TEACHER", null);
  }

  @Test
  void testHandleCourseCreated_NullSemester() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Test Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("department", "CS");
    testMessage.put("semester", null);

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher001", "TEACHER", "CS");
  }

  // ===================================================================
  // TEST COURSE DELETED
  // ===================================================================

  @Test
  void testHandleCourseDeleted() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Old Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("reason", "Course no longer offered");
    testMessage.put("deletionDate", System.currentTimeMillis());

    courseConsumerService.handleCourseDeleted(testMessage);

    verify(notificationService).notifyCourseDeletion(
        "course123", "teacher001", "Course no longer offered");
  }

  @Test
  void testHandleCourseDeleted_DifferentReasons() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Discontinued Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("reason", "Low enrollment");

    courseConsumerService.handleCourseDeleted(testMessage);

    verify(notificationService).notifyCourseDeletion(
        "course123", "teacher001", "Low enrollment");
  }

  @Test
  void testHandleCourseDeleted_NullReason() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Test Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("reason", null);

    courseConsumerService.handleCourseDeleted(testMessage);

    verify(notificationService).notifyCourseDeletion("course123", "teacher001", null);
  }

  @Test
  void testHandleCourseDeleted_NullDeletionDate() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Test Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("reason", "Test reason");
    testMessage.put("deletionDate", null);

    courseConsumerService.handleCourseDeleted(testMessage);

    verify(notificationService).notifyCourseDeletion("course123", "teacher001", "Test reason");
  }

  // ===================================================================
  // TEST HANDLE MESSAGE
  // ===================================================================

  @Test
  void testHandleMessage_CourseCreated() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Test Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("department", "CS");

    courseConsumerService.handleMessage(testMessage, "COURSE_CREATED");

    verify(notificationService).sendWelcomeNotification("teacher001", "TEACHER", "CS");
  }

  @Test
  void testHandleMessage_CourseDeleted() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Test Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("reason", "Test");

    courseConsumerService.handleMessage(testMessage, "COURSE_DELETED");

    verify(notificationService).notifyCourseDeletion("course123", "teacher001", "Test");
  }

  @Test
  void testHandleMessage_UnknownType() {
    testMessage.put("courseId", "course123");

    courseConsumerService.handleMessage(testMessage, "UNKNOWN_TYPE");

    verify(notificationService, never()).sendWelcomeNotification(anyString(), anyString(), anyString());
    verify(notificationService, never()).notifyCourseDeletion(anyString(), anyString(), anyString());
  }

  // ===================================================================
  // TEST EDGE CASES
  // ===================================================================

  @Test
  void testHandleCourseCreated_MinimalData() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Minimal Course");
    testMessage.put("teacherId", "teacher001");

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher001", "TEACHER", null);
  }

  @Test
  void testHandleCourseDeleted_MinimalData() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Minimal Course");
    testMessage.put("teacherId", "teacher001");

    courseConsumerService.handleCourseDeleted(testMessage);

    verify(notificationService).notifyCourseDeletion("course123", "teacher001", null);
  }

  @Test
  void testHandleCourseCreated_CompleteData() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Complete Course");
    testMessage.put("courseCode", "CS999");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("teacherName", "Prof. Complete");
    testMessage.put("department", "Computer Science");
    testMessage.put("academicYear", "2024-2025");
    testMessage.put("semester", "2");
    testMessage.put("credits", 9);
    testMessage.put("description", "A complete course with all fields");

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification(
        "teacher001", "TEACHER", "Computer Science");
  }

  @Test
  void testHandleCourseDeleted_CompleteData() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Complete Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("reason", "Complete deletion with all data");
    testMessage.put("deletionDate", System.currentTimeMillis());

    courseConsumerService.handleCourseDeleted(testMessage);

    verify(notificationService).notifyCourseDeletion(
        "course123", "teacher001", "Complete deletion with all data");
  }

  @Test
  void testHandleCourseCreated_EmptyStrings() {
    testMessage.put("courseId", "");
    testMessage.put("courseName", "");
    testMessage.put("teacherId", "");
    testMessage.put("department", "");

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("", "TEACHER", "");
  }

  @Test
  void testHandleCourseDeleted_EmptyStrings() {
    testMessage.put("courseId", "");
    testMessage.put("courseName", "");
    testMessage.put("teacherId", "");
    testMessage.put("reason", "");

    courseConsumerService.handleCourseDeleted(testMessage);

    verify(notificationService).notifyCourseDeletion("", "", "");
  }

  @Test
  void testHandleCourseCreated_CaseInsensitiveDepartment() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Test Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("department", "COMPUTER SCIENCE"); // Uppercase

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification(
        "teacher001", "TEACHER", "COMPUTER SCIENCE");
  }

  @Test
  void testHandleCourseCreated_ZeroCredits() {
    testMessage.put("courseId", "course123");
    testMessage.put("courseName", "Zero Credit Course");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("credits", 0);

    courseConsumerService.handleCourseCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher001", "TEACHER", null);
  }
}