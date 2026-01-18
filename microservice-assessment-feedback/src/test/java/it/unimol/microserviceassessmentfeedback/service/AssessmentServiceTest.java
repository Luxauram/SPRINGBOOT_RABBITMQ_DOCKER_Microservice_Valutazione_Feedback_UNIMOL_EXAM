package it.unimol.microserviceassessmentfeedback.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceassessmentfeedback.common.exception.ResourceNotFoundException;
import it.unimol.microserviceassessmentfeedback.dto.AssessmentDto;
import it.unimol.microserviceassessmentfeedback.enums.ReferenceType;
import it.unimol.microserviceassessmentfeedback.messaging.publishers.AssessmentMessageService;
import it.unimol.microserviceassessmentfeedback.model.Assessment;
import it.unimol.microserviceassessmentfeedback.repository.AssessmentRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

  @Mock
  private AssessmentRepository assessmentRepository;

  @Mock
  private AssessmentMessageService assessmentMessageService;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private AssessmentService assessmentService;

  private Assessment testAssessment;
  private AssessmentDto testAssessmentDto;

  @BeforeEach
  void setUp() {
    testAssessment = new Assessment();
    testAssessment.setId("assessment1");
    testAssessment.setStudentId("student1");
    testAssessment.setTeacherId("teacher1");
    testAssessment.setCourseId("course1");
    testAssessment.setReferenceId("ref1");
    testAssessment.setReferenceType(ReferenceType.ASSIGNMENT);
    testAssessment.setScore(85.0);
    testAssessment.setNotes("Good work");
    testAssessment.setAssessmentDate(LocalDateTime.now());

    testAssessmentDto = new AssessmentDto();
    testAssessmentDto.setId("assessment1");
    testAssessmentDto.setStudentId("student1");
    testAssessmentDto.setTeacherId("teacher1");
    testAssessmentDto.setCourseId("course1");
    testAssessmentDto.setReferenceId("ref1");
    testAssessmentDto.setReferenceType(ReferenceType.ASSIGNMENT);
    testAssessmentDto.setScore(85.0);
    testAssessmentDto.setNotes("Good work");
  }

  @Test
  void testGetAllAssessments() {
    when(assessmentRepository.findAll()).thenReturn(Arrays.asList(testAssessment));

    List<AssessmentDto> result = assessmentService.getAllAssessments();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("assessment1", result.get(0).getId());
    verify(assessmentRepository, times(1)).findAll();
  }

  @Test
  void testGetAllAssessments_Empty() {
    when(assessmentRepository.findAll()).thenReturn(Collections.emptyList());

    List<AssessmentDto> result = assessmentService.getAllAssessments();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetAssessmentById_AsTeacher() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn("teacher1");
    when(authentication.getAuthorities()).thenReturn(
        (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_TEACHER")));
    when(assessmentRepository.findById("assessment1")).thenReturn(Optional.of(testAssessment));

    AssessmentDto result = assessmentService.getAssessmentById("assessment1");

    assertNotNull(result);
    assertEquals("assessment1", result.getId());
  }

  @Test
  void testGetAssessmentById_AsStudent_Authorized() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn("student1");
    when(authentication.getAuthorities()).thenReturn(
        (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_STUDENT")));
    when(assessmentRepository.findById("assessment1")).thenReturn(Optional.of(testAssessment));

    AssessmentDto result = assessmentService.getAssessmentById("assessment1");

    assertNotNull(result);
    assertEquals("assessment1", result.getId());
  }

  @Test
  void testGetAssessmentById_NotAuthenticated() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(assessmentRepository.findById("assessment1")).thenReturn(Optional.of(testAssessment));

    assertThrows(AccessDeniedException.class,
        () -> assessmentService.getAssessmentById("assessment1"));
  }

  @Test
  void testGetAssessmentById_NotFound() {
    when(assessmentRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> assessmentService.getAssessmentById("nonexistent"));
  }

  @Test
  void testGetAssessmentsByStudentId_AsStudent_Authorized() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn("student1");
    when(authentication.getAuthorities()).thenReturn(
        (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_STUDENT")));
    when(assessmentRepository.findByStudentId("student1")).thenReturn(
        Arrays.asList(testAssessment));

    List<AssessmentDto> result = assessmentService.getAssessmentsByStudentId("student1");

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void testGetAssessmentsByStudentId_AsTeacher() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn("teacher1");
    when(authentication.getAuthorities()).thenReturn(
        (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_TEACHER")));
    when(assessmentRepository.findByStudentId("student1")).thenReturn(
        Arrays.asList(testAssessment));

    List<AssessmentDto> result = assessmentService.getAssessmentsByStudentId("student1");

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void testGetAssessmentsByAssignment() {
    when(assessmentRepository.findByReferenceIdAndReferenceType("assignment1",
        ReferenceType.ASSIGNMENT))
        .thenReturn(Arrays.asList(testAssessment));

    List<AssessmentDto> result = assessmentService.getAssessmentsByAssignment("assignment1");

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void testGetAssessmentsByExam() {
    when(assessmentRepository.findByReferenceIdAndReferenceType("exam1", ReferenceType.EXAM))
        .thenReturn(Arrays.asList(testAssessment));

    List<AssessmentDto> result = assessmentService.getAssessmentsByExam("exam1");

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void testGetAssessmentsByCourse() {
    when(assessmentRepository.findByCourseId("course1")).thenReturn(Arrays.asList(testAssessment));

    List<AssessmentDto> result = assessmentService.getAssessmentsByCourse("course1");

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void testCreateAssessment_Success() {
    when(assessmentRepository.save(any(Assessment.class))).thenReturn(testAssessment);
    doNothing().when(assessmentMessageService).publishAssessmentCreated(any(AssessmentDto.class));

    AssessmentDto result = assessmentService.createAssessment(testAssessmentDto);

    assertNotNull(result);
    assertEquals("assessment1", result.getId());
    verify(assessmentRepository, times(1)).save(any(Assessment.class));
    verify(assessmentMessageService, times(1)).publishAssessmentCreated(any(AssessmentDto.class));
  }

  @Test
  void testCreateAssessment_WithMessagingError() {
    when(assessmentRepository.save(any(Assessment.class))).thenReturn(testAssessment);
    doThrow(new RuntimeException("Messaging error")).when(assessmentMessageService)
        .publishAssessmentCreated(any(AssessmentDto.class));

    AssessmentDto result = assessmentService.createAssessment(testAssessmentDto);

    assertNotNull(result);
    verify(assessmentRepository, times(1)).save(any(Assessment.class));
  }

  @Test
  void testCreateAssessment_MissingStudentId() {
    testAssessmentDto.setStudentId(null);

    assertThrows(IllegalArgumentException.class,
        () -> assessmentService.createAssessment(testAssessmentDto));
  }

  @Test
  void testCreateAssessment_MissingTeacherId() {
    testAssessmentDto.setTeacherId(null);

    assertThrows(IllegalArgumentException.class,
        () -> assessmentService.createAssessment(testAssessmentDto));
  }

  @Test
  void testCreateAssessment_MissingCourseId() {
    testAssessmentDto.setCourseId(null);

    assertThrows(IllegalArgumentException.class,
        () -> assessmentService.createAssessment(testAssessmentDto));
  }

  @Test
  void testCreateAssessment_MissingReferenceId() {
    testAssessmentDto.setReferenceId(null);

    assertThrows(IllegalArgumentException.class,
        () -> assessmentService.createAssessment(testAssessmentDto));
  }

  @Test
  void testCreateAssessment_MissingReferenceType() {
    testAssessmentDto.setReferenceType(null);

    assertThrows(IllegalArgumentException.class,
        () -> assessmentService.createAssessment(testAssessmentDto));
  }

  @Test
  void testCreateAssessment_MissingScore() {
    testAssessmentDto.setScore(null);

    assertThrows(IllegalArgumentException.class,
        () -> assessmentService.createAssessment(testAssessmentDto));
  }

  @Test
  void testUpdateAssessment_Success() {
    when(assessmentRepository.findById("assessment1")).thenReturn(Optional.of(testAssessment));
    when(assessmentRepository.save(any(Assessment.class))).thenReturn(testAssessment);

    AssessmentDto updateDto = new AssessmentDto();
    updateDto.setScore(90.0);
    updateDto.setNotes("Excellent work");

    AssessmentDto result = assessmentService.updateAssessment("assessment1", updateDto);

    assertNotNull(result);
    verify(assessmentRepository, times(1)).save(any(Assessment.class));
    verify(assessmentMessageService, times(1)).publishAssessmentUpdated(any(AssessmentDto.class));
  }

  @Test
  void testUpdateAssessment_NotFound() {
    when(assessmentRepository.findById("nonexistent")).thenReturn(Optional.empty());

    AssessmentDto updateDto = new AssessmentDto();
    updateDto.setScore(90.0);

    assertThrows(ResourceNotFoundException.class,
        () -> assessmentService.updateAssessment("nonexistent", updateDto));
  }

  @Test
  void testUpdateAssessment_OnlyScore() {
    when(assessmentRepository.findById("assessment1")).thenReturn(Optional.of(testAssessment));
    when(assessmentRepository.save(any(Assessment.class))).thenReturn(testAssessment);

    AssessmentDto updateDto = new AssessmentDto();
    updateDto.setScore(95.0);

    AssessmentDto result = assessmentService.updateAssessment("assessment1", updateDto);

    assertNotNull(result);
  }

  @Test
  void testUpdateAssessment_OnlyNotes() {
    when(assessmentRepository.findById("assessment1")).thenReturn(Optional.of(testAssessment));
    when(assessmentRepository.save(any(Assessment.class))).thenReturn(testAssessment);

    AssessmentDto updateDto = new AssessmentDto();
    updateDto.setNotes("Updated notes");

    AssessmentDto result = assessmentService.updateAssessment("assessment1", updateDto);

    assertNotNull(result);
  }

  @Test
  void testDeleteAssessment_Success() {
    when(assessmentRepository.existsById("assessment1")).thenReturn(true);
    doNothing().when(assessmentRepository).deleteById("assessment1");
    doNothing().when(assessmentMessageService).publishAssessmentDeleted("assessment1");

    assertDoesNotThrow(() -> assessmentService.deleteAssessment("assessment1"));

    verify(assessmentRepository, times(1)).deleteById("assessment1");
    verify(assessmentMessageService, times(1)).publishAssessmentDeleted("assessment1");
  }

  @Test
  void testDeleteAssessment_NotFound() {
    when(assessmentRepository.existsById("nonexistent")).thenReturn(false);

    assertThrows(ResourceNotFoundException.class,
        () -> assessmentService.deleteAssessment("nonexistent"));
  }

  @Test
  void testDeleteAssessment_WithMessagingError() {
    when(assessmentRepository.existsById("assessment1")).thenReturn(true);
    doNothing().when(assessmentRepository).deleteById("assessment1");
    doThrow(new RuntimeException("Messaging error")).when(assessmentMessageService)
        .publishAssessmentDeleted("assessment1");

    assertDoesNotThrow(() -> assessmentService.deleteAssessment("assessment1"));
  }
}