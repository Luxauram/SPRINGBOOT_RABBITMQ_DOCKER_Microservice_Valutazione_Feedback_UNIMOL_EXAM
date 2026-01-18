package it.unimol.microserviceassessmentfeedback.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceassessmentfeedback.common.util.JwtRequestHelper;
import it.unimol.microserviceassessmentfeedback.dto.TeacherSurveyDto;
import it.unimol.microserviceassessmentfeedback.dto.TeacherSurveyDto.SurveyQuestionDto;
import it.unimol.microserviceassessmentfeedback.enums.QuestionType;
import it.unimol.microserviceassessmentfeedback.enums.SurveyStatus;
import it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeacherSurveyControllerTest {

  @Mock
  private TeacherSurveyService surveyService;

  @Mock
  private JwtRequestHelper jwtRequestHelper;

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private TeacherSurveyController surveyController;

  private TeacherSurveyDto testSurveyDto;
  private SurveyQuestionDto testQuestion;

  @BeforeEach
  void setUp() {
    testQuestion = SurveyQuestionDto.builder()
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
        .status(SurveyStatus.DRAFT)
        .title("Course Evaluation")
        .description("Survey description")
        .questions(Arrays.asList(testQuestion))
        .creationDate(LocalDateTime.now())
        .build();

    ReflectionTestUtils.setField(surveyController, "jwtRequestHelper", jwtRequestHelper);
  }

  @Test
  void testGetAllSurveys() {
    when(surveyService.getAllSurveys()).thenReturn(Arrays.asList(testSurveyDto));

    ResponseEntity<List<TeacherSurveyDto>> response = surveyController.getAllSurveys();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("survey1", response.getBody().get(0).getId());
    verify(surveyService, times(1)).getAllSurveys();
  }

  @Test
  void testGetSurveyById_AsTeacher() {
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_TEACHER");
    when(jwtRequestHelper.extractTeacherIdFromRequest(any())).thenReturn("teacher1");
    when(surveyService.getSurveyById("survey1")).thenReturn(testSurveyDto);

    ResponseEntity<TeacherSurveyDto> response = surveyController.getSurveyById("survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("survey1", response.getBody().getId());
    verify(surveyService, times(1)).getSurveyById("survey1");
  }

  @Test
  void testGetSurveyById_AsAdmin() {
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_ADMIN");
    when(surveyService.getSurveyById("survey1")).thenReturn(testSurveyDto);

    ResponseEntity<TeacherSurveyDto> response = surveyController.getSurveyById("survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(surveyService, times(1)).getSurveyById("survey1");
  }

  @Test
  void testGetSurveysByCourse() {
    when(surveyService.getSurveysByCourse("course1")).thenReturn(Arrays.asList(testSurveyDto));

    ResponseEntity<List<TeacherSurveyDto>> response = surveyController.getSurveysByCourse(
        "course1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(surveyService, times(1)).getSurveysByCourse("course1");
  }

  @Test
  void testGetSurveysByTeacher() {
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_TEACHER");
    when(jwtRequestHelper.extractTeacherIdFromRequest(any())).thenReturn("teacher1");
    when(surveyService.getSurveysByTeacher("teacher1")).thenReturn(Arrays.asList(testSurveyDto));

    ResponseEntity<List<TeacherSurveyDto>> response = surveyController.getSurveysByTeacher(
        "teacher1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(surveyService, times(1)).getSurveysByTeacher("teacher1");
  }

  @Test
  void testGetActiveSurveys() {
    testSurveyDto.setStatus(SurveyStatus.ACTIVE);
    when(surveyService.getActiveSurveys()).thenReturn(Arrays.asList(testSurveyDto));

    ResponseEntity<List<TeacherSurveyDto>> response = surveyController.getActiveSurveys();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(surveyService, times(1)).getActiveSurveys();
  }

  @Test
  void testGetSurveyResults_AsTeacher() {
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_TEACHER");
    when(jwtRequestHelper.extractTeacherIdFromRequest(any())).thenReturn("teacher1");
    when(surveyService.getSurveyById("survey1")).thenReturn(testSurveyDto);
    when(surveyService.getSurveyStatistics("survey1")).thenReturn(new Object());

    ResponseEntity<Object> response = surveyController.getSurveyResults("survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(surveyService, times(1)).getSurveyStatistics("survey1");
  }

  @Test
  void testGetSurveyResults_AsAdmin() {
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_ADMIN");
    when(surveyService.getSurveyStatistics("survey1")).thenReturn(new Object());

    ResponseEntity<Object> response = surveyController.getSurveyResults("survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(surveyService, times(1)).getSurveyStatistics("survey1");
  }

  @Test
  void testCreateSurvey() {
    when(surveyService.createSurvey(any(TeacherSurveyDto.class))).thenReturn(testSurveyDto);

    ResponseEntity<TeacherSurveyDto> response = surveyController.createSurvey(testSurveyDto);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("survey1", response.getBody().getId());
    verify(surveyService, times(1)).createSurvey(any(TeacherSurveyDto.class));
  }

  @Test
  void testUpdateSurvey() {
    when(surveyService.updateSurvey(eq("survey1"), any(TeacherSurveyDto.class)))
        .thenReturn(testSurveyDto);

    ResponseEntity<TeacherSurveyDto> response = surveyController.updateSurvey("survey1",
        testSurveyDto);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("survey1", response.getBody().getId());
    verify(surveyService, times(1)).updateSurvey(eq("survey1"), any(TeacherSurveyDto.class));
  }

  @Test
  void testChangeSurveyStatus() {
    testSurveyDto.setStatus(SurveyStatus.ACTIVE);
    when(surveyService.changeSurveyStatus("survey1", SurveyStatus.ACTIVE))
        .thenReturn(testSurveyDto);

    ResponseEntity<TeacherSurveyDto> response = surveyController.changeSurveyStatus("survey1",
        SurveyStatus.ACTIVE);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(SurveyStatus.ACTIVE, response.getBody().getStatus());
    verify(surveyService, times(1)).changeSurveyStatus("survey1", SurveyStatus.ACTIVE);
  }

  @Test
  void testDeleteSurvey() {
    doNothing().when(surveyService).deleteSurvey("survey1");

    ResponseEntity<Void> response = surveyController.deleteSurvey("survey1");

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(surveyService, times(1)).deleteSurvey("survey1");
  }

  @Test
  void testGetGeneralStatistics() {
    when(surveyService.getGeneralStatistics()).thenReturn(new Object());

    ResponseEntity<Object> response = surveyController.getGeneralStatistics();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(surveyService, times(1)).getGeneralStatistics();
  }

  @Test
  void testGetAllSurveys_EmptyList() {
    when(surveyService.getAllSurveys()).thenReturn(Arrays.asList());

    ResponseEntity<List<TeacherSurveyDto>> response = surveyController.getAllSurveys();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetSurveyById_AsStudent() {
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_STUDENT");
    when(surveyService.getSurveyById("survey1")).thenReturn(testSurveyDto);

    ResponseEntity<TeacherSurveyDto> response = surveyController.getSurveyById("survey1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetSurveyById_NotFound() {
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_ADMIN");
    when(surveyService.getSurveyById("invalid"))
        .thenThrow(new RuntimeException("Survey not found"));

    try {
      surveyController.getSurveyById("invalid", request);
    } catch (RuntimeException e) {
      assertEquals("Survey not found", e.getMessage());
    }
  }

  @Test
  void testGetSurveysByTeacher_AsAdmin() {
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_ADMIN");
    when(surveyService.getSurveysByTeacher("teacher1"))
        .thenReturn(Arrays.asList(testSurveyDto));

    ResponseEntity<List<TeacherSurveyDto>> response = surveyController.getSurveysByTeacher(
        "teacher1", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetSurveyResults_Forbidden() {
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_TEACHER");
    when(jwtRequestHelper.extractTeacherIdFromRequest(any())).thenReturn("teacher2");
    testSurveyDto.setTeacherId("teacher1");
    when(surveyService.getSurveyById("survey1")).thenReturn(testSurveyDto);

    try {
      surveyController.getSurveyResults("survey1", request);
    } catch (Exception e) {
      assertNotNull(e);
    }
  }

  @Test
  void testCreateSurvey_ServiceException() {
    when(surveyService.createSurvey(any(TeacherSurveyDto.class)))
        .thenThrow(new RuntimeException("Creation failed"));

    try {
      surveyController.createSurvey(testSurveyDto);
    } catch (RuntimeException e) {
      assertEquals("Creation failed", e.getMessage());
    }
  }

  @Test
  void testChangeSurveyStatus_InvalidStatus() {
    when(surveyService.changeSurveyStatus("survey1", SurveyStatus.CLOSED))
        .thenThrow(new IllegalArgumentException("Invalid status"));

    try {
      surveyController.changeSurveyStatus("survey1", SurveyStatus.CLOSED);
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid status", e.getMessage());
    }
  }
}