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
import it.unimol.microserviceassessmentfeedback.dto.DetailedFeedbackDto;
import it.unimol.microserviceassessmentfeedback.enums.FeedbackCategory;
import it.unimol.microserviceassessmentfeedback.messaging.publishers.FeedbackMessageService;
import it.unimol.microserviceassessmentfeedback.model.Assessment;
import it.unimol.microserviceassessmentfeedback.model.DetailedFeedback;
import it.unimol.microserviceassessmentfeedback.repository.AssessmentRepository;
import it.unimol.microserviceassessmentfeedback.repository.DetailedFeedbackRepository;
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

@ExtendWith(MockitoExtension.class)
class DetailedFeedbackServiceTest {

  @Mock
  private DetailedFeedbackRepository feedbackRepository;

  @Mock
  private AssessmentRepository assessmentRepository;

  @Mock
  private FeedbackMessageService feedbackMessageService;

  @InjectMocks
  private DetailedFeedbackService feedbackService;

  private DetailedFeedback testFeedback;
  private DetailedFeedbackDto testFeedbackDto;
  private Assessment testAssessment;

  @BeforeEach
  void setUp() {
    testAssessment = new Assessment();
    testAssessment.setId("assessment1");
    testAssessment.setStudentId("student1");

    testFeedback = new DetailedFeedback();
    testFeedback.setId("feedback1");
    testFeedback.setAssessment(testAssessment);
    testFeedback.setFeedbackText("Great job!");
    testFeedback.setCategory(FeedbackCategory.CONTENT);
    testFeedback.setStrengths("Strong analytical skills");
    testFeedback.setImprovementAreas("Time management");

    testFeedbackDto = new DetailedFeedbackDto();
    testFeedbackDto.setId("feedback1");
    testFeedbackDto.setAssessmentId("assessment1");
    testFeedbackDto.setFeedbackText("Great job!");
    testFeedbackDto.setCategory(FeedbackCategory.CONTENT);
    testFeedbackDto.setStrengths("Strong analytical skills");
    testFeedbackDto.setImprovementAreas("Time management");
  }

  @Test
  void testGetAllFeedback() {
    when(feedbackRepository.findAll()).thenReturn(Arrays.asList(testFeedback));

    List<DetailedFeedbackDto> result = feedbackService.getAllFeedback();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("feedback1", result.get(0).getId());
    verify(feedbackRepository, times(1)).findAll();
  }

