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
class UserConsumerServiceTest {

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private UserConsumerService userConsumerService;

  private Map<String, Object> testMessage;

  @BeforeEach
  void setUp() {
    testMessage = new HashMap<>();
  }

  // ===================================================================
  // TEST TEACHER CREATED
  // ===================================================================

  @Test
  void testHandleTeacherCreated() {
    testMessage.put("teacherId", "teacher123");
    testMessage.put("firstName", "John");
    testMessage.put("lastName", "Smith");
    testMessage.put("email", "john.smith@university.edu");
    testMessage.put("department", "Computer Science");
    testMessage.put("academicTitle", "Professor");
    testMessage.put("specialization", "Machine Learning");
    testMessage.put("phoneNumber", "+1234567890");
    testMessage.put("officeLocation", "Building A, Room 301");

    userConsumerService.handleTeacherCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher123", "TEACHER", "Computer Science");
  }

  @Test
  void testHandleTeacherCreated_ComputerScience() {
    testMessage.put("teacherId", "teacher123");
    testMessage.put("firstName", "Jane");
    testMessage.put("lastName", "Doe");
    testMessage.put("email", "jane.doe@university.edu");
    testMessage.put("department", "Computer Science");
    testMessage.put("academicTitle", "Associate Professor");

    userConsumerService.handleTeacherCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher123", "TEACHER", "Computer Science");
  }

  @Test
  void testHandleTeacherCreated_Mathematics() {
    testMessage.put("teacherId", "teacher456");
    testMessage.put("firstName", "Alice");
    testMessage.put("lastName", "Johnson");
    testMessage.put("email", "alice.j@university.edu");
    testMessage.put("department", "Mathematics");
    testMessage.put("academicTitle", "Professor");

    userConsumerService.handleTeacherCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher456", "TEACHER", "Mathematics");
  }

  @Test
  void testHandleTeacherCreated_Engineering() {
    testMessage.put("teacherId", "teacher789");
    testMessage.put("firstName", "Bob");
    testMessage.put("lastName", "Wilson");
    testMessage.put("email", "bob.w@university.edu");
    testMessage.put("department", "Engineering");
    testMessage.put("academicTitle", "Assistant Professor");

    userConsumerService.handleTeacherCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher789", "TEACHER", "Engineering");
  }

  @Test
  void testHandleTeacherCreated_OtherDepartment() {
    testMessage.put("teacherId", "teacher999");
    testMessage.put("firstName", "Charlie");
    testMessage.put("lastName", "Brown");
    testMessage.put("email", "charlie.b@university.edu");
    testMessage.put("department", "History");
    testMessage.put("academicTitle", "Lecturer");

    userConsumerService.handleTeacherCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher999", "TEACHER", "History");
  }

  @Test
  void testHandleTeacherCreated_WithSpecialization() {
    testMessage.put("teacherId", "teacher111");
    testMessage.put("firstName", "David");
    testMessage.put("lastName", "Lee");
    testMessage.put("email", "david.l@university.edu");
    testMessage.put("department", "Computer Science");
    testMessage.put("specialization", "Artificial Intelligence");

    userConsumerService.handleTeacherCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher111", "TEACHER", "Computer Science");
  }

  @Test
  void testHandleTeacherCreated_MinimalData() {
    testMessage.put("teacherId", "teacher222");
    testMessage.put("firstName", "Eve");
    testMessage.put("lastName", "Martin");
    testMessage.put("email", "eve.m@university.edu");

    userConsumerService.handleTeacherCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher222", "TEACHER", null);
  }

  // ===================================================================
  // TEST STUDENT CREATED
  // ===================================================================

  @Test
  void testHandleStudentCreated() {
    testMessage.put("studentId", "student123");
    testMessage.put("firstName", "Tom");
    testMessage.put("lastName", "Anderson");
    testMessage.put("email", "tom.a@students.university.edu");
    testMessage.put("matriculationNumber", "MAT001234");
    testMessage.put("degreeProgram", "Computer Science");
    testMessage.put("academicYear", "2023-2024");
    testMessage.put("yearOfStudy", 1);
    testMessage.put("department", "Computer Science");

    userConsumerService.handleStudentCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("student123", "STUDENT", "Computer Science");
  }

  @Test
  void testHandleStudentCreated_FirstYear() {
    testMessage.put("studentId", "student456");
    testMessage.put("firstName", "Sarah");
    testMessage.put("lastName", "Davis");
    testMessage.put("email", "sarah.d@students.university.edu");
    testMessage.put("matriculationNumber", "MAT001235");
    testMessage.put("degreeProgram", "Computer Science");
    testMessage.put("yearOfStudy", 1);
    testMessage.put("department", "CS");

    userConsumerService.handleStudentCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("student456", "STUDENT", "CS");
  }

