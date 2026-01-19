package it.unimol.microserviceassessmentfeedback.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceassessmentfeedback.common.exception.ResourceNotFoundException;
import it.unimol.microserviceassessmentfeedback.dto.TeacherSurveyDto;
import it.unimol.microserviceassessmentfeedback.dto.TeacherSurveyDto.SurveyQuestionDto;
import it.unimol.microserviceassessmentfeedback.enums.QuestionType;
import it.unimol.microserviceassessmentfeedback.enums.SurveyStatus;
import it.unimol.microserviceassessmentfeedback.messaging.publishers.TeacherSurveyMessageService;
import it.unimol.microserviceassessmentfeedback.model.TeacherSurvey;
import it.unimol.microserviceassessmentfeedback.repository.TeacherSurveyRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TeacherSurveyServiceTest {

  @Mock
  private TeacherSurveyRepository surveyRepository;

  @Mock
  private TeacherSurveyMessageService teacherSurveyMessageService;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private TeacherSurveyService surveyService;

  private TeacherSurvey testSurvey;
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

    testSurvey = new TeacherSurvey();
    testSurvey.setId("survey1");
    testSurvey.setCourseId("course1");
    testSurvey.setTeacherId("teacher1");
    testSurvey.setAcademicYear("2023/2024");
    testSurvey.setSemester(1);
    testSurvey.setStatus(SurveyStatus.DRAFT);
    testSurvey.setTitle("Course Evaluation");
    testSurvey.setDescription("Survey description");
    testSurvey.setQuestions(Arrays.asList(testQuestion));
    testSurvey.setCreationDate(LocalDateTime.now(ZoneId.systemDefault()));

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
        .build();
  }

  @Test
  void testGetAllSurveys() {
    when(surveyRepository.findAll()).thenReturn(Arrays.asList(testSurvey));

    List<TeacherSurveyDto> result = surveyService.getAllSurveys();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("survey1", result.get(0).getId());
  }

  @Test
  void testGetSurveyById() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));

    TeacherSurveyDto result = surveyService.getSurveyById("survey1");

    assertNotNull(result);
    assertEquals("survey1", result.getId());
  }

  @Test
  void testGetSurveyById_NotFound() {
    when(surveyRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> surveyService.getSurveyById("nonexistent"));
  }

  @Test
  void testGetSurveysByCourse() {
    when(surveyRepository.findByCourseId("course1")).thenReturn(Arrays.asList(testSurvey));

    List<TeacherSurveyDto> result = surveyService.getSurveysByCourse("course1");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("course1", result.get(0).getCourseId());
  }

  @Test
  void testGetSurveysByTeacher() {
    when(surveyRepository.findByTeacherId("teacher1")).thenReturn(Arrays.asList(testSurvey));

    List<TeacherSurveyDto> result = surveyService.getSurveysByTeacher("teacher1");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("teacher1", result.get(0).getTeacherId());
  }

  @Test
  void testGetActiveSurveys() {
    testSurvey.setStatus(SurveyStatus.ACTIVE);
    when(surveyRepository.findByStatus(SurveyStatus.ACTIVE)).thenReturn(Arrays.asList(testSurvey));

    List<TeacherSurveyDto> result = surveyService.getActiveSurveys();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(SurveyStatus.ACTIVE, result.get(0).getStatus());
  }

  @Test
  void testGetSurveyStatistics() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("teacher1");
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));

    Object result = surveyService.getSurveyStatistics("survey1");

    assertNotNull(result);
    verify(teacherSurveyMessageService).publishSurveyResultsRequested("survey1", "teacher1");
  }

  @Test
  void testGetSurveyStatistics_NotFound() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("teacher1");
    when(surveyRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> surveyService.getSurveyStatistics("nonexistent"));
  }

  @Test
  void testGetGeneralStatistics() {
    when(surveyRepository.count()).thenReturn(10L);
    when(surveyRepository.countByStatus(SurveyStatus.DRAFT)).thenReturn(3L);
    when(surveyRepository.countByStatus(SurveyStatus.ACTIVE)).thenReturn(5L);
    when(surveyRepository.countByStatus(SurveyStatus.CLOSED)).thenReturn(2L);

    Object result = surveyService.getGeneralStatistics();

    assertNotNull(result);
    verify(surveyRepository).count();
    verify(surveyRepository).countByStatus(SurveyStatus.DRAFT);
    verify(surveyRepository).countByStatus(SurveyStatus.ACTIVE);
    verify(surveyRepository).countByStatus(SurveyStatus.CLOSED);
  }

  @Test
  void testCreateSurvey_Success() {
    when(surveyRepository.existsByTeacherIdAndCourseIdAndAcademicYearAndSemester(
        anyString(), anyString(), anyString(), anyInt())).thenReturn(false);
    when(surveyRepository.save(any(TeacherSurvey.class))).thenReturn(testSurvey);

    TeacherSurveyDto result = surveyService.createSurvey(testSurveyDto);

    assertNotNull(result);
    assertEquals("survey1", result.getId());
    verify(surveyRepository).save(any(TeacherSurvey.class));
    verify(teacherSurveyMessageService).publishSurveyCompleted(any(TeacherSurveyDto.class));
  }

  @Test
  void testCreateSurvey_DuplicateExists() {
    when(surveyRepository.existsByTeacherIdAndCourseIdAndAcademicYearAndSemester(
        anyString(), anyString(), anyString(), anyInt())).thenReturn(true);

    assertThrows(IllegalArgumentException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_MissingTitle() {
    testSurveyDto.setTitle(null);

    assertThrows(IllegalArgumentException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_BlankTitle() {
    testSurveyDto.setTitle("   ");

    assertThrows(IllegalArgumentException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_NoQuestions() {
    testSurveyDto.setQuestions(Collections.emptyList());

    assertThrows(IllegalArgumentException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_NullQuestions() {
    testSurveyDto.setQuestions(null);

    assertThrows(IllegalArgumentException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_EmptyQuestionText() {
    SurveyQuestionDto invalidQuestion = SurveyQuestionDto.builder()
        .questionText("")
        .questionType(QuestionType.RATING)
        .minRating(1)
        .maxRating(5)
        .build();

    testSurveyDto.setQuestions(Arrays.asList(invalidQuestion));

    assertThrows(IllegalArgumentException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_NullQuestionType() {
    SurveyQuestionDto invalidQuestion = SurveyQuestionDto.builder()
        .questionText("Question?")
        .questionType(null)
        .build();

    testSurveyDto.setQuestions(Arrays.asList(invalidQuestion));

    assertThrows(IllegalArgumentException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_RatingWithoutMinMax() {
    SurveyQuestionDto invalidQuestion = SurveyQuestionDto.builder()
        .questionText("Rate this")
        .questionType(QuestionType.RATING)
        .build();

    testSurveyDto.setQuestions(Arrays.asList(invalidQuestion));

    assertThrows(NullPointerException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_RatingInvalidRange() {
    SurveyQuestionDto invalidQuestion = SurveyQuestionDto.builder()
        .questionText("Rate this")
        .questionType(QuestionType.RATING)
        .minRating(0)
        .maxRating(10)
        .build();

    testSurveyDto.setQuestions(Arrays.asList(invalidQuestion));

    assertThrows(IllegalArgumentException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_TextWithoutMaxLength() {
    SurveyQuestionDto invalidQuestion = SurveyQuestionDto.builder()
        .questionText("Comment here")
        .questionType(QuestionType.TEXT)
        .build();

    testSurveyDto.setQuestions(Arrays.asList(invalidQuestion));

    assertThrows(NullPointerException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_TextWithInvalidMaxLength() {
    SurveyQuestionDto invalidQuestion = SurveyQuestionDto.builder()
        .questionText("Comment here")
        .questionType(QuestionType.TEXT)
        .maxLengthText(-1)
        .build();

    testSurveyDto.setQuestions(Arrays.asList(invalidQuestion));

    assertThrows(IllegalArgumentException.class,
        () -> surveyService.createSurvey(testSurveyDto));
  }

  @Test
  void testCreateSurvey_WithMessagingError() {
    when(surveyRepository.existsByTeacherIdAndCourseIdAndAcademicYearAndSemester(
        anyString(), anyString(), anyString(), anyInt())).thenReturn(false);
    when(surveyRepository.save(any(TeacherSurvey.class))).thenReturn(testSurvey);
    doThrow(new RuntimeException("Messaging error")).when(teacherSurveyMessageService)
        .publishSurveyCompleted(any(TeacherSurveyDto.class));

    TeacherSurveyDto result = surveyService.createSurvey(testSurveyDto);

    assertNotNull(result);
    verify(surveyRepository).save(any(TeacherSurvey.class));
  }

  @Test
  void testUpdateSurvey_Success() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(surveyRepository.save(any(TeacherSurvey.class))).thenReturn(testSurvey);

    TeacherSurveyDto updateDto = TeacherSurveyDto.builder()
        .title("Updated Title")
        .description("Updated Description")
        .build();

    TeacherSurveyDto result = surveyService.updateSurvey("survey1", updateDto);

    assertNotNull(result);
    verify(surveyRepository).save(any(TeacherSurvey.class));
  }

  @Test
  void testUpdateSurvey_NotFound() {
    when(surveyRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> surveyService.updateSurvey("nonexistent", testSurveyDto));
  }

  @Test
  void testUpdateSurvey_NotInDraftStatus() {
    testSurvey.setStatus(SurveyStatus.ACTIVE);
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));

    assertThrows(IllegalStateException.class,
        () -> surveyService.updateSurvey("survey1", testSurveyDto));
  }

  @Test
  void testChangeSurveyStatus_DraftToActive() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(surveyRepository.save(any(TeacherSurvey.class))).thenReturn(testSurvey);

    TeacherSurveyDto result = surveyService.changeSurveyStatus("survey1", SurveyStatus.ACTIVE);

    assertNotNull(result);
    verify(surveyRepository).save(any(TeacherSurvey.class));
  }

  @Test
  void testChangeSurveyStatus_ActiveToClosed() {
    testSurvey.setStatus(SurveyStatus.ACTIVE);
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(surveyRepository.save(any(TeacherSurvey.class))).thenReturn(testSurvey);

    TeacherSurveyDto result = surveyService.changeSurveyStatus("survey1", SurveyStatus.CLOSED);

    assertNotNull(result);
    verify(teacherSurveyMessageService).publishSurveyCompleted(any(TeacherSurveyDto.class));
  }

  @Test
  void testChangeSurveyStatus_ClosedToActive() {
    testSurvey.setStatus(SurveyStatus.CLOSED);
    testSurvey.setClosingDate(LocalDateTime.now(ZoneId.systemDefault()));
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    when(surveyRepository.save(any(TeacherSurvey.class))).thenReturn(testSurvey);

    TeacherSurveyDto result = surveyService.changeSurveyStatus("survey1", SurveyStatus.ACTIVE);

    assertNotNull(result);
  }

  @Test
  void testChangeSurveyStatus_InvalidTransition() {
    testSurvey.setStatus(SurveyStatus.ACTIVE);
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));

    assertThrows(IllegalArgumentException.class,
        () -> surveyService.changeSurveyStatus("survey1", SurveyStatus.DRAFT));
  }

  @Test
  void testChangeSurveyStatus_NotFound() {
    when(surveyRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> surveyService.changeSurveyStatus("nonexistent", SurveyStatus.ACTIVE));
  }

  @Test
  void testDeleteSurvey_Success() {
    when(surveyRepository.findById("survey1")).thenReturn(Optional.of(testSurvey));
    doNothing().when(surveyRepository).deleteById("survey1");

    assertDoesNotThrow(() -> surveyService.deleteSurvey("survey1"));

    verify(surveyRepository).deleteById("survey1");
  }

  @Test
  void testDeleteSurvey_NotFound() {
    when(surveyRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> surveyService.deleteSurvey("nonexistent"));
  }
}