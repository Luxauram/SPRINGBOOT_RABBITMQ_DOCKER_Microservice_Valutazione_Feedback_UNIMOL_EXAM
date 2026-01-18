package it.unimol.microserviceassessmentfeedback.service;

import it.unimol.microserviceassessmentfeedback.common.exception.ResourceNotFoundException;
import it.unimol.microserviceassessmentfeedback.dto.DetailedFeedbackDto;
import it.unimol.microserviceassessmentfeedback.messaging.publishers.FeedbackMessageService;
import it.unimol.microserviceassessmentfeedback.model.Assessment;
import it.unimol.microserviceassessmentfeedback.model.DetailedFeedback;
import it.unimol.microserviceassessmentfeedback.repository.AssessmentRepository;
import it.unimol.microserviceassessmentfeedback.repository.DetailedFeedbackRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servizio per la gestione dei feedback dettagliati.
 * Fornisce operazioni CRUD per i feedback associati alle valutazioni
 * e pubblica eventi per la sincronizzazione con altri servizi.
 */
@Service
public class DetailedFeedbackService {

  private static final Logger logger = LoggerFactory.getLogger(DetailedFeedbackService.class);

  private final DetailedFeedbackRepository feedbackRepository;
  private final AssessmentRepository assessmentRepository;
  private final FeedbackMessageService feedbackMessageService;