  @Test
  void testHandleStudentCreated_UpperYear() {
    testMessage.put("studentId", "student789");
    testMessage.put("firstName", "Mike");
    testMessage.put("lastName", "Taylor");
    testMessage.put("email", "mike.t@students.university.edu");
    testMessage.put("matriculationNumber", "MAT001236");
    testMessage.put("degreeProgram", "Mathematics");
    testMessage.put("yearOfStudy", 3);
    testMessage.put("department", "Math");

    userConsumerService.handleStudentCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("student789", "STUDENT", "Math");
  }

  @Test
  void testHandleStudentCreated_NullYearOfStudy() {
    testMessage.put("studentId", "student999");
    testMessage.put("firstName", "Lisa");
    testMessage.put("lastName", "White");
    testMessage.put("email", "lisa.w@students.university.edu");
    testMessage.put("matriculationNumber", "MAT001237");
    testMessage.put("degreeProgram", "Physics");
    testMessage.put("yearOfStudy", null);

    userConsumerService.handleStudentCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("student999", "STUDENT", null);
  }

  @Test
  void testHandleStudentCreated_MinimalData() {
    testMessage.put("studentId", "student111");
    testMessage.put("firstName", "Anna");
    testMessage.put("lastName", "Green");
    testMessage.put("email", "anna.g@students.university.edu");
    testMessage.put("matriculationNumber", "MAT001238");
    testMessage.put("degreeProgram", "Biology");

    userConsumerService.handleStudentCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("student111", "STUDENT", null);
  }

  @Test
  void testHandleStudentCreated_CompleteData() {
    testMessage.put("studentId", "student222");
    testMessage.put("firstName", "Robert");
    testMessage.put("lastName", "Black");
    testMessage.put("email", "robert.b@students.university.edu");
    testMessage.put("matriculationNumber", "MAT001239");
    testMessage.put("degreeProgram", "Engineering");
    testMessage.put("academicYear", "2024-2025");
    testMessage.put("yearOfStudy", 2);
    testMessage.put("department", "Engineering");

    userConsumerService.handleStudentCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("student222", "STUDENT", "Engineering");
  }

  // ===================================================================
  // TEST USER DELETED
  // ===================================================================

  @Test
  void testHandleUserDeleted_Teacher() {
    testMessage.put("userId", "teacher123");
    testMessage.put("userType", "TEACHER");
    testMessage.put("reason", "Retirement");
    testMessage.put("deletionDate", System.currentTimeMillis());

    userConsumerService.handleUserDeleted(testMessage);

    verify(notificationService, never()).sendWelcomeNotification(anyString(), anyString(), anyString());
  }

  @Test
  void testHandleUserDeleted_Student() {
    testMessage.put("userId", "student456");
    testMessage.put("userType", "STUDENT");
    testMessage.put("reason", "Graduation");
    testMessage.put("deletionDate", System.currentTimeMillis());

    userConsumerService.handleUserDeleted(testMessage);

    verify(notificationService, never()).sendWelcomeNotification(anyString(), anyString(), anyString());
  }

  @Test
  void testHandleUserDeleted_UnknownType() {
    testMessage.put("userId", "user999");
    testMessage.put("userType", "ADMIN");
    testMessage.put("reason", "Unknown");

    userConsumerService.handleUserDeleted(testMessage);

    verify(notificationService, never()).sendWelcomeNotification(anyString(), anyString(), anyString());
  }

  @Test
  void testHandleUserDeleted_NullReason() {
    testMessage.put("userId", "teacher456");
    testMessage.put("userType", "TEACHER");
    testMessage.put("reason", null);

    userConsumerService.handleUserDeleted(testMessage);

    verify(notificationService, never()).sendWelcomeNotification(anyString(), anyString(), anyString());
  }

  @Test
  void testHandleUserDeleted_NullDeletionDate() {
    testMessage.put("userId", "student789");
    testMessage.put("userType", "STUDENT");
    testMessage.put("reason", "Transfer");
    testMessage.put("deletionDate", null);

    userConsumerService.handleUserDeleted(testMessage);

    verify(notificationService, never()).sendWelcomeNotification(anyString(), anyString(), anyString());
  }

  // ===================================================================
  // TEST HANDLE MESSAGE
  // ===================================================================

