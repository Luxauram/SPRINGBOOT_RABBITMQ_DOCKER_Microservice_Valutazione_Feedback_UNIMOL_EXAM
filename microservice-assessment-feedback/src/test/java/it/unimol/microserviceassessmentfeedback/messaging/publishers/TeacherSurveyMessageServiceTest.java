package it.unimol.microserviceassessmentfeedback.messaging.publishers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import it.unimol.microserviceassessmentfeedback.dto.TeacherSurveyDto;
import it.unimol.microserviceassessmentfeedback.dto.TeacherSurveyDto.SurveyQuestionDto;
import it.unimol.microserviceassessmentfeedback.enums.QuestionType;
import it.unimol.microserviceassessmentfeedback.enums.SurveyStatus;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TeacherSurveyMessageServiceTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private TeacherSurveyMessageService teacherSurveyMessageService;

  private TeacherSurveyDto testSurveyDto;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(teacherSurveyMessageService, "assessmentsExchange", "assessments.exchange");
    ReflectionTestUtils.setField(teacherSurveyMessageService, "serviceName", "microservice-assessment-feedback");

    SurveyQuestionDto testQuestion = SurveyQuestionDto.builder()
        .questionText("How satisfied are you?")
        .questionType(QuestionType.RATING)
        .minRating(1)
        .maxRating(5)
        .build();

    testSurveyDto = TeacherSurveyDto.builder()
        .id("survey1")
        .courseId("course1")
        .teacherId("teacher1")
        .academicYear("2023/2024")
        .semester(1)
        .status(SurveyStatus.ACTIVE)
        .title("Course Evaluation")
        .description("Survey description")
        .questions(Arrays.asList(testQuestion))
        .creationDate(LocalDateTime.now())
        .build();
  }

  @Test
  void testPublishSurveyCompleted() {
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    teacherSurveyMessageService.publishSurveyCompleted(testSurveyDto);

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("survey.completed"),
        any(Map.class)
    );
  }

  @Test
  void testPublishSurveyResultsRequested() {
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    teacherSurveyMessageService.publishSurveyResultsRequested("survey1", "teacher1");

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("survey.results.requested"),
        any(Map.class)
    );
  }

  @Test
  void testPublishSurveyCommentsRequested() {
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    teacherSurveyMessageService.publishSurveyCommentsRequested("survey1", "teacher1");

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("survey.comments.requested"),
        any(Map.class)
    );
  }

  @Test
  void testPublishSurveyCompleted_WithException() {
    doThrow(new RuntimeException("RabbitMQ error"))
        .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    try {
      teacherSurveyMessageService.publishSurveyCompleted(testSurveyDto);
    } catch (Exception e) {
      // Exception expected
    }

    verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Map.class));
  }

  @Test
  void testPublishSurveyCompleted_WithClosingDate() {
    testSurveyDto.setStatus(SurveyStatus.CLOSED);
    testSurveyDto.setClosingDate(LocalDateTime.now());

    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    teacherSurveyMessageService.publishSurveyCompleted(testSurveyDto);

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("survey.completed"),
        any(Map.class)
    );
  }
}