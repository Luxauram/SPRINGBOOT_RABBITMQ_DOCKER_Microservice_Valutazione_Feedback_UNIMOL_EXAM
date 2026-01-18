package it.unimol.microserviceassessmentfeedback.service;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service per la gestione delle risposte ai questionari dei docenti.
 *
 * <p>Fornisce funzionalità di invio, recupero e validazione delle risposte
 * ai questionari, oltre alla pubblicazione degli eventi associati.</p>
 */
@Service
public class SurveyResponseService {

  private static final Logger logger = LoggerFactory.getLogger(DetailedFeedbackService.class);

  private final SurveyResponseRepository responseRepository;
  private final TeacherSurveyRepository surveyRepository;
  private final SurveyResponseMessageService surveyResponseMessageService;
  private final TeacherSurveyMessageService teacherSurveyMessageService;

  // ============ Costruttore ============
  /**
   * Costruttore del servizio SurveyResponseService.
   *
   * @param responseRepository repository delle risposte al questionario
   * @param surveyRepository repository dei questionari
   * @param surveyResponseMessageService servizio di pubblicazione eventi risposte
   * @param teacherSurveyMessageService servizio di pubblicazione eventi questionari
   */
  public SurveyResponseService(SurveyResponseRepository responseRepository,
      TeacherSurveyRepository surveyRepository,
      SurveyResponseMessageService surveyResponseMessageService,
      TeacherSurveyMessageService teacherSurveyMessageService) {
    this.responseRepository = responseRepository;
    this.surveyRepository = surveyRepository;
    this.surveyResponseMessageService = surveyResponseMessageService;
    this.teacherSurveyMessageService = teacherSurveyMessageService;
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============
  /**
   * Restituisce tutte le risposte associate a un questionario.
   *
   * @param surveyId identificativo del questionario
   * @param userId identificativo dell'utente richiedente
   * @return lista delle risposte del questionario
   */
  public List<SurveyResponseDto> getResponsesBySurveyId(String surveyId, String userId) {
    TeacherSurvey survey = surveyRepository.findById(surveyId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Questionario non trovato con id: " + surveyId));

    return responseRepository.findBySurveyId(surveyId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Restituisce i commenti testuali associati a un questionario.
   *
   * @param surveyId identificativo del questionario
   * @param userId identificativo dell'utente richiedente
   * @return lista delle risposte contenenti commenti
   */
  public List<SurveyResponseDto> getSurveyComments(String surveyId, String userId) {
    TeacherSurvey survey = surveyRepository.findById(surveyId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Questionario non trovato con id: " + surveyId));

    teacherSurveyMessageService.publishSurveyCommentsRequested(surveyId, userId);

    return responseRepository.findBySurveyId(surveyId).stream()
        .filter(response -> response.getTextComment() != null && !response.getTextComment().trim()
            .isEmpty())
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Calcola i risultati medi delle valutazioni numeriche di un questionario.
   *
   * @param surveyId identificativo del questionario
   * @param userId identificativo dell'utente richiedente
   * @return mappa questionId → media delle valutazioni
   */
  public Map<String, Double> getSurveyResults(String surveyId, String userId) {
    TeacherSurvey survey = surveyRepository.findById(surveyId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Questionario non trovato con id: " + surveyId));

    teacherSurveyMessageService.publishSurveyResultsRequested(surveyId, userId);

    List<SurveyResponse> responses = responseRepository.findBySurveyId(surveyId);

    return responses.stream()
        .filter(response -> response.getNumericRating() != null)
        .collect(Collectors.groupingBy(
            SurveyResponse::getQuestionId,
            Collectors.averagingDouble(SurveyResponse::getNumericRating)
        ));
  }

  /**
   * Restituisce tutte le risposte fornite da uno studente.
   *
   * @param studentId identificativo dello studente
   * @return lista delle risposte dello studente
   */
  public List<SurveyResponseDto> getResponsesByStudentId(String studentId) {
    return responseRepository.findByStudentId(studentId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Restituisce i questionari attualmente attivi per lo studente.
   *
   * @param studentId identificativo dello studente
   * @return lista dei questionari attivi
   */
  public List<TeacherSurvey> getAvailableSurveysForStudent(String studentId) {
    return surveyRepository.findByStatus(SurveyStatus.ACTIVE);
  }

  // ============ Metodi di Classe ============
  /**
   * Invia un insieme di risposte per un questionario.
   *
   * @param surveyId identificativo del questionario
   * @param responseDtos lista delle risposte
   * @param authenticatedUserId identificativo dello studente autenticato
   * @return lista delle risposte salvate
   */
  @Transactional
  public List<SurveyResponseDto> submitSurveyResponses(String surveyId,
      List<SurveyResponseDto> responseDtos, String authenticatedUserId) {

    TeacherSurvey survey = surveyRepository.findById(surveyId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Questionario non trovato con id: " + surveyId));

    if (survey.getStatus() != SurveyStatus.ACTIVE) {
      throw new SurveyClosedException("Non è possibile inviare risposte ad un Questionario chiuso");
    }

    boolean hasAlreadyResponded = responseRepository.existsBySurveyIdAndStudentId(surveyId,
        authenticatedUserId);
    if (hasAlreadyResponded) {
      throw new DuplicateResponseException("Hai già compilato questo questionario");
    }

    validateSurveyResponses(responseDtos, survey);

    Set<String> questionIds = responseDtos.stream()
        .map(SurveyResponseDto::getQuestionId)
        .collect(Collectors.toSet());

    if (questionIds.size() != responseDtos.size()) {
      throw new IllegalArgumentException(
          "Non è possibile inviare più risposte per la stessa domanda");
    }

    LocalDateTime submissionTime = LocalDateTime.now();
    List<SurveyResponse> responses = responseDtos.stream()
        .map(dto -> {
          SurveyResponse response = convertToEntity(dto);
          response.setSurvey(survey);
          response.setStudentId(authenticatedUserId);
          response.setSubmissionDate(submissionTime);
          return response;
        })
        .collect(Collectors.toList());

    List<SurveyResponse> savedResponses = responseRepository.saveAll(responses);
    List<SurveyResponseDto> result = savedResponses.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());

    try {
      surveyResponseMessageService.publishSurveyResponsesSubmitted(result, surveyId);
    } catch (Exception e) {
      logger.warn("Errore durante invio notifica per risposte questionario {}: {}", surveyId,
          e.getMessage());
    }

    return result;
  }

  /**
   * Valida l'insieme di risposte fornite per un questionario.
   *
   * <p>Verifica la presenza dell'ID domanda, la validità delle valutazioni
   * numeriche e la lunghezza dei commenti.</p>
   *
   * @param responseDtos lista delle risposte da validare
   * @param survey questionario associato alle risposte
   * @throws IllegalArgumentException se una risposta non rispetta i vincoli
   */
  private void validateSurveyResponses(List<SurveyResponseDto> responseDtos, TeacherSurvey survey) {
    for (SurveyResponseDto dto : responseDtos) {

      if (dto.getQuestionId() == null || dto.getQuestionId().trim().isEmpty()) {
        throw new IllegalArgumentException("ID domanda non può essere vuoto");
      }

      if (dto.getNumericRating() == null
          && (dto.getTextComment() == null
          || dto.getTextComment().trim().isEmpty())) {
        throw new IllegalArgumentException(
            "Ogni risposta deve contenere almeno una valutazione numerica o un commento");
      }

      if (dto.getNumericRating() != null) {
        if (dto.getNumericRating() < 1 || dto.getNumericRating() > 5) {
          throw new IllegalArgumentException("La valutazione numerica deve essere tra 1 e 5");
        }
      }

      if (dto.getTextComment() != null && dto.getTextComment().length() > 1000) {
        throw new IllegalArgumentException("Il commento non può superare i 1000 caratteri");
      }

    }
  }

  /**
   * Crea e salva una singola risposta al questionario.
   *
   * @param responseDto dati della risposta
   * @return risposta salvata
   */
  @Transactional
  public SurveyResponseDto createResponse(SurveyResponseDto responseDto) {
    TeacherSurvey survey = surveyRepository.findById(responseDto.getSurveyId())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Questionario non trovato con id: " + responseDto.getSurveyId()));

    if (survey.getStatus() != SurveyStatus.ACTIVE) {
      throw new SurveyClosedException("Non è possibile inviare risposte ad un Questionario chiuso");
    }

    SurveyResponse response = convertToEntity(responseDto);
    response.setSurvey(survey);
    response.setSubmissionDate(LocalDateTime.now());

    SurveyResponse savedResponse = responseRepository.save(response);
    SurveyResponseDto result = convertToDto(savedResponse);

    surveyResponseMessageService.publishSurveyResponseSubmitted(result);

    return result;
  }

  private SurveyResponseDto convertToDto(SurveyResponse response) {
    SurveyResponseDto dto = new SurveyResponseDto();
    dto.setId(response.getId());
    dto.setSurveyId(response.getSurvey().getId());
    dto.setStudentId(response.getStudentId());
    dto.setQuestionId(response.getQuestionId());
    dto.setNumericRating(response.getNumericRating());
    dto.setTextComment(response.getTextComment());
    dto.setSubmissionDate(response.getSubmissionDate());
    return dto;
  }

  private SurveyResponse convertToEntity(SurveyResponseDto dto) {
    SurveyResponse response = new SurveyResponse();
    response.setId(dto.getId());
    response.setStudentId(dto.getStudentId());
    response.setQuestionId(dto.getQuestionId());
    response.setNumericRating(dto.getNumericRating());
    response.setTextComment(dto.getTextComment());
    response.setSubmissionDate(
        dto.getSubmissionDate() != null ? dto.getSubmissionDate() : LocalDateTime.now());
    return response;
  }
}