package it.unimol.microserviceassessmentfeedback.messaging.publishers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import it.unimol.microserviceassessmentfeedback.dto.SurveyResponseDto;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
class SurveyResponseMessageServiceTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private SurveyResponseMessageService surveyResponseMessageService;

  private SurveyResponseDto testResponseDto;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(surveyResponseMessageService, "assessmentsExchange", "assessments.exchange");
    ReflectionTestUtils.setField(surveyResponseMessageService, "serviceName", "microservice-assessment-feedback");

    testResponseDto = new SurveyResponseDto();
    testResponseDto.setId("response1");
    testResponseDto.setSurveyId("survey1");
    testResponseDto.setStudentId("student1");
    testResponseDto.setQuestionId("q1");
    testResponseDto.setNumericRating(4);
    testResponseDto.setTextComment("Good course");
    testResponseDto.setSubmissionDate(LocalDateTime.now());
  }

  @Test
  void testPublishSurveyResponseSubmitted() {
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    surveyResponseMessageService.publishSurveyResponseSubmitted(testResponseDto);

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("survey.response.submitted"),
        any(Map.class)
    );
  }

  @Test
  void testPublishSurveyResponsesSubmitted() {
    SurveyResponseDto response2 = new SurveyResponseDto();
    response2.setId("response2");
    response2.setSurveyId("survey1");
    response2.setStudentId("student1");
    response2.setQuestionId("q2");
    response2.setNumericRating(5);
    response2.setTextComment("Excellent");
    response2.setSubmissionDate(LocalDateTime.now());

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto, response2);

    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    surveyResponseMessageService.publishSurveyResponsesSubmitted(responses, "survey1");

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("survey.responses.bulk.submitted"),
        any(Map.class)
    );
  }

  @Test
  void testPublishSurveyResponseSubmitted_WithException() {
    doThrow(new RuntimeException("RabbitMQ error"))
        .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    try {
      surveyResponseMessageService.publishSurveyResponseSubmitted(testResponseDto);
    } catch (Exception e) {
      // Exception expected
    }

    verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Map.class));
  }

  @Test
  void testPublishSurveyResponsesSubmitted_SingleResponse() {
    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Map.class));

    surveyResponseMessageService.publishSurveyResponsesSubmitted(responses, "survey1");

    verify(rabbitTemplate, times(1)).convertAndSend(
        eq("assessments.exchange"),
        eq("survey.responses.bulk.submitted"),
        any(Map.class)
    );
  }
}