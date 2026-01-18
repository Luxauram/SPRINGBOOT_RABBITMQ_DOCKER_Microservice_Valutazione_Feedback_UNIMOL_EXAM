package it.unimol.microserviceassessmentfeedback.messaging.publishers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import it.unimol.microserviceassessmentfeedback.dto.DetailedFeedbackDto;
import it.unimol.microserviceassessmentfeedback.enums.FeedbackCategory;
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
class FeedbackMessageServiceTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private FeedbackMessageService feedbackMessageService;

  private DetailedFeedbackDto testFeedbackDto;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(feedbackMessageService, "assessmentsExchange", "assessments.exchange");
    ReflectionTestUtils.setField(feedbackMessageService, "serviceName", "microservice-assessment-feedback");

    testFeedbackDto = new DetailedFeedbackDto();
    testFeedbackDto.setId("feedback1");
    testFeedbackDto.setAssessmentId("assessment1");
    testFeedbackDto.setFeedbackText("Great job!");
    testFeedbackDto.setCategory(FeedbackCategory.CONTENT);
    testFeedbackDto.setStrengths("Strong analytical skills");
    testFeedbackDto.setImprovementAreas("Time management");
  }

  @Test
  void testPublishFeedbackCreated() {
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    feedbackMessageService.publishFeedbackCreated(testFeedbackDto);

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("feedback.created"),
        any(Map.class)
    );
  }

  @Test
  void testPublishFeedbackUpdated() {
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    feedbackMessageService.publishFeedbackUpdated(testFeedbackDto);

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("feedback.updated"),
        any(Map.class)
    );
  }

  @Test
  void testPublishFeedbackDeleted() {
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    feedbackMessageService.publishFeedbackDeleted("feedback1");

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("feedback.deleted"),
        any(Map.class)
    );
  }

  @Test
  void testPublishFeedbackCreated_WithException() {
    doThrow(new RuntimeException("RabbitMQ error"))
        .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    try {
      feedbackMessageService.publishFeedbackCreated(testFeedbackDto);
    } catch (Exception e) {
      // Exception expected
    }

    verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Map.class));
  }
}