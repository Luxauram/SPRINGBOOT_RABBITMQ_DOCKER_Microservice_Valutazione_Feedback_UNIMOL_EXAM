package it.unimol.microserviceassessmentfeedback.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceassessmentfeedback.common.util.JwtRequestHelper;
import it.unimol.microserviceassessmentfeedback.dto.DetailedFeedbackDto;
import it.unimol.microserviceassessmentfeedback.enums.FeedbackCategory;
import it.unimol.microserviceassessmentfeedback.service.DetailedFeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
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
class DetailedFeedbackControllerTest {

  @Mock
  private DetailedFeedbackService feedbackService;

  @Mock
  private JwtRequestHelper jwtRequestHelper;

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private DetailedFeedbackController feedbackController;

  private DetailedFeedbackDto testFeedbackDto;

  @BeforeEach
  void setUp() {
    testFeedbackDto = new DetailedFeedbackDto();
    testFeedbackDto.setId("feedback1");
    testFeedbackDto.setAssessmentId("assessment1");
    testFeedbackDto.setFeedbackText("Great job!");
    testFeedbackDto.setCategory(FeedbackCategory.CONTENT);
    testFeedbackDto.setStrengths("Strong analytical skills");
    testFeedbackDto.setImprovementAreas("Time management");

    ReflectionTestUtils.setField(feedbackController, "jwtRequestHelper", jwtRequestHelper);
  }

  @Test
  void testGetAllFeedback() {
    when(feedbackService.getAllFeedback()).thenReturn(Arrays.asList(testFeedbackDto));

    ResponseEntity<List<DetailedFeedbackDto>> response = feedbackController.getAllFeedback();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("feedback1", response.getBody().get(0).getId());
    verify(feedbackService, times(1)).getAllFeedback();
  }

  @Test
  void testGetFeedbackByAssessmentId() {
    when(feedbackService.getFeedbackByAssessmentId("assessment1"))
        .thenReturn(Arrays.asList(testFeedbackDto));

    ResponseEntity<List<DetailedFeedbackDto>> response = feedbackController
        .getFeedbackByAssessmentId("assessment1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("assessment1", response.getBody().get(0).getAssessmentId());
    verify(feedbackService, times(1)).getFeedbackByAssessmentId("assessment1");
  }

  @Test
  void testGetFeedbackById() {
    when(feedbackService.getFeedbackById("feedback1")).thenReturn(testFeedbackDto);

    ResponseEntity<DetailedFeedbackDto> response = feedbackController.getFeedbackById("feedback1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("feedback1", response.getBody().getId());
    assertEquals("Great job!", response.getBody().getFeedbackText());
    verify(feedbackService, times(1)).getFeedbackById("feedback1");
  }

  @Test
  void testGetPersonalFeedback() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.extractStudentIdFromRequest(any())).thenReturn("student1");
    when(feedbackService.getFeedbackByStudentId("student1"))
        .thenReturn(Arrays.asList(testFeedbackDto));

    ResponseEntity<List<DetailedFeedbackDto>> response = feedbackController.getPersonalFeedback(
        request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(feedbackService, times(1)).getFeedbackByStudentId("student1");
  }

  @Test
  void testCreateFeedback() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("teacher1");
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_TEACHER");
    when(feedbackService.createFeedback(any(DetailedFeedbackDto.class)))
        .thenReturn(testFeedbackDto);

    ResponseEntity<DetailedFeedbackDto> response = feedbackController.createFeedback(
        testFeedbackDto, request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("feedback1", response.getBody().getId());
    verify(feedbackService, times(1)).createFeedback(any(DetailedFeedbackDto.class));
  }

  @Test
  void testUpdateFeedback() {
    when(feedbackService.updateFeedback(eq("feedback1"), any(DetailedFeedbackDto.class)))
        .thenReturn(testFeedbackDto);

    ResponseEntity<DetailedFeedbackDto> response = feedbackController.updateFeedback("feedback1",
        testFeedbackDto);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("feedback1", response.getBody().getId());
    verify(feedbackService, times(1)).updateFeedback(eq("feedback1"),
        any(DetailedFeedbackDto.class));
  }

  @Test
  void testDeleteFeedback() {
    doNothing().when(feedbackService).deleteFeedback("feedback1");

    ResponseEntity<Void> response = feedbackController.deleteFeedback("feedback1");

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(feedbackService, times(1)).deleteFeedback("feedback1");
  }

  @Test
  void testGetFeedbackById_NotFound() {
    when(feedbackService.getFeedbackById("invalid"))
        .thenThrow(new RuntimeException("Feedback not found"));

    try {
      feedbackController.getFeedbackById("invalid");
    } catch (RuntimeException e) {
      assertEquals("Feedback not found", e.getMessage());
    }
  }

  @Test
  void testGetPersonalFeedback_NoStudentId() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn(null);
    when(jwtRequestHelper.extractStudentIdFromRequest(any())).thenReturn(null);

    try {
      feedbackController.getPersonalFeedback(request);
    } catch (Exception e) {
      assertNotNull(e);
    }
  }

