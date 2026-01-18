package it.unimol.microserviceassessmentfeedback.messaging.consumers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
class ExamConsumerServiceTest {

  @Mock
  private AssessmentService assessmentService;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private ExamConsumerService examConsumerService;

  private Map<String, Object> testMessage;

  @BeforeEach
  void setUp() {
    testMessage = new HashMap<>();
  }

  // ===================================================================
  // TEST EXAM COMPLETED
  // ===================================================================

  @Test
  void testHandleExamCompleted() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("completionTime", System.currentTimeMillis());
    testMessage.put("duration", 120);
    testMessage.put("examType", "MIDTERM");

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);

    examConsumerService.handleExamCompleted(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
    verify(notificationService).notifyTeacherOfExamCompletion("teacher001", "exam123", "student456");
    verify(notificationService).scheduleFeedbackSurvey("student456", "exam123", "course789");
  }

  @Test
  void testHandleExamCompleted_NullCompletionTime() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("completionTime", null);
    testMessage.put("examType", "FINAL");

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);

    examConsumerService.handleExamCompleted(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
  }

  @Test
  void testHandleExamCompleted_NullExamType() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("examType", null);

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);

    examConsumerService.handleExamCompleted(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
  }

  @Test
  void testHandleExamCompleted_NullDuration() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("duration", null);

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);

    examConsumerService.handleExamCompleted(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
  }

  // ===================================================================
  // TEST EXAM GRADE REGISTERED
  // ===================================================================

  @Test
  void testHandleExamGradeRegistered_WithAssessmentId() {
    testMessage.put("examId", "exam123");
    testMessage.put("assessmentId", "assessment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 28);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "A");
    testMessage.put("feedback", "Excellent work");
    testMessage.put("gradeDate", System.currentTimeMillis());

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    mockAssessment.setNotes("Exam completed - awaiting grade");
    when(assessmentService.getAssessmentById("assessment123")).thenReturn(mockAssessment);
    when(assessmentService.updateAssessment(eq("assessment123"), any(AssessmentDto.class)))
        .thenReturn(mockAssessment);

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(assessmentService).getAssessmentById("assessment123");
    verify(assessmentService).updateAssessment(eq("assessment123"), any(AssessmentDto.class));
    verify(notificationService).notifyStudentOfGrade("student456", "exam123", "A", 28, 30);
    verify(notificationService).activatePostExamSurvey("student456", "exam123", "course789");
  }

  @Test
  void testHandleExamGradeRegistered_WithoutAssessmentId() {
    testMessage.put("examId", "exam123");
    testMessage.put("assessmentId", null);
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 25);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "B");

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
    verify(notificationService).notifyStudentOfGrade("student456", "exam123", "B", 25, 30);
  }

  @Test
  void testHandleExamGradeRegistered_ExcellentPerformance() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 29);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "A+");

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(notificationService).notifyStudentOfGrade("student456", "exam123", "A+", 29, 30);
  }

  @Test
  void testHandleExamGradeRegistered_LowPerformance() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 15);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "F");

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(notificationService).notifyStudentOfGrade("student456", "exam123", "F", 15, 30);
  }

  @Test
  void testHandleExamGradeRegistered_WithLongFeedback() {
    testMessage.put("examId", "exam123");
    testMessage.put("assessmentId", "assessment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 28);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "A");
    testMessage.put("feedback", "a".repeat(150)); // Feedback > 100 chars

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    mockAssessment.setNotes("Exam completed - awaiting grade");
    when(assessmentService.getAssessmentById("assessment123")).thenReturn(mockAssessment);

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(assessmentService).updateAssessment(eq("assessment123"), any(AssessmentDto.class));
  }

  @Test
  void testHandleExamGradeRegistered_EmptyFeedback() {
    testMessage.put("examId", "exam123");
    testMessage.put("assessmentId", "assessment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 25);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "B");
    testMessage.put("feedback", "   "); // Whitespace only

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    mockAssessment.setNotes("Exam completed - awaiting grade");
    when(assessmentService.getAssessmentById("assessment123")).thenReturn(mockAssessment);

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(assessmentService).updateAssessment(eq("assessment123"), any(AssessmentDto.class));
  }

  @Test
  void testHandleExamGradeRegistered_NullGradeDate() {
    testMessage.put("examId", "exam123");
    testMessage.put("assessmentId", "assessment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 28);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "A");
    testMessage.put("gradeDate", null);

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    mockAssessment.setNotes("Exam completed - awaiting grade");
    when(assessmentService.getAssessmentById("assessment123")).thenReturn(mockAssessment);

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(assessmentService).updateAssessment(eq("assessment123"), any(AssessmentDto.class));
  }

  @Test
  void testHandleExamGradeRegistered_UpdateFails() {
    testMessage.put("examId", "exam123");
    testMessage.put("assessmentId", "assessment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 28);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "A");

    when(assessmentService.getAssessmentById("assessment123"))
        .thenThrow(new RuntimeException("Assessment not found"));

    examConsumerService.handleExamGradeRegistered(testMessage);

    // Deve continuare anche se l'update fallisce
    verify(notificationService).notifyStudentOfGrade("student456", "exam123", "A", 28, 30);
  }

  @Test
  void testHandleExamGradeRegistered_NullScore() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", null);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "N/A");

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
  }

  // ===================================================================
  // TEST HANDLE MESSAGE
  // ===================================================================

  @Test
  void testHandleMessage_ExamCompleted() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);

    examConsumerService.handleMessage(testMessage, "EXAM_COMPLETED");

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
  }

  @Test
  void testHandleMessage_ExamGradeRegistered() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 28);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "A");

    examConsumerService.handleMessage(testMessage, "EXAM_GRADE_REGISTERED");

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
  }

  @Test
  void testHandleMessage_UnknownType() {
    examConsumerService.handleMessage(testMessage, "UNKNOWN_TYPE");

    verify(assessmentService, never()).createAssessment(any());
    verify(notificationService, never()).notifyStudentOfGrade(anyString(), anyString(),
        anyString(), anyInt(), anyInt());
  }

  // ===================================================================
  // TEST EDGE CASES
  // ===================================================================

  @Test
  void testHandleExamGradeRegistered_PerfectScore() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 30);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "A+");

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(notificationService).notifyStudentOfGrade("student456", "exam123", "A+", 30, 30);
  }

  @Test
  void testHandleExamGradeRegistered_ZeroScore() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 0);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "F");

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(notificationService).notifyStudentOfGrade("student456", "exam123", "F", 0, 30);
  }

  @Test
  void testHandleExamGradeRegistered_ShortFeedback() {
    testMessage.put("examId", "exam123");
    testMessage.put("assessmentId", "assessment123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");
    testMessage.put("score", 28);
    testMessage.put("maxScore", 30);
    testMessage.put("grade", "A");
    testMessage.put("feedback", "Good"); // Short feedback < 100 chars

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    mockAssessment.setNotes("Exam completed - awaiting grade");
    when(assessmentService.getAssessmentById("assessment123")).thenReturn(mockAssessment);

    examConsumerService.handleExamGradeRegistered(testMessage);

    verify(assessmentService).updateAssessment(eq("assessment123"), any(AssessmentDto.class));
  }

  @Test
  void testHandleExamCompleted_MinimalData() {
    testMessage.put("examId", "exam123");
    testMessage.put("studentId", "student456");
    testMessage.put("courseId", "course789");
    testMessage.put("teacherId", "teacher001");

    AssessmentDto mockAssessment = new AssessmentDto();
    mockAssessment.setId("assessment123");
    when(assessmentService.createAssessment(any(AssessmentDto.class))).thenReturn(mockAssessment);

    examConsumerService.handleExamCompleted(testMessage);

    verify(assessmentService).createAssessment(any(AssessmentDto.class));
  }
}