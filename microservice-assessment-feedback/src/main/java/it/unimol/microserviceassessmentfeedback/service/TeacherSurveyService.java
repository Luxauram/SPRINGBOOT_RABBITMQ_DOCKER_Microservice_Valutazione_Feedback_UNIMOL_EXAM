package it.unimol.microserviceassessmentfeedback.service;

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
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service per la gestione dei questionari dei docenti.
 *
 * <p>Fornisce operazioni di creazione, modifica, eliminazione,
 * cambio di stato e consultazione dei questionari, oltre alla
 * pubblicazione degli eventi associati.</p>
 */
@Service
public class TeacherSurveyService {

  private static final Logger logger = LoggerFactory.getLogger(TeacherSurveyService.class);

  private final TeacherSurveyRepository surveyRepository;
  private final TeacherSurveyMessageService teacherSurveyMessageService;

  // ============ Costruttore ============
  /**
   * Costruttore del servizio TeacherSurveyService.
   *
   * @param surveyRepository repository dei questionari
   * @param teacherSurveyMessageService servizio di pubblicazione eventi questionari
   */
  @Autowired
  public TeacherSurveyService(TeacherSurveyRepository surveyRepository,
      TeacherSurveyMessageService teacherSurveyMessageService) {
    this.surveyRepository = surveyRepository;
    this.teacherSurveyMessageService = teacherSurveyMessageService;
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============
  /**
   * Restituisce tutti i questionari presenti nel sistema.
   *
   * @return lista di tutti i questionari
   */
  public List<TeacherSurveyDto> getAllSurveys() {
    logger.info("Recupero di tutti i questionari");
    return surveyRepository.findAll().stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Restituisce un questionario dato il suo identificativo.
   *
   * @param id identificativo del questionario
   * @return questionario trovato
   */
  public TeacherSurveyDto getSurveyById(String id) {
    logger.info("Recupero questionario con id: {}", id);
    TeacherSurvey survey = surveyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Questionario non trovato con id: " + id));
    return convertToDto(survey);
  }

  /**
   * Restituisce i questionari associati a un corso.
   *
   * @param courseId identificativo del corso
   * @return lista dei questionari del corso
   */
  public List<TeacherSurveyDto> getSurveysByCourse(String courseId) {
    logger.info("Recupero questionari per corso: {}", courseId);
    return surveyRepository.findByCourseId(courseId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Restituisce i questionari creati da un docente.
   *
   * @param teacherId identificativo del docente
   * @return lista dei questionari del docente
   */
  public List<TeacherSurveyDto> getSurveysByTeacher(String teacherId) {
    logger.info("Recupero questionari per docente: {}", teacherId);
    return surveyRepository.findByTeacherId(teacherId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Restituisce tutti i questionari attualmente attivi.
   *
   * @return lista dei questionari attivi
   */
  public List<TeacherSurveyDto> getActiveSurveys() {
    logger.info("Recupero questionari attivi");
    return surveyRepository.findByStatus(SurveyStatus.ACTIVE).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Restituisce statistiche relative a un questionario.
   *
   * @param surveyId identificativo del questionario
   * @return oggetto contenente statistiche del questionario
   */
  @SuppressWarnings("unused")
  public Object getSurveyStatistics(String surveyId) {
    logger.info("Richiesta statistiche per questionario: {}", surveyId);
    String requestedBy = getCurrentUser();
    teacherSurveyMessageService.publishSurveyResultsRequested(surveyId, requestedBy);

    TeacherSurvey survey = surveyRepository.findById(surveyId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Questionario non trovato con id: " + surveyId));

    return new Object() {
      public final String surveyId = survey.getId();
      public final String title = survey.getTitle();
      public final String description = survey.getDescription();
      public final List<SurveyQuestionDto> questions = survey.getQuestions();
      public final String status = survey.getStatus().toString();
      public final String message = "Statistiche placeholder per questionario " + surveyId;
    };
  }

  /**
   * Restituisce statistiche generali sui questionari.
   *
   * @return oggetto contenente statistiche aggregate
   */
  @SuppressWarnings("unused")
  public Object getGeneralStatistics() {
    logger.info("Richiesta statistiche generali questionari");

    return new Object() {
      public final long totalSurveys = surveyRepository.count();
      public final long draftSurveys = surveyRepository.countByStatus(SurveyStatus.DRAFT);
      public final long activeSurveys = surveyRepository.countByStatus(SurveyStatus.ACTIVE);
      public final long closedSurveys = surveyRepository.countByStatus(SurveyStatus.CLOSED);
      public final String generatedAt = LocalDateTime.now(ZoneId.systemDefault()).toString();
    };
  }

  private String getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null ? authentication.getName() : "anonymous";
  }

  // ============ Metodi di Classe ============
  /**
   * Crea un nuovo questionario in stato DRAFT.
   *
   * @param surveyDto dati del questionario
   * @return questionario creato
   */
  @Transactional
  public TeacherSurveyDto createSurvey(TeacherSurveyDto surveyDto) {
    logger.info("Creazione nuovo questionario per docente: {} e corso: {}",
        surveyDto.getTeacherId(), surveyDto.getCourseId());

    if (surveyRepository.existsByTeacherIdAndCourseIdAndAcademicYearAndSemester(
        surveyDto.getTeacherId(), surveyDto.getCourseId(),
        surveyDto.getAcademicYear(), surveyDto.getSemester())) {
      throw new IllegalArgumentException(
          "Esiste già un questionario per questo docente, corso e periodo");
    }

    if (surveyDto.getTitle() == null || surveyDto.getTitle().isBlank()) {
      throw new IllegalArgumentException("Il titolo del questionario è obbligatorio.");
    }
    if (surveyDto.getQuestions() == null || surveyDto.getQuestions().isEmpty()) {
      throw new IllegalArgumentException("Il questionario deve contenere almeno una domanda.");
    }

    for (SurveyQuestionDto question : surveyDto.getQuestions()) {
      if (question.getQuestionText() == null || question.getQuestionText().isBlank()) {
        throw new IllegalArgumentException("Il testo di una domanda non può essere vuoto.");
      }
      if (question.getQuestionType() == null) {
        throw new IllegalArgumentException("Il tipo di domanda è obbligatorio.");
      }
      if (question.getQuestionType() == QuestionType.RATING) {
        if (question.getMinRating() == null || question.getMaxRating() == null) {
          throw new IllegalArgumentException(
              "Per le domande RATING, minRating e maxRating sono obbligatori.");
        }
        if (question.getMinRating() < 1 || question.getMaxRating() > 5
            || question.getMinRating() > question.getMaxRating()) {
          throw new IllegalArgumentException("Il range di rating non è valido (es. 1-5).");
        }
      } else if (question.getQuestionType() == QuestionType.TEXT) {
        if (question.getMaxLengthText() == null || question.getMaxLengthText() <= 0) {
          throw new IllegalArgumentException(
              "Per le domande TEXT, maxLengthText è obbligatorio e deve essere positivo.");
        }
      }
    }

    TeacherSurvey survey = convertToEntity(surveyDto);
    survey.setStatus(SurveyStatus.DRAFT);
    survey.setCreationDate(LocalDateTime.now(ZoneId.systemDefault()));

    TeacherSurvey savedSurvey = surveyRepository.save(survey);
    TeacherSurveyDto result = convertToDto(savedSurvey);

    try {
      teacherSurveyMessageService.publishSurveyCompleted(result);
      logger.info("Evento di questionario creato pubblicato per id: {}", result.getId());
    } catch (Exception e) {
      logger.error("Errore nella pubblicazione dell'evento per questionario id: {}", result.getId(),
          e);
    }

    return result;
  }

  /**
   * Aggiorna un questionario esistente in stato DRAFT.
   *
   * @param id identificativo del questionario
   * @param surveyDto nuovi dati del questionario
   * @return questionario aggiornato
   */
  @Transactional
  public TeacherSurveyDto updateSurvey(String id, TeacherSurveyDto surveyDto) {
    logger.info("Aggiornamento questionario con id: {}", id);

    TeacherSurvey existingSurvey = surveyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Questionario non trovato con id: " + id));

    if (existingSurvey.getStatus() != SurveyStatus.DRAFT) {
      throw new IllegalStateException(
          "Impossibile modificare un questionario che non sia in stato DRAFT. Utilizzare "
              + "l'endpoint di cambio stato se necessario.");
    }

    if (surveyDto.getTitle() != null) {
      existingSurvey.setTitle(surveyDto.getTitle());
    }
    if (surveyDto.getDescription() != null) {
      existingSurvey.setDescription(surveyDto.getDescription());
    }
    if (surveyDto.getQuestions() != null) {
      existingSurvey.setQuestions(surveyDto.getQuestions());
    }
    if (surveyDto.getAcademicYear() != null) {
      existingSurvey.setAcademicYear(surveyDto.getAcademicYear());
    }
    if (surveyDto.getSemester() != null) {
      existingSurvey.setSemester(surveyDto.getSemester());
    }

    TeacherSurvey updatedSurvey = surveyRepository.save(existingSurvey);
    TeacherSurveyDto result = convertToDto(updatedSurvey);

    return result;
  }

  /**
   * Cambia lo stato di un questionario.
   *
   * @param id identificativo del questionario
   * @param newStatus nuovo stato da impostare
   * @return questionario aggiornato
   */
  @Transactional
  public TeacherSurveyDto changeSurveyStatus(String id, SurveyStatus newStatus) {
    logger.info("Cambio stato questionario id: {} a: {}", id, newStatus);

    TeacherSurvey survey = surveyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Questionario non trovato con id: " + id));

    validateStatusTransition(survey.getStatus(), newStatus);

    survey.setStatus(newStatus);

    if (newStatus == SurveyStatus.CLOSED) {
      survey.setClosingDate(LocalDateTime.now(ZoneId.systemDefault()));
    } else if (newStatus == SurveyStatus.ACTIVE && survey.getClosingDate() != null) {
      survey.setClosingDate(null);
    }

    TeacherSurvey updatedSurvey = surveyRepository.save(survey);
    TeacherSurveyDto result = convertToDto(updatedSurvey);

    if (newStatus == SurveyStatus.CLOSED) {
      try {
        teacherSurveyMessageService.publishSurveyCompleted(result);
        logger.info("Evento di questionario completato pubblicato per id: {}", result.getId());
      } catch (Exception e) {
        logger.error("Errore nella pubblicazione dell'evento per questionario id: {}",
            result.getId(), e);
      }
    }

    return result;
  }

  /**
   * Elimina un questionario dal sistema.
   *
   * @param id identificativo del questionario
   */
  @Transactional
  @SuppressWarnings("unused")
  public void deleteSurvey(String id) {
    logger.info("Eliminazione questionario con id: {}", id);

    TeacherSurvey survey = surveyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Questionario non trovato con id: " + id));

    surveyRepository.deleteById(id);
    logger.info("Questionario eliminato con successo: {}", id);
  }

  /**
   * Valida la transizione tra stati di un questionario.
   *
   * @param currentStatus stato attuale
   * @param newStatus nuovo stato richiesto
   */
  private void validateStatusTransition(SurveyStatus currentStatus, SurveyStatus newStatus) {
    switch (currentStatus) {
      case DRAFT:
        if (newStatus != SurveyStatus.ACTIVE && newStatus != SurveyStatus.CLOSED) {
          throw new IllegalArgumentException("Da DRAFT si può passare solo a ACTIVE o CLOSED");
        }
        break;
      case ACTIVE:
        if (newStatus != SurveyStatus.CLOSED) {
          throw new IllegalArgumentException("Da ACTIVE si può passare solo a CLOSED");
        }
        break;
      case CLOSED:
        if (newStatus != SurveyStatus.ACTIVE && newStatus != SurveyStatus.DRAFT) {
          throw new IllegalArgumentException("Da CLOSED si può passare solo ad ACTIVE o DRAFT");
        }
        break;
      default:
        throw new IllegalArgumentException(
            "Transizione di stato non valida da " + currentStatus + " a " + newStatus);
    }
  }

  private TeacherSurveyDto convertToDto(TeacherSurvey survey) {
    return TeacherSurveyDto.builder()
        .id(survey.getId())
        .courseId(survey.getCourseId())
        .teacherId(survey.getTeacherId())
        .academicYear(survey.getAcademicYear())
        .semester(survey.getSemester())
        .status(survey.getStatus())
        .creationDate(survey.getCreationDate())
        .closingDate(survey.getClosingDate())
        .title(survey.getTitle())
        .description(survey.getDescription())
        .questions(survey.getQuestions())
        .build();
  }

  private TeacherSurvey convertToEntity(TeacherSurveyDto dto) {
    TeacherSurvey survey = new TeacherSurvey();
    if (dto.getId() != null) {
      survey.setId(dto.getId());
    }
    survey.setCourseId(dto.getCourseId());
    survey.setTeacherId(dto.getTeacherId());
    survey.setAcademicYear(dto.getAcademicYear());
    survey.setSemester(dto.getSemester());
    survey.setStatus(dto.getStatus() != null ? dto.getStatus() : SurveyStatus.DRAFT);
    survey.setCreationDate(
        dto.getCreationDate() != null
            ? dto.getCreationDate()
            : LocalDateTime.now(ZoneId.systemDefault()));
    survey.setClosingDate(dto.getClosingDate());
    survey.setTitle(dto.getTitle());
    survey.setDescription(dto.getDescription());
    survey.setQuestions(dto.getQuestions());
    return survey;
  }
}