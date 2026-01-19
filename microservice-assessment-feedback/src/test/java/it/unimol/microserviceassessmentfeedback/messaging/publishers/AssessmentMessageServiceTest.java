package it.unimol.microserviceassessmentfeedback.messaging.publishers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import it.unimol.microserviceassessmentfeedback.dto.AssessmentDto;
import it.unimol.microserviceassessmentfeedback.enums.ReferenceType;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
class AssessmentMessageServiceTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private AssessmentMessageService assessmentMessageService;

  private AssessmentDto testAssessmentDto;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(assessmentMessageService, "assessmentsExchange", "assessments.exchange");
    ReflectionTestUtils.setField(assessmentMessageService, "serviceName", "microservice-assessment-feedback");

    testAssessmentDto = new AssessmentDto();
    testAssessmentDto.setId("assessment1");
    testAssessmentDto.setStudentId("student1");
    testAssessmentDto.setTeacherId("teacher1");
    testAssessmentDto.setCourseId("course1");
    testAssessmentDto.setReferenceId("ref1");
    testAssessmentDto.setReferenceType(ReferenceType.ASSIGNMENT);
    testAssessmentDto.setScore(85.0);
    testAssessmentDto.setNotes("Good work");
    testAssessmentDto.setAssessmentDate(LocalDateTime.now(ZoneId.systemDefault()));
  }

  @Test
  void testPublishAssessmentCreated() {
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    assessmentMessageService.publishAssessmentCreated(testAssessmentDto);

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("assessment.created"),
        any(Map.class)
    );
  }

  @Test
  void testPublishAssessmentUpdated() {
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    assessmentMessageService.publishAssessmentUpdated(testAssessmentDto);

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("assessment.updated"),
        any(Map.class)
    );
  }

  @Test
  void testPublishAssessmentDeleted() {
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    assessmentMessageService.publishAssessmentDeleted("assessment1");

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("assessment.deleted"),
        any(Map.class)
    );
  }

  @Test
  void testPublishAssessmentCreated_WithException() {
    doThrow(new RuntimeException("RabbitMQ error"))
        .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    try {
      assessmentMessageService.publishAssessmentCreated(testAssessmentDto);
    } catch (Exception e) {
      // Exception expected
    }

    verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Map.class));
  }
}