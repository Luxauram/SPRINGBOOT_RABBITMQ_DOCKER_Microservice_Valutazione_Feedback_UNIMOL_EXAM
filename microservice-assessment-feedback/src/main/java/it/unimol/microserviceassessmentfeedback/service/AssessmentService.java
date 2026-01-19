package it.unimol.microserviceassessmentfeedback.service;

import it.unimol.microserviceassessmentfeedback.common.exception.ResourceNotFoundException;
import it.unimol.microserviceassessmentfeedback.dto.AssessmentDto;
import it.unimol.microserviceassessmentfeedback.enums.ReferenceType;
import it.unimol.microserviceassessmentfeedback.enums.RoleType;
import it.unimol.microserviceassessmentfeedback.messaging.publishers.AssessmentMessageService;
import it.unimol.microserviceassessmentfeedback.model.Assessment;
import it.unimol.microserviceassessmentfeedback.repository.AssessmentRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servizio per la gestione delle valutazioni (Assessment).
 * Fornisce operazioni CRUD e logica di business per le valutazioni di studenti,
 * inclusi controlli di autorizzazione e pubblicazione di eventi.
 */
@Service
public class AssessmentService {

  private static final Logger logger = LoggerFactory.getLogger(AssessmentService.class);

  private final AssessmentRepository assessmentRepository;
  private final AssessmentMessageService assessmentMessageService;