  @Test
  void testCreateFeedback_NoRole() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn(null);
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn(null);

    try {
      feedbackController.createFeedback(testFeedbackDto, request);
    } catch (Exception e) {
      assertNotNull(e);
    }
  }





  @Test
  void testGetAllFeedback_ServiceException() {
    when(feedbackService.getAllFeedback())
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class,
        () -> feedbackController.getAllFeedback());
  }

  @Test
  void testGetAllFeedback_EmptyList() {
    when(feedbackService.getAllFeedback()).thenReturn(Arrays.asList());

    ResponseEntity<List<DetailedFeedbackDto>> response = feedbackController.getAllFeedback();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetFeedbackByAssessmentId_EmptyList() {
    when(feedbackService.getFeedbackByAssessmentId("assessment1"))
        .thenReturn(Arrays.asList());

    ResponseEntity<List<DetailedFeedbackDto>> response = feedbackController
        .getFeedbackByAssessmentId("assessment1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetFeedbackByAssessmentId_ServiceException() {
    when(feedbackService.getFeedbackByAssessmentId("assessment1"))
        .thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class,
        () -> feedbackController.getFeedbackByAssessmentId("assessment1"));
  }

  @Test
  void testGetFeedbackById_ServiceException() {
    when(feedbackService.getFeedbackById("feedback1"))
        .thenThrow(new RuntimeException("Not found"));

    assertThrows(RuntimeException.class,
        () -> feedbackController.getFeedbackById("feedback1"));
  }

  @Test
  void testGetPersonalFeedback_EmptyList() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.extractStudentIdFromRequest(any())).thenReturn("student1");
    when(feedbackService.getFeedbackByStudentId("student1"))
        .thenReturn(Arrays.asList());

    ResponseEntity<List<DetailedFeedbackDto>> response = feedbackController
        .getPersonalFeedback(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetPersonalFeedback_ServiceException() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.extractStudentIdFromRequest(any())).thenReturn("student1");
    when(feedbackService.getFeedbackByStudentId("student1"))
        .thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class,
        () -> feedbackController.getPersonalFeedback(request));
  }

  @Test
  void testCreateFeedback_ServiceException() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("teacher1");
    when(jwtRequestHelper.getUserRoleFromRequest(any())).thenReturn("ROLE_TEACHER");
    when(feedbackService.createFeedback(any(DetailedFeedbackDto.class)))
        .thenThrow(new RuntimeException("Creation failed"));

    assertThrows(RuntimeException.class,
        () -> feedbackController.createFeedback(testFeedbackDto, request));
  }

  @Test
  void testUpdateFeedback_GenericException() {
    when(feedbackService.updateFeedback(eq("feedback1"), any(DetailedFeedbackDto.class)))
        .thenThrow(new RuntimeException("Update failed"));

    assertThrows(RuntimeException.class,
        () -> feedbackController.updateFeedback("feedback1", testFeedbackDto));
  }

  @Test
  void testDeleteFeedback_ServiceException() {
    doThrow(new RuntimeException("Delete failed"))
        .when(feedbackService).deleteFeedback("feedback1");

    assertThrows(RuntimeException.class,
        () -> feedbackController.deleteFeedback("feedback1"));
  }
}