  @Test
  void testGetAllFeedback_Empty() {
    when(feedbackRepository.findAll()).thenReturn(Collections.emptyList());

    List<DetailedFeedbackDto> result = feedbackService.getAllFeedback();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetFeedbackByAssessmentId() {
    when(feedbackRepository.findByAssessmentId("assessment1")).thenReturn(
        Arrays.asList(testFeedback));

    List<DetailedFeedbackDto> result = feedbackService.getFeedbackByAssessmentId("assessment1");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("feedback1", result.get(0).getId());
  }

  @Test
  void testGetFeedbackByAssessmentId_Empty() {
    when(feedbackRepository.findByAssessmentId("assessment1")).thenReturn(Collections.emptyList());

    List<DetailedFeedbackDto> result = feedbackService.getFeedbackByAssessmentId("assessment1");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetFeedbackById() {
    when(feedbackRepository.findById("feedback1")).thenReturn(Optional.of(testFeedback));

    DetailedFeedbackDto result = feedbackService.getFeedbackById("feedback1");

    assertNotNull(result);
    assertEquals("feedback1", result.getId());
    assertEquals("assessment1", result.getAssessmentId());
  }

  @Test
  void testGetFeedbackById_NotFound() {
    when(feedbackRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> feedbackService.getFeedbackById("nonexistent"));
  }

  @Test
  void testGetFeedbackByStudentId() {
    when(feedbackRepository.findByStudentId("student1")).thenReturn(Arrays.asList(testFeedback));

    List<DetailedFeedbackDto> result = feedbackService.getFeedbackByStudentId("student1");

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void testGetFeedbackByStudentId_Empty() {
    when(feedbackRepository.findByStudentId("student1")).thenReturn(Collections.emptyList());

    List<DetailedFeedbackDto> result = feedbackService.getFeedbackByStudentId("student1");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testCreateFeedback_Success() {
    when(assessmentRepository.findById("assessment1")).thenReturn(Optional.of(testAssessment));
    when(feedbackRepository.save(any(DetailedFeedback.class))).thenReturn(testFeedback);
    doNothing().when(feedbackMessageService).publishFeedbackCreated(any(DetailedFeedbackDto.class));

    DetailedFeedbackDto result = feedbackService.createFeedback(testFeedbackDto);

    assertNotNull(result);
    assertEquals("feedback1", result.getId());
    verify(feedbackRepository, times(1)).save(any(DetailedFeedback.class));
    verify(feedbackMessageService, times(1)).publishFeedbackCreated(any(DetailedFeedbackDto.class));
  }

  @Test
  void testCreateFeedback_AssessmentNotFound() {
    when(assessmentRepository.findById("assessment1")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> feedbackService.createFeedback(testFeedbackDto));
  }

  @Test
  void testCreateFeedback_WithMessagingError() {
    when(assessmentRepository.findById("assessment1")).thenReturn(Optional.of(testAssessment));
    when(feedbackRepository.save(any(DetailedFeedback.class))).thenReturn(testFeedback);
    doThrow(new RuntimeException("Messaging error")).when(feedbackMessageService)
        .publishFeedbackCreated(any(DetailedFeedbackDto.class));

    DetailedFeedbackDto result = feedbackService.createFeedback(testFeedbackDto);

    assertNotNull(result);
    verify(feedbackRepository, times(1)).save(any(DetailedFeedback.class));
  }

  @Test
  void testUpdateFeedback_Success() {
    when(feedbackRepository.findById("feedback1")).thenReturn(Optional.of(testFeedback));
    when(feedbackRepository.save(any(DetailedFeedback.class))).thenReturn(testFeedback);
    doNothing().when(feedbackMessageService).publishFeedbackUpdated(any(DetailedFeedbackDto.class));

    DetailedFeedbackDto updateDto = new DetailedFeedbackDto();
    updateDto.setFeedbackText("Updated feedback");
    updateDto.setCategory(FeedbackCategory.PRESENTATION);
    updateDto.setStrengths("Updated strengths");
    updateDto.setImprovementAreas("Updated areas");

    DetailedFeedbackDto result = feedbackService.updateFeedback("feedback1", updateDto);

    assertNotNull(result);
    verify(feedbackRepository, times(1)).save(any(DetailedFeedback.class));
    verify(feedbackMessageService, times(1)).publishFeedbackUpdated(any(DetailedFeedbackDto.class));
  }

  @Test
  void testUpdateFeedback_NotFound() {
    when(feedbackRepository.findById("nonexistent")).thenReturn(Optional.empty());

    DetailedFeedbackDto updateDto = new DetailedFeedbackDto();
    updateDto.setFeedbackText("Updated feedback");

    assertThrows(ResourceNotFoundException.class,
        () -> feedbackService.updateFeedback("nonexistent", updateDto));
  }

  @Test
  void testUpdateFeedback_WithMessagingError() {
    when(feedbackRepository.findById("feedback1")).thenReturn(Optional.of(testFeedback));
    when(feedbackRepository.save(any(DetailedFeedback.class))).thenReturn(testFeedback);
    doThrow(new RuntimeException("Messaging error")).when(feedbackMessageService)
        .publishFeedbackUpdated(any(DetailedFeedbackDto.class));

    DetailedFeedbackDto updateDto = new DetailedFeedbackDto();
    updateDto.setFeedbackText("Updated feedback");

    DetailedFeedbackDto result = feedbackService.updateFeedback("feedback1", updateDto);

    assertNotNull(result);
    verify(feedbackRepository, times(1)).save(any(DetailedFeedback.class));
  }

  @Test
  void testDeleteFeedback_Success() {
    when(feedbackRepository.existsById("feedback1")).thenReturn(true);
    doNothing().when(feedbackRepository).deleteById("feedback1");
    doNothing().when(feedbackMessageService).publishFeedbackDeleted("feedback1");

    assertDoesNotThrow(() -> feedbackService.deleteFeedback("feedback1"));

    verify(feedbackRepository, times(1)).deleteById("feedback1");
    verify(feedbackMessageService, times(1)).publishFeedbackDeleted("feedback1");
  }

  @Test
  void testDeleteFeedback_NotFound() {
    when(feedbackRepository.existsById("nonexistent")).thenReturn(false);

    assertThrows(ResourceNotFoundException.class,
        () -> feedbackService.deleteFeedback("nonexistent"));
  }

  @Test
  void testDeleteFeedback_WithMessagingError() {
    when(feedbackRepository.existsById("feedback1")).thenReturn(true);
    doNothing().when(feedbackRepository).deleteById("feedback1");
    doThrow(new RuntimeException("Messaging error")).when(feedbackMessageService)
        .publishFeedbackDeleted("feedback1");

    assertDoesNotThrow(() -> feedbackService.deleteFeedback("feedback1"));

    verify(feedbackRepository, times(1)).deleteById("feedback1");
  }
}