  // ============ Costruttore ============
  /**
   * Costruttore con iniezione delle dipendenze.
   *
   * @param assessmentRepository il repository per le valutazioni
   * @param assessmentMessageService il servizio per la pubblicazione di eventi
   */
  public AssessmentService(AssessmentRepository assessmentRepository,
      AssessmentMessageService assessmentMessageService) {
    this.assessmentRepository = assessmentRepository;
    this.assessmentMessageService = assessmentMessageService;
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============
  /**
   * Recupera tutte le valutazioni presenti nel sistema.
   *
   * @return la lista di tutte le valutazioni
   */
  public List<AssessmentDto> getAllAssessments() {
    logger.debug("Recupero di tutte le valutazioni");
    List<Assessment> assessments = assessmentRepository.findAll();
    logger.debug("Trovate {} valutazioni", assessments.size());

    return assessments.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Recupera una valutazione specifica per ID con controlli di autorizzazione.
   * Gli studenti possono accedere solo alle proprie valutazioni.
   *
   * @param id l'ID della valutazione
   * @return la valutazione richiesta
   * @throws ResourceNotFoundException se la valutazione non esiste
   * @throws AccessDeniedException se l'utente non è autorizzato
   */
  public AssessmentDto getAssessmentById(String id) {
    logger.debug("Recupero valutazione con ID: {}", id);

    Assessment assessment = assessmentRepository.findById(id)
        .orElseThrow(() -> {
          logger.warn("Valutazione non trovata con ID: {}", id);
          return new ResourceNotFoundException("Valutazione non trovata con id: " + id);
        });

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AccessDeniedException("Utente non autenticato");
    }

    String currentUsername = authentication.getName();

    String userRole = null;
    for (GrantedAuthority authority : authentication.getAuthorities()) {
      if (authority.getAuthority().startsWith("ROLE_")) {
        userRole = authority.getAuthority();
        break;
      }
    }

    if (RoleType.ROLE_STUDENT.equals(userRole)) {
      if (!assessment.getStudentId().equals(currentUsername)) {
        logger.warn("Studente {} ha tentato di accedere alla valutazione {} non sua",
            currentUsername, id);
        throw new AccessDeniedException("Non autorizzato ad accedere a questa valutazione");
      }
      logger.debug("Accesso autorizzato per studente {} alla valutazione {}",
          currentUsername, id);
    }

    logger.debug("Valutazione trovata con ID: {}", id);
    return convertToDto(assessment);
  }

  /**
   * Recupera tutte le valutazioni di uno studente specifico.
   * Gli studenti possono accedere solo alle proprie valutazioni.
   *
   * @param studentId l'ID dello studente
   * @return la lista delle valutazioni dello studente
   * @throws AccessDeniedException se l'utente non è autorizzato
   */
  public List<AssessmentDto> getAssessmentsByStudentId(String studentId) {
    logger.debug("Recupero valutazioni per studente con ID: {}", studentId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AccessDeniedException("Utente non autenticato");
    }

    String currentUsername = authentication.getName();

    String userRole = null;
    for (GrantedAuthority authority : authentication.getAuthorities()) {
      if (authority.getAuthority().startsWith("ROLE_")) {
        userRole = authority.getAuthority();
        break;
      }
    }

    if (RoleType.ROLE_STUDENT.equals(userRole)) {
      if (!studentId.equals(currentUsername)) {
        logger.warn(
            "Studente {} ha tentato di accedere alle valutazioni dello studente {} non autorizzate",
            currentUsername, studentId);
        throw new AccessDeniedException(
            "Non autorizzato ad accedere alle valutazioni di questo studente");
      }
      logger.debug("Accesso autorizzato per studente {} alle proprie valutazioni", currentUsername);
    }

    List<Assessment> assessments = assessmentRepository.findByStudentId(studentId);
    logger.debug("Trovate {} valutazioni per studente {}", assessments.size(), studentId);

    return assessments.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Recupera tutte le valutazioni associate a un assignment.
   *
   * @param assignmentId l'ID dell'assignment
   * @return la lista delle valutazioni per l'assignment
   */
  public List<AssessmentDto> getAssessmentsByAssignment(String assignmentId) {
    logger.debug("Recupero valutazioni per assignment con ID: {}", assignmentId);
    List<Assessment> assessments = assessmentRepository.findByReferenceIdAndReferenceType(
        assignmentId, ReferenceType.ASSIGNMENT);
    logger.debug("Trovate {} valutazioni per assignment {}", assessments.size(), assignmentId);

    return assessments.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Recupera tutte le valutazioni associate a un esame.
   *
   * @param examId l'ID dell'esame
   * @return la lista delle valutazioni per l'esame
   */
  public List<AssessmentDto> getAssessmentsByExam(String examId) {
    logger.debug("Recupero valutazioni per exam con ID: {}", examId);
    List<Assessment> assessments = assessmentRepository.findByReferenceIdAndReferenceType(examId,
        ReferenceType.EXAM);
    logger.debug("Trovate {} valutazioni per exam {}", assessments.size(), examId);

    return assessments.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  /**
   * Recupera tutte le valutazioni associate a un corso.
   *
   * @param courseId l'ID del corso
   * @return la lista delle valutazioni per il corso
   */
  public List<AssessmentDto> getAssessmentsByCourse(String courseId) {
    logger.debug("Recupero valutazioni per corso con ID: {}", courseId);
    List<Assessment> assessments = assessmentRepository.findByCourseId(courseId);
    logger.debug("Trovate {} valutazioni per corso {}", assessments.size(), courseId);

    return assessments.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  // ============ Metodi di Classe ============
  /**
   * Crea una nuova valutazione.
   * Valida i dati e pubblica un evento di creazione.
   *
   * @param assessmentDto i dati della valutazione da creare
   * @return la valutazione creata
   * @throws IllegalArgumentException se i dati non sono validi
   */
  @Transactional
  public AssessmentDto createAssessment(AssessmentDto assessmentDto) {
    logger.info("Creazione nuova valutazione per studente: {} e corso: {}",
        assessmentDto.getStudentId(), assessmentDto.getCourseId());

    validateAssessmentData(assessmentDto);

    Assessment assessment = convertToEntity(assessmentDto);
    assessment.setAssessmentDate(LocalDateTime.now(ZoneId.systemDefault()));

    Assessment savedAssessment = assessmentRepository.save(assessment);
    logger.info("Valutazione creata con successo con ID: {}", savedAssessment.getId());

    AssessmentDto result = convertToDto(savedAssessment);
    try {
      assessmentMessageService.publishAssessmentCreated(result);
      logger.debug("Evento di creazione valutazione pubblicato per ID: {}", result.getId());
    } catch (Exception e) {
      logger.warn("Errore nella pubblicazione dell'evento di creazione valutazione: {}",
          e.getMessage());
    }

    return result;
  }

  /**
   * Aggiorna una valutazione esistente.
   * Pubblica un evento di aggiornamento.
   *
   * @param id l'ID della valutazione da aggiornare
   * @param assessmentDto i nuovi dati della valutazione
   * @return la valutazione aggiornata
   * @throws ResourceNotFoundException se la valutazione non esiste
   */
  @Transactional
  public AssessmentDto updateAssessment(String id, AssessmentDto assessmentDto) {
    logger.info("Aggiornamento valutazione con ID: {}", id);

    Assessment existingAssessment = assessmentRepository.findById(id)
        .orElseThrow(() -> {
          logger.warn("Tentativo di aggiornamento di valutazione inesistente con ID: {}", id);
          return new ResourceNotFoundException("Valutazione non trovata con id: " + id);
        });

    if (assessmentDto.getScore() != null) {
      existingAssessment.setScore(assessmentDto.getScore());
    }
    if (assessmentDto.getNotes() != null) {
      existingAssessment.setNotes(assessmentDto.getNotes());
    }

    Assessment updatedAssessment = assessmentRepository.save(existingAssessment);
    logger.info("Valutazione aggiornata con successo con ID: {}", id);

    AssessmentDto result = convertToDto(updatedAssessment);
    try {
      assessmentMessageService.publishAssessmentUpdated(result);
      logger.debug("Evento di aggiornamento valutazione pubblicato per ID: {}", result.getId());
    } catch (Exception e) {
      logger.warn("Errore nella pubblicazione dell'evento di aggiornamento valutazione: {}",
          e.getMessage());
    }

    return result;
  }

  /**
   * Elimina una valutazione.
   * Pubblica un evento di eliminazione.
   *
   * @param id l'ID della valutazione da eliminare
   * @throws ResourceNotFoundException se la valutazione non esiste
   */
  @Transactional
  public void deleteAssessment(String id) {
    logger.info("Eliminazione valutazione con ID: {}", id);

    if (!assessmentRepository.existsById(id)) {
      logger.warn("Tentativo di eliminazione di valutazione inesistente con ID: {}", id);
      throw new ResourceNotFoundException("Valutazione non trovata con id: " + id);
    }

    assessmentRepository.deleteById(id);
    logger.info("Valutazione eliminata con successo con ID: {}", id);

    try {
      assessmentMessageService.publishAssessmentDeleted(id);
      logger.debug("Evento di eliminazione valutazione pubblicato per ID: {}", id);
    } catch (Exception e) {
      logger.warn("Errore nella pubblicazione dell'evento di eliminazione valutazione: {}",
          e.getMessage());
    }
  }

  /**
   * Valida i dati essenziali dell'assessment prima della creazione.
   *
   * @param assessmentDto i dati da validare
   * @throws IllegalArgumentException se i dati non sono validi
   */
  private void validateAssessmentData(AssessmentDto assessmentDto) {
    if (assessmentDto.getStudentId() == null) {
      throw new IllegalArgumentException("StudentId è obbligatorio per creare una valutazione");
    }
    if (assessmentDto.getTeacherId() == null) {
      throw new IllegalArgumentException("TeacherId è obbligatorio per creare una valutazione");
    }
    if (assessmentDto.getCourseId() == null) {
      throw new IllegalArgumentException("CourseId è obbligatorio per creare una valutazione");
    }
    if (assessmentDto.getReferenceId() == null) {
      throw new IllegalArgumentException("ReferenceId è obbligatorio per creare una valutazione");
    }
    if (assessmentDto.getReferenceType() == null) {
      throw new IllegalArgumentException("ReferenceType è obbligatorio per creare una valutazione");
    }
    if (assessmentDto.getScore() == null) {
      throw new IllegalArgumentException("Score è obbligatorio per creare una valutazione");
    }
  }


  private AssessmentDto convertToDto(Assessment assessment) {
    AssessmentDto dto = new AssessmentDto();
    dto.setId(assessment.getId());
    dto.setReferenceId(assessment.getReferenceId());
    dto.setReferenceType(assessment.getReferenceType());
    dto.setStudentId(assessment.getStudentId());
    dto.setTeacherId(assessment.getTeacherId());
    dto.setScore(assessment.getScore());
    dto.setAssessmentDate(assessment.getAssessmentDate());
    dto.setNotes(assessment.getNotes());
    dto.setCourseId(assessment.getCourseId());
    return dto;
  }

  private Assessment convertToEntity(AssessmentDto dto) {
    Assessment assessment = new Assessment();
    assessment.setId(dto.getId());
    assessment.setReferenceId(dto.getReferenceId());
    assessment.setReferenceType(dto.getReferenceType());
    assessment.setStudentId(dto.getStudentId());
    assessment.setTeacherId(dto.getTeacherId());
    assessment.setScore(dto.getScore());
    assessment.setAssessmentDate(
        dto.getAssessmentDate() != null
            ? dto.getAssessmentDate()
            : LocalDateTime.now(ZoneId.systemDefault()));
    assessment.setNotes(dto.getNotes());
    assessment.setCourseId(dto.getCourseId());
    return assessment;
  }
}