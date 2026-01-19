package it.unimol.microserviceassessmentfeedback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceassessmentfeedback.common.exception.DuplicateResponseException;
import it.unimol.microserviceassessmentfeedback.common.exception.ResourceNotFoundException;
import it.unimol.microserviceassessmentfeedback.common.exception.SurveyClosedException;
import it.unimol.microserviceassessmentfeedback.dto.SurveyResponseDto;
import it.unimol.microserviceassessmentfeedback.enums.SurveyStatus;
import it.unimol.microserviceassessmentfeedback.messaging.publishers.SurveyResponseMessageService;
import it.unimol.microserviceassessmentfeedback.messaging.publishers.TeacherSurveyMessageService;
import it.unimol.microserviceassessmentfeedback.model.SurveyResponse;
import it.unimol.microserviceassessmentfeedback.model.TeacherSurvey;
import it.unimol.microserviceassessmentfeedback.repository.SurveyResponseRepository;
import it.unimol.microserviceassessmentfeedback.repository.TeacherSurveyRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SurveyResponseServiceTest {

  @Mock
  private SurveyResponseRepository responseRepository;

  @Mock
  private TeacherSurveyRepository surveyRepository;

  @Mock
  private SurveyResponseMessageService surveyResponseMessageService;

  @Mock
  private TeacherSurveyMessageService teacherSurveyMessageService;

  @InjectMocks
  private SurveyResponseService surveyResponseService;

  private TeacherSurvey testSurvey;
  private SurveyResponse testResponse;
  private SurveyResponseDto testResponseDto;

  @BeforeEach
  void setUp() {
    testSurvey = new TeacherSurvey();
    testSurvey.setId("survey1");
    testSurvey.setCourseId("course1");
    testSurvey.setTeacherId("teacher1");
    testSurvey.setStatus(SurveyStatus.ACTIVE);

    testResponse = new SurveyResponse();
    testResponse.setId("response1");
    testResponse.setSurvey(testSurvey);
    testResponse.setStudentId("student1");
    testResponse.setQuestionId("q1");
    testResponse.setNumericRating(4);
    testResponse.setTextComment("Good course");
    testResponse.setSubmissionDate(LocalDateTime.now(ZoneId.systemDefault()));

    testResponseDto = new SurveyResponseDto();
    testResponseDto.setId("response1");
    testResponseDto.setSurveyId("survey1");
    testResponseDto.setStudentId("student1");
    testResponseDto.setQuestionId("q1");
    testResponseDto.setNumericRating(4);
    testResponseDto.setTextComment("Good course");
  }

  @Test
  void testGetResponsesBySurveyId() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.findBySurveyId("survey1")).thenReturn(Arrays.asList(testResponse));

    List<SurveyResponseDto> result = surveyResponseService.getResponsesBySurveyId("survey1",
        "user1");

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void testGetResponsesBySurveyId_SurveyNotFound() {
    when(surveyRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> surveyResponseService.getResponsesBySurveyId("nonexistent", "user1"));
  }

  @Test
  void testGetSurveyComments() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.findBySurveyId("survey1")).thenReturn(Arrays.asList(testResponse));

    List<SurveyResponseDto> result = surveyResponseService.getSurveyComments("survey1", "user1");

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(teacherSurveyMessageService).publishSurveyCommentsRequested("survey1", "user1");
  }

  @Test
  void testGetSurveyComments_FilterEmptyComments() {
    SurveyResponse noComment = new SurveyResponse();
    noComment.setId("response2");
    noComment.setSurvey(testSurvey);
    noComment.setStudentId("student2");
    noComment.setQuestionId("q2");
    noComment.setNumericRating(3);
    noComment.setTextComment("");

    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.findBySurveyId("survey1")).thenReturn(
        Arrays.asList(testResponse, noComment));

    List<SurveyResponseDto> result = surveyResponseService.getSurveyComments("survey1", "user1");

    assertEquals(1, result.size());
  }

  @Test
  void testGetSurveyResults() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.findBySurveyId("survey1")).thenReturn(Arrays.asList(testResponse));

    Map<String, Double> result = surveyResponseService.getSurveyResults("survey1", "user1");

    assertNotNull(result);
    assertTrue(result.containsKey("q1"));
    assertEquals(4.0, result.get("q1"));
    verify(teacherSurveyMessageService).publishSurveyResultsRequested("survey1", "user1");
  }

  @Test
  void testGetSurveyResults_MultipleResponses() {
    SurveyResponse response2 = new SurveyResponse();
    response2.setId("response2");
    response2.setSurvey(testSurvey);
    response2.setQuestionId("q1");
    response2.setNumericRating(5);

    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.findBySurveyId("survey1")).thenReturn(
        Arrays.asList(testResponse, response2));

    Map<String, Double> result = surveyResponseService.getSurveyResults("survey1", "user1");

    assertEquals(4.5, result.get("q1"));
  }

  @Test
  void testGetResponsesByStudentId() {
    when(responseRepository.findByStudentId("student1")).thenReturn(Arrays.asList(testResponse));

    List<SurveyResponseDto> result = surveyResponseService.getResponsesByStudentId("student1");

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void testGetAvailableSurveysForStudent() {
    when(surveyRepository.findByStatus(SurveyStatus.ACTIVE)).thenReturn(Arrays.asList(testSurvey));

    List<TeacherSurvey> result = surveyResponseService.getAvailableSurveysForStudent("student1");

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void testSubmitSurveyResponses_Success() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.existsBySurveyIdAndStudentId("survey1", "student1")).thenReturn(false);
    when(responseRepository.saveAll(anyList())).thenReturn(Arrays.asList(testResponse));

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);
    List<SurveyResponseDto> result = surveyResponseService.submitSurveyResponses("survey1",
        responses, "student1");

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(surveyResponseMessageService).publishSurveyResponsesSubmitted(anyList(), eq("survey1"));
  }

  @Test
  void testSubmitSurveyResponses_SurveyNotFound() {
    when(surveyRepository.findById("nonexistent")).thenReturn(Optional.empty());

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    assertThrows(ResourceNotFoundException.class,
        () -> surveyResponseService.submitSurveyResponses("nonexistent", responses, "student1"));
  }

  @Test
  void testSubmitSurveyResponses_SurveyClosed() {
    testSurvey.setStatus(SurveyStatus.CLOSED);
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    assertThrows(SurveyClosedException.class,
        () -> surveyResponseService.submitSurveyResponses("survey1", responses, "student1"));
  }

  @Test
  void testSubmitSurveyResponses_DuplicateResponse() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.existsBySurveyIdAndStudentId("survey1", "student1")).thenReturn(true);

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    assertThrows(DuplicateResponseException.class,
        () -> surveyResponseService.submitSurveyResponses("survey1", responses, "student1"));
  }

  @Test
  void testSubmitSurveyResponses_DuplicateQuestions() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.existsBySurveyIdAndStudentId("survey1", "student1")).thenReturn(false);

    SurveyResponseDto dto2 = new SurveyResponseDto();
    dto2.setQuestionId("q1");
    dto2.setNumericRating(3);

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto, dto2);

    assertThrows(IllegalArgumentException.class,
        () -> surveyResponseService.submitSurveyResponses("survey1", responses, "student1"));
  }

  @Test
  void testSubmitSurveyResponses_EmptyQuestionId() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.existsBySurveyIdAndStudentId("survey1", "student1")).thenReturn(false);

    testResponseDto.setQuestionId("");

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    assertThrows(IllegalArgumentException.class,
        () -> surveyResponseService.submitSurveyResponses("survey1", responses, "student1"));
  }

  @Test
  void testSubmitSurveyResponses_NoRatingNoComment() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.existsBySurveyIdAndStudentId("survey1", "student1")).thenReturn(false);

    testResponseDto.setNumericRating(null);
    testResponseDto.setTextComment(null);

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    assertThrows(IllegalArgumentException.class,
        () -> surveyResponseService.submitSurveyResponses("survey1", responses, "student1"));
  }

  @Test
  void testSubmitSurveyResponses_InvalidRatingLow() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.existsBySurveyIdAndStudentId("survey1", "student1")).thenReturn(false);

    testResponseDto.setNumericRating(0);

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    assertThrows(IllegalArgumentException.class,
        () -> surveyResponseService.submitSurveyResponses("survey1", responses, "student1"));
  }

  @Test
  void testSubmitSurveyResponses_InvalidRatingHigh() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.existsBySurveyIdAndStudentId("survey1", "student1")).thenReturn(false);

    testResponseDto.setNumericRating(6);

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    assertThrows(IllegalArgumentException.class,
        () -> surveyResponseService.submitSurveyResponses("survey1", responses, "student1"));
  }

  @Test
  void testSubmitSurveyResponses_CommentTooLong() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.existsBySurveyIdAndStudentId("survey1", "student1")).thenReturn(false);

    testResponseDto.setTextComment("a".repeat(1001));

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    assertThrows(IllegalArgumentException.class,
        () -> surveyResponseService.submitSurveyResponses("survey1", responses, "student1"));
  }

  @Test
  void testSubmitSurveyResponses_OnlyComment() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.existsBySurveyIdAndStudentId("survey1", "student1")).thenReturn(false);
    when(responseRepository.saveAll(anyList())).thenReturn(Arrays.asList(testResponse));

    testResponseDto.setNumericRating(null);

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);
    List<SurveyResponseDto> result = surveyResponseService.submitSurveyResponses("survey1",
        responses, "student1");

    assertNotNull(result);
  }

  @Test
  void testSubmitSurveyResponses_WithMessagingError() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.existsBySurveyIdAndStudentId("survey1", "student1")).thenReturn(false);
    when(responseRepository.saveAll(anyList())).thenReturn(Arrays.asList(testResponse));
    doThrow(new RuntimeException("Messaging error")).when(surveyResponseMessageService)
        .publishSurveyResponsesSubmitted(anyList(), anyString());

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);
    List<SurveyResponseDto> result = surveyResponseService.submitSurveyResponses("survey1",
        responses, "student1");

    assertNotNull(result);
  }

  @Test
  void testCreateResponse_Success() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(responseRepository.save(any(SurveyResponse.class))).thenReturn(testResponse);

    SurveyResponseDto result = surveyResponseService.createResponse(testResponseDto);

    assertNotNull(result);
    verify(surveyResponseMessageService).publishSurveyResponseSubmitted(any(SurveyResponseDto.class));
  }

  @Test
  void testCreateResponse_SurveyNotFound() {
    when(surveyRepository.findById("nonexistent")).thenReturn(Optional.empty());

    testResponseDto.setSurveyId("nonexistent");

    assertThrows(ResourceNotFoundException.class,
        () -> surveyResponseService.createResponse(testResponseDto));
  }

  @Test
  void testCreateResponse_SurveyClosed() {
    testSurvey.setStatus(SurveyStatus.CLOSED);
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));

    assertThrows(SurveyClosedException.class,
        () -> surveyResponseService.createResponse(testResponseDto));
  }
}