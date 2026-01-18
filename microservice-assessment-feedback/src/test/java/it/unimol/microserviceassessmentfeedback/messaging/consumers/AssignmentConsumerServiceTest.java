package it.unimol.microserviceassessmentfeedback.messaging.consumers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceassessmentfeedback.dto.AssessmentDto;
import it.unimol.microserviceassessmentfeedback.service.AssessmentService;
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
class AssignmentConsumerServiceTest {

  @Mock
  private AssessmentService assessmentService;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private AssignmentConsumerService assignmentConsumerService;

  private Map<String, Object> testMessage;

  @BeforeEach
  void setUp() {
    testMessage = new HashMap<>();
  }

  // ===================================================================
  // TEST ASSIGNMENT SUBMITTED
  // ===================================================================

  @Test
  void testHandleAssignmentSubmitted() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("submissionTime", System.currentTimeMillis());
    testMessage.put("fileName", "homework.pdf");

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);

    assignmentConsumerService.handleAssignmentSubmitted(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
    verify(notificationService).notifyTeacherOfSubmission("teacher001", "assignment123", "student456");
  }

  @Test
  void testHandleAssignmentSubmitted_WithSubmissionContent() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("submissionTime", System.currentTimeMillis());
    testMessage.put("submissionContent", "This is my submission content");
    testMessage.put("fileUrl", "https://example.com/file.pdf");

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    mockAssessment.setNotes("Initial notes");

    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);
    when(assessmentService.getAssessmentById("assessment123")).thenReturn(mockAssessment);
    when(assessmentService.updateAssessment(eq("assessment123"), any(AssessmentDto.class)))
        .thenReturn(mockAssessment);

    assignmentConsumerService.handleAssignmentSubmitted(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
    verify(assessmentService).getAssessmentById("assessment123");
    verify(assessmentService).updateAssessment(eq("assessment123"), any(AssessmentDto.class));
  }

  @Test
  void testHandleAssignmentSubmitted_WithLongContent() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("submissionContent", "a".repeat(100)); // Content > 50 chars

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    mockAssessment.setNotes("Initial notes");

    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);
    when(assessmentService.getAssessmentById("assessment123")).thenReturn(mockAssessment);

    assignmentConsumerService.handleAssignmentSubmitted(testMessage);

    verify(assessmentService).getAssessmentById("assessment123");
  }

  // ===================================================================
  // TEST ASSIGNMENT CREATED
  // ===================================================================

  @Test
  void testHandleAssignmentCreated() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("title", "Test Assignment");
    testMessage.put("description", "Assignment description");
    testMessage.put("dueDate", System.currentTimeMillis());
    testMessage.put("maxScore", 100);
    testMessage.put("assignmentType", "STANDARD");

    assignmentConsumerService.handleAssignmentCreated(testMessage);

    // Verifica che il metodo non lanci eccezioni
    verify(notificationService, never()).notifyTeacherOfSubmission(anyString(), anyString(), anyString());
  }

  @Test
  void testHandleAssignmentCreated_PeerReview() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("title", "Peer Review Assignment");
    testMessage.put("assignmentType", "PEER_REVIEW");
    testMessage.put("maxScore", 100);

    assignmentConsumerService.handleAssignmentCreated(testMessage);

    // Verifica che non lanci eccezioni con tipo PEER_REVIEW
    verify(assessmentService, never()).createAssessment(any());
  }

  // ===================================================================
  // TEST ASSIGNMENT UPDATED
  // ===================================================================

  @Test
  void testHandleAssignmentUpdated_DueDateChanged() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("title", "Updated Assignment");
    testMessage.put("updateType", "DUE_DATE_CHANGED");
    testMessage.put("dueDate", System.currentTimeMillis());

    assignmentConsumerService.handleAssignmentUpdated(testMessage);

    verify(notificationService).notifyStudentsOfAssignmentUpdate(
        "assignment123", "course789", "DUE_DATE_CHANGED", "Updated Assignment");
  }

  @Test
  void testHandleAssignmentUpdated_MaxScoreChanged() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("title", "Updated Assignment");
    testMessage.put("updateType", "MAX_SCORE_CHANGED");
    testMessage.put("maxScore", 150);

    assignmentConsumerService.handleAssignmentUpdated(testMessage);

    verify(notificationService).notifyStudentsOfAssignmentUpdate(
        "assignment123", "course789", "MAX_SCORE_CHANGED", "Updated Assignment");
  }

  @Test
  void testHandleAssignmentUpdated_RequirementsChanged() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("title", "Updated Assignment");
    testMessage.put("updateType", "REQUIREMENTS_CHANGED");

    assignmentConsumerService.handleAssignmentUpdated(testMessage);

    verify(notificationService).notifyStudentsOfAssignmentUpdate(
        "assignment123", "course789", "REQUIREMENTS_CHANGED", "Updated Assignment");
  }

  @Test
  void testHandleAssignmentUpdated_DescriptionUpdated() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("title", "Updated Assignment");
    testMessage.put("updateType", "DESCRIPTION_UPDATED");
    testMessage.put("description", "New description");

    assignmentConsumerService.handleAssignmentUpdated(testMessage);

    // DESCRIPTION_UPDATED non è un aggiornamento significativo, quindi nessuna notifica
    verify(notificationService, never()).notifyStudentsOfAssignmentUpdate(
        anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testHandleAssignmentUpdated_CriteriaUpdated() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("title", "Updated Assignment");
    testMessage.put("updateType", "CRITERIA_UPDATED");
    testMessage.put("maxScore", 100);
    testMessage.put("description", "Updated criteria");

    assignmentConsumerService.handleAssignmentUpdated(testMessage);

    // CRITERIA_UPDATED non è nella lista degli aggiornamenti significativi
    verify(notificationService, never()).notifyStudentsOfAssignmentUpdate(
        anyString(), anyString(), anyString(), anyString());
  }

  // ===================================================================
  // TEST EDGE CASES
  // ===================================================================

  @Test
  void testHandleAssignmentSubmitted_NullSubmissionTime() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("submissionTime", null);

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);

    assignmentConsumerService.handleAssignmentSubmitted(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
  }

  @Test
  void testHandleAssignmentSubmitted_EmptySubmissionContent() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("submissionContent", "   "); // Whitespace only

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);

    assignmentConsumerService.handleAssignmentSubmitted(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
    verify(assessmentService, never()).getAssessmentById(anyString());
  }

  @Test
  void testHandleAssignmentCreated_NullDueDate() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("title", "Test Assignment");
    testMessage.put("assignmentType", "STANDARD");
    testMessage.put("dueDate", null);

    assignmentConsumerService.handleAssignmentCreated(testMessage);

    verify(assessmentService, never()).createAssessment(any());
  }

  @Test
  void testHandleAssignmentUpdated_UnknownUpdateType() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("title", "Updated Assignment");
    testMessage.put("updateType", "UNKNOWN_TYPE");

    assignmentConsumerService.handleAssignmentUpdated(testMessage);

    verify(notificationService, never()).notifyStudentsOfAssignmentUpdate(
        anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testHandleAssignmentSubmitted_UpdateAssessmentFails() {
    testMessage.put("assignmentId", "assignment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("submissionContent", "Content");

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");

    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);
    when(assessmentService.getAssessmentById("assessment123"))
        .thenThrow(new RuntimeException("Assessment not found"));

    // Deve continuare anche se l'update fallisce
    assignmentConsumerService.handleAssignmentSubmitted(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
  }

  @Test
  void testHandleMessage_UnknownMessageType() {
    testMessage.put("eventType", "UNKNOWN_EVENT");
    testMessage.put("serviceName", "test-service");
    testMessage.put("timestamp", System.currentTimeMillis());

    // Non deve lanciare eccezioni
    assignmentConsumerService.handleMessage(testMessage, "UNKNOWN_TYPE");

    verify(assessmentService, never()).createAssessment(any());
    verify(notificationService, never()).notifyTeacherOfSubmission(anyString(), anyString(), anyString());
  }
}