  @Test
  void testHandleMessage_TeacherCreated() {
    testMessage.put("teacherId", "teacher123");
    testMessage.put("firstName", "Test");
    testMessage.put("lastName", "Teacher");
    testMessage.put("email", "test@university.edu");
    testMessage.put("department", "CS");

    userConsumerService.handleMessage(testMessage, "TEACHER_CREATED");

    verify(notificationService).sendWelcomeNotification("teacher123", "TEACHER", "CS");
  }

  @Test
  void testHandleMessage_StudentCreated() {
    testMessage.put("studentId", "student123");
    testMessage.put("firstName", "Test");
    testMessage.put("lastName", "Student");
    testMessage.put("email", "test@students.university.edu");
    testMessage.put("matriculationNumber", "MAT123");
    testMessage.put("degreeProgram", "CS");

    userConsumerService.handleMessage(testMessage, "STUDENT_CREATED");

    verify(notificationService).sendWelcomeNotification("student123", "STUDENT", null);
  }

  @Test
  void testHandleMessage_UserDeleted() {
    testMessage.put("userId", "user123");
    testMessage.put("userType", "TEACHER");
    testMessage.put("reason", "Test");

    userConsumerService.handleMessage(testMessage, "USER_DELETED");

    verify(notificationService, never()).sendWelcomeNotification(anyString(), anyString(), anyString());
  }

  @Test
  void testHandleMessage_UnknownType() {
    userConsumerService.handleMessage(testMessage, "UNKNOWN_TYPE");

    verify(notificationService, never()).sendWelcomeNotification(anyString(), anyString(), anyString());
  }

  // ===================================================================
  // TEST EDGE CASES
  // ===================================================================

  @Test
  void testHandleTeacherCreated_EmptyStrings() {
    testMessage.put("teacherId", "");
    testMessage.put("firstName", "");
    testMessage.put("lastName", "");
    testMessage.put("email", "");
    testMessage.put("department", "");

    userConsumerService.handleTeacherCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("", "TEACHER", "");
  }

  @Test
  void testHandleStudentCreated_EmptyStrings() {
    testMessage.put("studentId", "");
    testMessage.put("firstName", "");
    testMessage.put("lastName", "");
    testMessage.put("email", "");
    testMessage.put("matriculationNumber", "");
    testMessage.put("degreeProgram", "");

    userConsumerService.handleStudentCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("", "STUDENT", null);
  }

  @Test
  void testHandleUserDeleted_EmptyStrings() {
    testMessage.put("userId", "");
    testMessage.put("userType", "TEACHER");
    testMessage.put("reason", "");

    userConsumerService.handleUserDeleted(testMessage);

    verify(notificationService, never()).sendWelcomeNotification(anyString(), anyString(), anyString());
  }

  @Test
  void testHandleTeacherCreated_CaseInsensitiveDepartment() {
    testMessage.put("teacherId", "teacher333");
    testMessage.put("firstName", "Test");
    testMessage.put("lastName", "Prof");
    testMessage.put("email", "test@university.edu");
    testMessage.put("department", "COMPUTER SCIENCE"); // Uppercase

    userConsumerService.handleTeacherCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher333", "TEACHER", "COMPUTER SCIENCE");
  }

  @Test
  void testHandleStudentCreated_ZeroYearOfStudy() {
    testMessage.put("studentId", "student444");
    testMessage.put("firstName", "Test");
    testMessage.put("lastName", "Student");
    testMessage.put("email", "test@students.university.edu");
    testMessage.put("matriculationNumber", "MAT444");
    testMessage.put("degreeProgram", "CS");
    testMessage.put("yearOfStudy", 0);

    userConsumerService.handleStudentCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("student444", "STUDENT", null);
  }

  @Test
  void testHandleTeacherCreated_NullSpecialization() {
    testMessage.put("teacherId", "teacher555");
    testMessage.put("firstName", "Test");
    testMessage.put("lastName", "Professor");
    testMessage.put("email", "test@university.edu");
    testMessage.put("department", "Physics");
    testMessage.put("specialization", null);

    userConsumerService.handleTeacherCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("teacher555", "TEACHER", "Physics");
  }

  @Test
  void testHandleStudentCreated_NonComputerScience() {
    testMessage.put("studentId", "student666");
    testMessage.put("firstName", "Test");
    testMessage.put("lastName", "Student");
    testMessage.put("email", "test@students.university.edu");
    testMessage.put("matriculationNumber", "MAT666");
    testMessage.put("degreeProgram", "History");
    testMessage.put("yearOfStudy", 1);

    userConsumerService.handleStudentCreated(testMessage);

    verify(notificationService).sendWelcomeNotification("student666", "STUDENT", null);
  }
}