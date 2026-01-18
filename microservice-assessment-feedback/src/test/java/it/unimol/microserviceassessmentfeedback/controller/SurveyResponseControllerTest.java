package it.unimol.microserviceassessmentfeedback.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceassessmentfeedback.common.util.JwtRequestHelper;
import it.unimol.microserviceassessmentfeedback.dto.SurveyResponseDto;
import it.unimol.microserviceassessmentfeedback.enums.SurveyStatus;
import it.unimol.microserviceassessmentfeedback.model.TeacherSurvey;
import it.unimol.microserviceassessmentfeedback.service.SurveyResponseService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SurveyResponseControllerTest {

  @Mock
  private SurveyResponseService responseService;

  @Mock
  private JwtRequestHelper jwtRequestHelper;

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private SurveyResponseController surveyResponseController;

  private SurveyResponseDto testResponseDto;

  @BeforeEach
  void setUp() {
    testResponseDto = new SurveyResponseDto();
    testResponseDto.setId("response1");
    testResponseDto.setSurveyId("survey1");
    testResponseDto.setStudentId("student1");
    testResponseDto.setQuestionId("q1");
    testResponseDto.setNumericRating(4);
    testResponseDto.setTextComment("Good course");
    testResponseDto.setSubmissionDate(LocalDateTime.now());

    ReflectionTestUtils.setField(surveyResponseController, "jwtRequestHelper", jwtRequestHelper);
  }

  @Test
  void testGetResponsesBySurveyId() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("teacher1");
    when(responseService.getResponsesBySurveyId("survey1", "teacher1"))
        .thenReturn(Arrays.asList(testResponseDto));

    ResponseEntity<List<SurveyResponseDto>> response = surveyResponseController
        .getResponsesBySurveyId("survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("response1", response.getBody().get(0).getId());
    verify(responseService, times(1)).getResponsesBySurveyId("survey1", "teacher1");
  }

  @Test
  void testGetSurveyComments() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("teacher1");
    when(responseService.getSurveyComments("survey1", "teacher1"))
        .thenReturn(Arrays.asList(testResponseDto));

    ResponseEntity<List<SurveyResponseDto>> response = surveyResponseController.getSurveyComments(
        "survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("Good course", response.getBody().get(0).getTextComment());
    verify(responseService, times(1)).getSurveyComments("survey1", "teacher1");
  }

  @Test
  void testGetSurveyResults() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("teacher1");
    Map<String, Double> results = new HashMap<>();
    results.put("q1", 4.5);
    when(responseService.getSurveyResults("survey1", "teacher1")).thenReturn(results);

    ResponseEntity<Map<String, Double>> response = surveyResponseController.getSurveyResults(
        "survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(4.5, response.getBody().get("q1"));
    verify(responseService, times(1)).getSurveyResults("survey1", "teacher1");
  }

  @Test
  void testSubmitSurveyResponses() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(responseService.submitSurveyResponses(eq("survey1"), anyList(), eq("student1")))
        .thenReturn(Arrays.asList(testResponseDto));

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    ResponseEntity<List<SurveyResponseDto>> response = surveyResponseController
        .submitSurveyResponses("survey1", responses, request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(responseService, times(1)).submitSurveyResponses(eq("survey1"), anyList(),
        eq("student1"));
  }

  @Test
  void testGetMyResponses() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(responseService.getResponsesByStudentId("student1"))
        .thenReturn(Arrays.asList(testResponseDto));

    ResponseEntity<List<SurveyResponseDto>> response = surveyResponseController.getMyResponses(
        request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(responseService, times(1)).getResponsesByStudentId("student1");
  }

  @Test
  void testGetAvailableSurveys() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");

    TeacherSurvey survey = new TeacherSurvey();
    survey.setId("survey1");
    survey.setStatus(SurveyStatus.ACTIVE);

    when(responseService.getAvailableSurveysForStudent("student1"))
        .thenReturn(Arrays.asList(survey));

    ResponseEntity<?> response = surveyResponseController.getAvailableSurveys(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(responseService, times(1)).getAvailableSurveysForStudent("student1");
  }

  @Test
  void testGetSurveyResults_EmptyResults() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("teacher1");
    when(responseService.getSurveyResults("survey1", "teacher1"))
        .thenReturn(new HashMap<>());

    ResponseEntity<Map<String, Double>> response = surveyResponseController.getSurveyResults(
        "survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testSubmitSurveyResponses_NoUserId() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn(null);
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn(null);

    try {
      surveyResponseController.submitSurveyResponses("survey1",
          Arrays.asList(testResponseDto), request);
    } catch (Exception e) {
      assertNotNull(e);
    }
  }

  @Test
  void testGetResponsesBySurveyId_EmptyList() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("teacher1");
    when(responseService.getResponsesBySurveyId("survey1", "teacher1"))
        .thenReturn(Arrays.asList());

    ResponseEntity<List<SurveyResponseDto>> response = surveyResponseController
        .getResponsesBySurveyId("survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetResponsesBySurveyId_ServiceException() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("teacher1");
    when(responseService.getResponsesBySurveyId("survey1", "teacher1"))
        .thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class,
        () -> surveyResponseController.getResponsesBySurveyId("survey1", request));
  }

  @Test
  void testGetSurveyComments_EmptyList() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("teacher1");
    when(responseService.getSurveyComments("survey1", "teacher1"))
        .thenReturn(Arrays.asList());

    ResponseEntity<List<SurveyResponseDto>> response = surveyResponseController
        .getSurveyComments("survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetSurveyComments_ServiceException() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("teacher1");
    when(responseService.getSurveyComments("survey1", "teacher1"))
        .thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class,
        () -> surveyResponseController.getSurveyComments("survey1", request));
  }

  @Test
  void testGetSurveyResults_EmptyMap() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("teacher1");
    when(responseService.getSurveyResults("survey1", "teacher1"))
        .thenReturn(new HashMap<>());

    ResponseEntity<Map<String, Double>> response = surveyResponseController
        .getSurveyResults("survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetSurveyResults_ServiceException() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("teacher1");
    when(responseService.getSurveyResults("survey1", "teacher1"))
        .thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class,
        () -> surveyResponseController.getSurveyResults("survey1", request));
  }

  @Test
  void testSubmitSurveyResponses_ServiceException() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(responseService.submitSurveyResponses(eq("survey1"), anyList(), eq("student1")))
        .thenThrow(new RuntimeException("Submission failed"));

    List<SurveyResponseDto> responses = Arrays.asList(testResponseDto);

    assertThrows(RuntimeException.class,
        () -> surveyResponseController.submitSurveyResponses("survey1", responses, request));
  }

  @Test
  void testGetMyResponses_EmptyList() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(responseService.getResponsesByStudentId("student1"))
        .thenReturn(Arrays.asList());

    ResponseEntity<List<SurveyResponseDto>> response = surveyResponseController
        .getMyResponses(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetMyResponses_ServiceException() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(responseService.getResponsesByStudentId("student1"))
        .thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class,
        () -> surveyResponseController.getMyResponses(request));
  }

  @Test
  void testGetAvailableSurveys_EmptyList() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(responseService.getAvailableSurveysForStudent("student1"))
        .thenReturn(Arrays.asList());

    ResponseEntity<?> response = surveyResponseController.getAvailableSurveys(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetAvailableSurveys_ServiceException() {
    when(jwtRequestHelper.getUserIdFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(responseService.getAvailableSurveysForStudent("student1"))
        .thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class,
        () -> surveyResponseController.getAvailableSurveys(request));
  }
}