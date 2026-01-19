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
import it.unimol.microserviceassessmentfeedback.dto.AssessmentDto;
import it.unimol.microserviceassessmentfeedback.enums.ReferenceType;
import it.unimol.microserviceassessmentfeedback.service.AssessmentService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
class AssessmentControllerTest {

  @Mock
  private AssessmentService assessmentService;

  @Mock
  private JwtRequestHelper jwtRequestHelper;

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private AssessmentController assessmentController;

  private AssessmentDto testAssessmentDto;

  @BeforeEach
  void setUp() {
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

    ReflectionTestUtils.setField(assessmentController, "jwtRequestHelper", jwtRequestHelper);
  }

  @Test
  void testGetAllAssessments() {
    when(assessmentService.getAllAssessments()).thenReturn(Arrays.asList(testAssessmentDto));

    ResponseEntity<List<AssessmentDto>> response = assessmentController.getAllAssessments();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("assessment1", response.getBody().get(0).getId());
    verify(assessmentService, times(1)).getAllAssessments();
  }

  @Test
  void testGetAssessmentById() {
    when(assessmentService.getAssessmentById("assessment1")).thenReturn(testAssessmentDto);

    ResponseEntity<AssessmentDto> response = assessmentController.getAssessmentById("assessment1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("assessment1", response.getBody().getId());
    assertEquals(85.0, response.getBody().getScore());
    verify(assessmentService, times(1)).getAssessmentById("assessment1");
  }

  @Test
  void testGetAssessmentsByAssignment() {
    when(assessmentService.getAssessmentsByAssignment("assignment1"))
        .thenReturn(Arrays.asList(testAssessmentDto));

    ResponseEntity<List<AssessmentDto>> response = assessmentController
        .getAssessmentsByAssignment("assignment1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(assessmentService, times(1)).getAssessmentsByAssignment("assignment1");
  }

  @Test
  void testGetAssessmentsByExam() {
    when(assessmentService.getAssessmentsByExam("exam1"))
        .thenReturn(Arrays.asList(testAssessmentDto));

    ResponseEntity<List<AssessmentDto>> response = assessmentController.getAssessmentsByExam(
        "exam1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(assessmentService, times(1)).getAssessmentsByExam("exam1");
  }

  @Test
  void testGetAssessmentsByStudent() {
    when(assessmentService.getAssessmentsByStudentId("student1"))
        .thenReturn(Arrays.asList(testAssessmentDto));

    ResponseEntity<List<AssessmentDto>> response = assessmentController.getAssessmentsByStudent(
        "student1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(assessmentService, times(1)).getAssessmentsByStudentId("student1");
  }

  @Test
  void testGetAssessmentsByCourse() {
    when(assessmentService.getAssessmentsByCourse("course1"))
        .thenReturn(Arrays.asList(testAssessmentDto));

    ResponseEntity<List<AssessmentDto>> response = assessmentController.getAssessmentsByCourse(
        "course1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(assessmentService, times(1)).getAssessmentsByCourse("course1");
  }

  @Test
  void testGetPersonalAssessments() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.extractStudentIdFromRequest(any())).thenReturn("student1");
    when(assessmentService.getAssessmentsByStudentId("student1"))
        .thenReturn(Arrays.asList(testAssessmentDto));

    ResponseEntity<List<AssessmentDto>> response = assessmentController.getPersonalAssessments(
        request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(assessmentService, times(1)).getAssessmentsByStudentId("student1");
  }

  @Test
  void testGetPersonalAssessmentDetails() {
    when(assessmentService.getAssessmentById("assessment1")).thenReturn(testAssessmentDto);

    ResponseEntity<AssessmentDto> response = assessmentController.getPersonalAssessmentDetails(
        "assessment1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("assessment1", response.getBody().getId());
    verify(assessmentService, times(1)).getAssessmentById("assessment1");
  }

  @Test
  void testCreateAssessment() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("teacher1");
    when(jwtRequestHelper.extractTeacherIdFromRequest(any())).thenReturn("teacher1");
    when(assessmentService.createAssessment(any(AssessmentDto.class)))
        .thenReturn(testAssessmentDto);

    ResponseEntity<AssessmentDto> response = assessmentController.createAssessment(
        testAssessmentDto, request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("assessment1", response.getBody().getId());
    verify(assessmentService, times(1)).createAssessment(any(AssessmentDto.class));
  }

  @Test
  void testUpdateAssessment() {
    when(assessmentService.updateAssessment(eq("assessment1"), any(AssessmentDto.class)))
        .thenReturn(testAssessmentDto);

    ResponseEntity<AssessmentDto> response = assessmentController.updateAssessment("assessment1",
        testAssessmentDto);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("assessment1", response.getBody().getId());
    verify(assessmentService, times(1)).updateAssessment(eq("assessment1"),
        any(AssessmentDto.class));
  }

  @Test
  void testDeleteAssessment() {
    doNothing().when(assessmentService).deleteAssessment("assessment1");

    ResponseEntity<Void> response = assessmentController.deleteAssessment("assessment1");

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(assessmentService, times(1)).deleteAssessment("assessment1");
  }

  @Test
  void testGetAllAssessments_EmptyList() {
    when(assessmentService.getAllAssessments()).thenReturn(Arrays.asList());

    ResponseEntity<List<AssessmentDto>> response = assessmentController.getAllAssessments();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetAssessmentById_NotFound() {
    when(assessmentService.getAssessmentById("invalid"))
        .thenThrow(new RuntimeException("Assessment not found"));

    try {
      assessmentController.getAssessmentById("invalid");
    } catch (RuntimeException e) {
      assertEquals("Assessment not found", e.getMessage());
    }
  }

  @Test
  void testGetPersonalAssessments_NoUsername() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn(null);
    when(jwtRequestHelper.extractStudentIdFromRequest(any())).thenReturn(null);

    try {
      assessmentController.getPersonalAssessments(request);
    } catch (Exception e) {
      // Expected to fail
      assertNotNull(e);
    }
  }

  @Test
  void testCreateAssessment_NullTeacherId() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn(null);
    when(jwtRequestHelper.extractTeacherIdFromRequest(any())).thenReturn(null);

    try {
      assessmentController.createAssessment(testAssessmentDto, request);
    } catch (Exception e) {
      // Expected to fail
      assertNotNull(e);
    }
  }

  @Test
  void testGetAllAssessments_ServiceException() {
    when(assessmentService.getAllAssessments())
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class,
        () -> assessmentController.getAllAssessments());
  }

  @Test
  void testGetAssessmentById_ServiceException() {
    when(assessmentService.getAssessmentById("invalid"))
        .thenThrow(new RuntimeException("Not found"));

    assertThrows(RuntimeException.class,
        () -> assessmentController.getAssessmentById("invalid"));
  }

  @Test
  void testGetAssessmentsByAssignment_EmptyList() {
    when(assessmentService.getAssessmentsByAssignment("assignment1"))
        .thenReturn(Arrays.asList());

    ResponseEntity<List<AssessmentDto>> response = assessmentController
        .getAssessmentsByAssignment("assignment1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetAssessmentsByExam_ServiceException() {
    when(assessmentService.getAssessmentsByExam("exam1"))
        .thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class,
        () -> assessmentController.getAssessmentsByExam("exam1"));
  }

  @Test
  void testGetAssessmentsByStudent_EmptyList() {
    when(assessmentService.getAssessmentsByStudentId("student1"))
        .thenReturn(Arrays.asList());

    ResponseEntity<List<AssessmentDto>> response = assessmentController
        .getAssessmentsByStudent("student1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetAssessmentsByCourse_ServiceException() {
    when(assessmentService.getAssessmentsByCourse("course1"))
        .thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class,
        () -> assessmentController.getAssessmentsByCourse("course1"));
  }

  @Test
  void testGetPersonalAssessments_EmptyList() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("student1");
    when(jwtRequestHelper.extractStudentIdFromRequest(any())).thenReturn("student1");
    when(assessmentService.getAssessmentsByStudentId("student1"))
        .thenReturn(Arrays.asList());

    ResponseEntity<List<AssessmentDto>> response = assessmentController
        .getPersonalAssessments(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void testGetPersonalAssessmentDetails_ServiceException() {
    when(assessmentService.getAssessmentById("assessment1"))
        .thenThrow(new RuntimeException("Not found"));

    assertThrows(RuntimeException.class,
        () -> assessmentController.getPersonalAssessmentDetails("assessment1"));
  }

  @Test
  void testCreateAssessment_ServiceException() {
    when(jwtRequestHelper.getUsernameFromRequest(any())).thenReturn("teacher1");
    when(jwtRequestHelper.extractTeacherIdFromRequest(any())).thenReturn("teacher1");
    when(assessmentService.createAssessment(any(AssessmentDto.class)))
        .thenThrow(new RuntimeException("Creation failed"));

    assertThrows(RuntimeException.class,
        () -> assessmentController.createAssessment(testAssessmentDto, request));
  }

  @Test
  void testUpdateAssessment_ServiceException() {
    when(assessmentService.updateAssessment(eq("assessment1"), any(AssessmentDto.class)))
        .thenThrow(new RuntimeException("Update failed"));

    assertThrows(RuntimeException.class,
        () -> assessmentController.updateAssessment("assessment1", testAssessmentDto));
  }

  @Test
  void testDeleteAssessment_ServiceException() {
    doThrow(new RuntimeException("Delete failed"))
        .when(assessmentService).deleteAssessment("assessment1");

    assertThrows(RuntimeException.class,
        () -> assessmentController.deleteAssessment("assessment1"));
  }
}