  // ============ Costruttore ============
  /**
   * Costruttore con iniezione delle dipendenze.
   *
   * @param feedbackRepository il repository per i feedback
   * @param assessmentRepository il repository per le valutazioni
   * @param feedbackMessageService il servizio per la pubblicazione di eventi
   */
  public DetailedFeedbackService(DetailedFeedbackRepository feedbackRepository,
      AssessmentRepository assessmentRepository,
      FeedbackMessageService feedbackMessageService) {
    this.feedbackRepository = feedbackRepository;
    this.assessmentRepository = assessmentRepository;
    this.feedbackMessageService = feedbackMessageService;
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============
  /**
   * Recupera tutti i feedback presenti nel sistema.
   *
   * @return la lista di tutti i feedback
   */
  public List<DetailedFeedbackDto> getAllFeedback() {
    logger.debug("Recupero di tutti i feedback");
    List<DetailedFeedback> feedbacks = feedbackRepository.findAll();
    logger.debug("Trovati {} feedback totali", feedbacks.size());
    return feedbacks.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Recupera tutti i feedback associati a una valutazione.
   *
   * @param assessmentId l'ID della valutazione
   * @return la lista dei feedback per la valutazione
   */
  public List<DetailedFeedbackDto> getFeedbackByAssessmentId(String assessmentId) {
    logger.debug("Retrieving feedback for assessment ID: {}", assessmentId);
    return feedbackRepository.findByAssessmentId(assessmentId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Recupera un feedback specifico per ID.
   *
   * @param id l'ID del feedback
   * @return il feedback richiesto
   * @throws ResourceNotFoundException se il feedback non esiste
   */
  public DetailedFeedbackDto getFeedbackById(String id) {
    logger.debug("Retrieving feedback with ID: {}", id);
    DetailedFeedback feedback = feedbackRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Feedback non trovato con id: " + id));
    return convertToDto(feedback);
  }

  /**
   * Recupera tutti i feedback di uno studente.
   *
   * @param studentId l'ID dello studente
   * @return la lista dei feedback dello studente
   */
  public List<DetailedFeedbackDto> getFeedbackByStudentId(String studentId) {
    logger.debug("Retrieving all feedback for student ID: {}", studentId);
    return feedbackRepository.findByStudentId(studentId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  // ============ Metodi di Classe ============
  /**
   * Crea un nuovo feedback dettagliato.
   * Associa il feedback a una valutazione esistente e pubblica un evento.
   *
   * @param feedbackDto i dati del feedback da creare
   * @return il feedback creato
   * @throws ResourceNotFoundException se la valutazione non esiste
   */
  @Transactional
  public DetailedFeedbackDto createFeedback(DetailedFeedbackDto feedbackDto) {
    logger.info("Creating new feedback for assessment ID: {}", feedbackDto.getAssessmentId());

    Assessment assessment = assessmentRepository.findById(feedbackDto.getAssessmentId())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Valutazione non trovata con id: " + feedbackDto.getAssessmentId()));

    DetailedFeedback feedback = convertToEntity(feedbackDto);
    feedback.setAssessment(assessment);

    DetailedFeedback savedFeedback = feedbackRepository.save(feedback);
    DetailedFeedbackDto resultDto = convertToDto(savedFeedback);

    try {
      feedbackMessageService.publishFeedbackCreated(resultDto);
      logger.info("Feedback created event published for feedback ID: {}", resultDto.getId());
    } catch (Exception e) {
      logger.error("Failed to publish feedback created event for feedback ID: {}",
          resultDto.getId(), e);
    }

    return resultDto;
  }

  /**
   * Aggiorna un feedback esistente.
   * Pubblica un evento di aggiornamento.
   *
   * @param feedbackId l'ID del feedback da aggiornare
   * @param feedbackDto i nuovi dati del feedback
   * @return il feedback aggiornato
   * @throws ResourceNotFoundException se il feedback non esiste
   */
  @Transactional
  public DetailedFeedbackDto updateFeedback(String feedbackId, DetailedFeedbackDto feedbackDto) {
    logger.info("Updating feedback with ID: {}", feedbackId);
    logger.debug("Received feedbackDTO: {}", feedbackDto);

    try {
      logger.debug("Searching for feedback with ID: {}", feedbackId);
      DetailedFeedback existingFeedback = feedbackRepository.findById(feedbackId)
          .orElseThrow(() -> {
            logger.error("Feedback not found with ID: {}", feedbackId);
            return new ResourceNotFoundException("Feedback non trovato con id: " + feedbackId);
          });

      logger.info("Found existing feedback with ID: {} for assessment: {}",
          feedbackId, existingFeedback.getAssessment().getId());

      logger.debug("Current values - Text: {}, Category: {}, Strengths: {}, ImprovementAreas: {}",
          existingFeedback.getFeedbackText(),
          existingFeedback.getCategory(),
          existingFeedback.getStrengths(),
          existingFeedback.getImprovementAreas());

      logger.debug("New values - Text: {}, Category: {}, Strengths: {}, ImprovementAreas: {}",
          feedbackDto.getFeedbackText(),
          feedbackDto.getCategory(),
          feedbackDto.getStrengths(),
          feedbackDto.getImprovementAreas());

      existingFeedback.setFeedbackText(feedbackDto.getFeedbackText());
      existingFeedback.setCategory(feedbackDto.getCategory());
      existingFeedback.setStrengths(feedbackDto.getStrengths());
      existingFeedback.setImprovementAreas(feedbackDto.getImprovementAreas());

      logger.debug("About to save updated feedback with ID: {}", existingFeedback.getId());

      DetailedFeedback updatedFeedback = feedbackRepository.save(existingFeedback);
      logger.debug("Feedback saved successfully with ID: {}", updatedFeedback.getId());

      DetailedFeedbackDto resultDto = convertToDto(updatedFeedback);
      logger.debug("Converted to DTO: {}", resultDto);

      logger.info("Feedback updated successfully with ID: {}", feedbackId);

      try {
        feedbackMessageService.publishFeedbackUpdated(resultDto);
        logger.info("Feedback updated event published for feedback ID: {}", resultDto.getId());
      } catch (Exception e) {
        logger.error("Failed to publish feedback updated event for feedback ID: {}",
            resultDto.getId(), e);
      }

      return resultDto;

    } catch (Exception e) {
      logger.error("Error updating feedback with ID: {}", feedbackId, e);
      throw e;
    }
  }

  /**
   * Elimina un feedback.
   * Pubblica un evento di eliminazione.
   *
   * @param id l'ID del feedback da eliminare
   * @throws ResourceNotFoundException se il feedback non esiste
   */
  @Transactional
  public void deleteFeedback(String id) {
    logger.info("Deleting feedback with ID: {}", id);

    if (!feedbackRepository.existsById(id)) {
      throw new ResourceNotFoundException("Feedback non trovato con id: " + id);
    }

    feedbackRepository.deleteById(id);
    logger.info("Feedback deleted successfully with ID: {}", id);

    try {
      feedbackMessageService.publishFeedbackDeleted(id);
      logger.info("Feedback deleted event published for feedback ID: {}", id);
    } catch (Exception e) {
      logger.error("Failed to publish feedback deleted event for feedback ID: {}", id, e);
    }
  }

  private DetailedFeedbackDto convertToDto(DetailedFeedback feedback) {
    DetailedFeedbackDto dto = new DetailedFeedbackDto();
    dto.setId(feedback.getId());
    dto.setAssessmentId(feedback.getAssessment().getId());
    dto.setFeedbackText(feedback.getFeedbackText());
    dto.setCategory(feedback.getCategory());
    dto.setStrengths(feedback.getStrengths());
    dto.setImprovementAreas(feedback.getImprovementAreas());
    return dto;
  }

  private DetailedFeedback convertToEntity(DetailedFeedbackDto dto) {
    DetailedFeedback feedback = new DetailedFeedback();
    feedback.setId(dto.getId());
    feedback.setFeedbackText(dto.getFeedbackText());
    feedback.setCategory(dto.getCategory());
    feedback.setStrengths(dto.getStrengths());
    feedback.setImprovementAreas(dto.getImprovementAreas());
    return feedback;
  }
}