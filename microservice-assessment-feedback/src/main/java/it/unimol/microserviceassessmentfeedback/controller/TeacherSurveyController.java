package it.unimol.microserviceassessmentfeedback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unimol.microserviceassessmentfeedback.common.exception.ErrorResponse;
import it.unimol.microserviceassessmentfeedback.common.util.JwtRequestHelper;
import it.unimol.microserviceassessmentfeedback.dto.TeacherSurveyDto;
import it.unimol.microserviceassessmentfeedback.enums.RoleType;
import it.unimol.microserviceassessmentfeedback.enums.SurveyStatus;
import it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per la gestione dei questionari di valutazione docenti.
 * Fornisce endpoint per operazioni CRUD e gestione dello stato dei questionari.
 */
@RestController
@RequestMapping("/api/v1/teacher-surveys")
@Tag(name = "Teacher Survey Management", description = "API per la gestione dei questionari di "
    + "valutazione docenti")
@SecurityRequirement(name = "bearerAuth")
public class TeacherSurveyController {

  private static final Logger logger = LoggerFactory.getLogger(TeacherSurveyController.class);
  private final TeacherSurveyService surveyService;
  @Autowired
  private JwtRequestHelper jwtRequestHelper;

  // ============ Costruttore ============

  @Autowired
  public TeacherSurveyController(TeacherSurveyService surveyService) {
    this.surveyService = surveyService;
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  /**
   * Recupera tutti i questionari presenti nel sistema.
   *
   * @return Una lista di oggetti {@link TeacherSurveyDto} che rappresentano tutti i questionari
   *     presenti.
   * @apiNote GET - getAllSurveys - ADMIN/SUPER_ADMIN TRACCIA: [NON SPECIFICATO/RICHIESTO NELLA
   *     TRACCIA] NOTA: Solo amministratori per supervisione completa del sistema di valutazione
   *     docenti
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService#getAllSurveys()
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping
  @PreAuthorize("hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni tutti i questionari",
      description = "Recupera tutti i questionari di valutazione docenti (solo amministratori)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Questionari recuperati con successo",
          content = @Content(schema = @Schema(implementation = TeacherSurveyDto.class))),
      @ApiResponse(responseCode = "401", description = "Accesso non autorizzato - Token JWT "
          + "richiesto"),
      @ApiResponse(responseCode = "403", description = "Accesso vietato - ruolo ADMIN o "
          + "SUPER_ADMIN richiesto"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<TeacherSurveyDto>> getAllSurveys() {
    logger.info("Richiesta per ottenere tutti i questionari docenti");
    List<TeacherSurveyDto> surveys = surveyService.getAllSurveys();
    logger.info("Recuperati {} questionari", surveys.size());
    return ResponseEntity.ok(surveys);
  }

  /**
   * Recupera un questionario specifico tramite ID.
   *
   * @param id L'ID univoco del questionario da recuperare.
   * @param request La richiesta HTTP per estrarre informazioni dell'utente autenticato.
   * @return Un oggetto {@link TeacherSurveyDto} che rappresenta il questionario richiesto.
   * @apiNote GET - getSurveyById - TEACHER/ADMIN/SUPER_ADMIN (TEACHER solo per i propri) TRACCIA:
   *     Gestione questionari da parte docenti e amministratori NOTA: TEACHER per visualizzare i
   *     propri questionari, ADMIN/SUPER_ADMIN per supervisione
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService
   *     #getSurveyById(String)
   * @see JwtRequestHelper#getUserRoleFromRequest(HttpServletRequest)
   * @see JwtRequestHelper#extractTeacherIdFromRequest(HttpServletRequest)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni questionario per ID",
      description = "Recupera un questionario specifico tramite ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Questionario trovato con successo",
          content = @Content(schema = @Schema(implementation = TeacherSurveyDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - autorizzazione insufficiente"),
      @ApiResponse(responseCode = "404", description = "Questionario non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<TeacherSurveyDto> getSurveyById(
      @Parameter(description = "ID Questionario", required = true, example = "uuid-survey-123")
      @PathVariable String id,
      HttpServletRequest request) {

    logger.info("Richiesta per ottenere questionario con ID: {}", id);
    TeacherSurveyDto survey = surveyService.getSurveyById(id);

    String userRole = jwtRequestHelper.getUserRoleFromRequest(request);

    if (RoleType.ROLE_TEACHER.equals(userRole)) {
      String currentTeacherId = jwtRequestHelper.extractTeacherIdFromRequest(request);
      if (!survey.getTeacherId().equals(currentTeacherId)) {
        logger.warn(
            "Tentativo di accesso non autorizzato: docente {} ha tentato di accedere al "
                + "questionario {} del docente {}",
            currentTeacherId, id, survey.getTeacherId());
        throw new AccessDeniedException("Un docente può visualizzare solo i propri questionari");
      }
    }

    return ResponseEntity.ok(survey);
  }

  /**
   * Recupera tutti i questionari associati a un corso specifico.
   *
   * @param courseId L'ID univoco del corso di cui recuperare i questionari.
   * @return Una lista di oggetti {@link TeacherSurveyDto} che rappresentano i questionari per il
   *     corso specificato.
   * @apiNote GET - getSurveysByCourse - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: [NON
   *     SPECIFICATO/RICHIESTO NELLA TRACCIA] NOTA: TEACHER per i propri corsi, ADMIN/SUPER_ADMIN
   *     per supervisione amministrativa
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService
   *     #getSurveysByCourse(String)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping("/course/{courseId}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni questionari per corso",
      description = "Recupera tutti i questionari associati a un corso specifico")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Questionari per corso trovati con successo",
          content = @Content(schema = @Schema(implementation = TeacherSurveyDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - autorizzazione insufficiente"),
      @ApiResponse(responseCode = "404", description = "Corso non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<TeacherSurveyDto>> getSurveysByCourse(
      @Parameter(description = "ID Corso", required = true, example = "uuid-course-456")
      @PathVariable String courseId) {
    logger.info("Richiesta per ottenere questionari per corso con ID: {}", courseId);
    List<TeacherSurveyDto> surveys = surveyService.getSurveysByCourse(courseId);
    return ResponseEntity.ok(surveys);
  }

  /**
   * Recupera tutti i questionari associati a un docente specifico.
   *
   * @param teacherId L'ID univoco del docente di cui recuperare i questionari.
   * @param request La richiesta HTTP per estrarre informazioni dell'utente autenticato.
   * @return Una lista di oggetti {@link TeacherSurveyDto} che rappresentano i questionari per il
   *     docente specificato.
   * @apiNote GET - getSurveysByTeacher - TEACHER/ADMIN/SUPER_ADMIN (TEACHER solo per se stesso)
   *     TRACCIA: [NON SPECIFICATO/RICHIESTO NELLA TRACCIA] NOTA: TEACHER per visualizzare i propri
   *     questionari, ADMIN/SUPER_ADMIN per supervisione
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService
   *     #getSurveysByTeacher(String)
   * @see JwtRequestHelper#getUserRoleFromRequest(HttpServletRequest)
   * @see JwtRequestHelper#extractTeacherIdFromRequest(HttpServletRequest)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  public ResponseEntity<List<TeacherSurveyDto>> getSurveysByTeacher(
      @Parameter(description = "ID Docente", required = true, example = "uuid-teacher-789")
      @PathVariable String teacherId,
      HttpServletRequest request) {

    String userRole = jwtRequestHelper.getUserRoleFromRequest(request);

    if (RoleType.ROLE_TEACHER.equals(userRole)) {
      String currentTeacherId = jwtRequestHelper.extractTeacherIdFromRequest(request);
      if (!teacherId.equals(currentTeacherId)) {
        logger.warn(
            "Tentativo di accesso non autorizzato: docente {} ha tentato di accedere ai "
                + "questionari del docente {}",
            currentTeacherId, teacherId);
        throw new AccessDeniedException("Un docente può visualizzare solo i propri questionari");
      }
    }

    logger.info("Richiesta per ottenere questionari per docente con ID: {}", teacherId);
    List<TeacherSurveyDto> surveys = surveyService.getSurveysByTeacher(teacherId);
    return ResponseEntity.ok(surveys);
  }

  /**
   * Recupera tutti i questionari attivi disponibili per la compilazione.
   *
   * @return Una lista di oggetti {@link TeacherSurveyDto} che rappresentano i questionari attivi.
   * @apiNote GET - getActiveSurveys - STUDENT/ADMIN/SUPER_ADMIN TRACCIA: "Studenti - Compilazione
   *     del questionario di feedback sui docenti" NOTA: STUDENT per visualizzare questionari
   *     disponibili per compilazione, ADMIN/SUPER_ADMIN per supervisione
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService#getActiveSurveys()
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping("/active")
  @PreAuthorize("hasRole('" + RoleType.ROLE_STUDENT + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni questionari attivi",
      description = "Recupera tutti i questionari attivi disponibili per la compilazione")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Questionari attivi trovati con successo",
          content = @Content(schema = @Schema(implementation = TeacherSurveyDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - ruolo STUDENT, ADMIN o SUPER_ADMIN richiesto"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<TeacherSurveyDto>> getActiveSurveys() {
    logger.info("Richiesta questionari attivi per compilazione studenti");
    List<TeacherSurveyDto> activeSurveys = surveyService.getActiveSurveys();
    logger.info("Trovati {} questionari attivi", activeSurveys.size());
    return ResponseEntity.ok(activeSurveys);
  }

  /**
   * Recupera i risultati statistici di un questionario completato.
   *
   * @param id L'ID univoco del questionario di cui recuperare i risultati.
   * @param request La richiesta HTTP per estrarre informazioni dell'utente autenticato.
   * @return Un oggetto contenente le statistiche e i risultati del questionario completato.
   * @apiNote GET - getSurveyResults - TEACHER/ADMIN/SUPER_ADMIN (TEACHER solo per i propri)
   *     TRACCIA: [NON SPECIFICATO/RICHIESTO NELLA TRACCIA] NOTA: TEACHER per i risultati dei propri
   *     questionari, ADMIN/SUPER_ADMIN per supervisione completa
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService
   *     #getSurveyStatistics(String)
   * @see JwtRequestHelper#getUserRoleFromRequest(HttpServletRequest)
   * @see JwtRequestHelper#extractTeacherIdFromRequest(HttpServletRequest)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping("/{id}/results")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni risultati questionario",
      description = "Recupera i risultati statistici di un questionario completato")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Risultati recuperati con successo",
          content = @Content(schema = @Schema(implementation = Object.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - Solo docente proprietario o amministratori"),
      @ApiResponse(responseCode = "404", description = "Questionario non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Object> getSurveyResults(
      @Parameter(description = "ID Questionario", required = true, example = "uuid-survey-123")
      @PathVariable String id,
      HttpServletRequest request) {

    String userRole = jwtRequestHelper.getUserRoleFromRequest(request);

    if (RoleType.ROLE_TEACHER.equals(userRole)) {
      String currentTeacherId = jwtRequestHelper.extractTeacherIdFromRequest(request);
      TeacherSurveyDto survey = surveyService.getSurveyById(id);
      if (!survey.getTeacherId().equals(currentTeacherId)) {
        logger.warn(
            "Tentativo di accesso non autorizzato: docente {} ha tentato di accedere ai risultati"
                + " del questionario {} del docente {}",
            currentTeacherId, id, survey.getTeacherId());
        throw new AccessDeniedException(
            "Un docente può visualizzare solo i risultati dei propri questionari");
      }
    }

    logger.info("Richiesta per ottenere risultati per questionario con ID: {}", id);
    Object results = surveyService.getSurveyStatistics(id);
    return ResponseEntity.ok(results);
  }

  /**
   * Crea un nuovo questionario di valutazione per un docente.
   *
   * @param surveyDto Un oggetto {@link TeacherSurveyDto} contenente i dati del nuovo questionario
   *     da creare.
   * @return Un {@link org.springframework.http.ResponseEntity} contenente l'oggetto
   *     {@link TeacherSurveyDto} del questionario appena creato, con stato HTTP 201 (Created).
   * @apiNote POST - createSurvey - ADMIN/SUPER_ADMIN TRACCIA: "Amministrativi - Creazione di un
   *     questionario di feedback sui docenti" NOTA: Solo amministratori possono creare questionari
   *     per garantire controllo centralizzato
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService
   *     #createSurvey(TeacherSurveyDto)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @PostMapping
  @PreAuthorize("hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Crea nuovo questionario",
      description = "Crea un nuovo questionario di valutazione per un docente con titolo, "
          + "descrizione e domande.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Questionario creato con successo",
          content = @Content(schema = @Schema(implementation = TeacherSurveyDto.class))),
      @ApiResponse(responseCode = "400", description = "Dati richiesta non validi",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - ruolo ADMIN o SUPER_ADMIN richiesto"),
      @ApiResponse(responseCode = "409",
          description = "Questionario già esistente per il docente/corso",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<TeacherSurveyDto> createSurvey(
      @Parameter(description = "Dati del questionario (inclusi titolo, descrizione e domande)",
          required = true)
      @Valid @RequestBody TeacherSurveyDto surveyDto) {
    logger.info("Richiesta pcreazione questionario da amministratore per docente: {}",
        surveyDto.getTeacherId());
    TeacherSurveyDto createdSurvey = surveyService.createSurvey(surveyDto);
    logger.info("Questionario creato con successo con ID: {}", createdSurvey.getId());
    return new ResponseEntity<>(createdSurvey, HttpStatus.CREATED);
  }

  /**
   * Aggiorna un questionario di valutazione esistente.
   *
   * @param id L'ID univoco del questionario da aggiornare.
   * @param surveyDto Un oggetto {@link TeacherSurveyDto} contenente i dati aggiornati per il
   *     questionario.
   * @return Un {@link org.springframework.http.ResponseEntity} contenente l'oggetto
   *     {@link TeacherSurveyDto} del questionario aggiornato, con stato HTTP 200 (OK).
   * @apiNote PUT - updateSurvey - ADMIN/SUPER_ADMIN TRACCIA: Gestione e modifica questionari da
   *     parte amministratori NOTA: Solo amministratori per mantenere controllo centralizzato sui
   *     questionari
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService
   *     #updateSurvey(String, TeacherSurveyDto)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Aggiorna questionario",
      description = "Aggiorna un questionario di valutazione esistente, inclusi titolo, "
          + "descrizione e domande.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Questionario aggiornato con successo",
          content = @Content(schema = @Schema(implementation = TeacherSurveyDto.class))),
      @ApiResponse(responseCode = "400", description = "Dati richiesta non validi",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - ruolo ADMIN o SUPER_ADMIN richiesto"),
      @ApiResponse(responseCode = "404", description = "Questionario non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409",
          description = "Conflitto - Questionario non modificabile nel suo stato attuale",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<TeacherSurveyDto> updateSurvey(
      @Parameter(description = "ID del questionario da aggiornare", required = true,
          example = "uuid-survey-123")
      @PathVariable String id,
      @Parameter(description = "Dati aggiornati del questionario", required = true)
      @Valid @RequestBody TeacherSurveyDto surveyDto) {
    logger.info("Richiesta aggiornamento questionario con ID: {}", id);
    TeacherSurveyDto updatedSurvey = surveyService.updateSurvey(id, surveyDto);
    logger.info("Questionario aggiornato con successo con ID: {}", id);
    return ResponseEntity.ok(updatedSurvey);
  }

  /**
   * Cambia lo stato di un questionario.
   *
   * @param id L'ID univoco del questionario di cui modificare lo stato.
   * @param status Il nuovo stato del questionario (DRAFT/ACTIVE/CLOSED).
   * @return Un {@link org.springframework.http.ResponseEntity} contenente l'oggetto
   *     {@link TeacherSurveyDto} del questionario con stato aggiornato, con stato HTTP 200 (OK).
   * @apiNote PUT - changeSurveyStatus - ADMIN/SUPER_ADMIN TRACCIA: [NON SPECIFICATO/RICHIESTO
   *     NELLA TRACCIA] NOTA: Solo amministratori per controllare quando i questionari sono
   *     attivi/chiusi
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService
   *     #changeSurveyStatus(String,
   *     SurveyStatus)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   * @see it.unimol.microserviceassessmentfeedback.enums.SurveyStatus
   */
  @PutMapping("/{id}/status")
  @PreAuthorize("hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Modifica stato questionario",
      description = "Cambia lo stato di un questionario (DRAFT/ACTIVE/CLOSED)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Stato modificato con successo",
          content = @Content(schema = @Schema(implementation = TeacherSurveyDto.class))),
      @ApiResponse(responseCode = "400",
          description = "Stato non valido o transizione non permessa",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - ruolo ADMIN o SUPER_ADMIN richiesto"),
      @ApiResponse(responseCode = "404", description = "Questionario non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<TeacherSurveyDto> changeSurveyStatus(
      @Parameter(description = "ID Questionario", required = true, example = "uuid-survey-123")
      @PathVariable String id,
      @Parameter(description = "Nuovo stato del questionario", required = true)
      @RequestParam SurveyStatus status) {
    logger.info("Richiesta per modificare stato questionario con ID: {} a stato: {}", id, status);
    TeacherSurveyDto updatedSurvey = surveyService.changeSurveyStatus(id, status);
    logger.info("Stato questionario modificato con successo - ID: {}, nuovo stato: {}", id, status);
    return ResponseEntity.ok(updatedSurvey);
  }

  /**
   * Elimina un questionario di valutazione.
   *
   * @param id L'ID univoco del questionario da eliminare.
   * @return Un {@link org.springframework.http.ResponseEntity} con stato HTTP 204 (No Content) se
   *     il questionario è stato eliminato con successo.
   * @apiNote DELETE - deleteSurvey - ADMIN/SUPER_ADMIN TRACCIA: Gestione eliminazione questionari
   *     da parte amministratori NOTA: Solo amministratori per mantenere controllo sulla rimozione
   *     dei questionari
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService
   *     #deleteSurvey(String)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Elimina questionario",
      description = "Elimina un questionario di valutazione (solo se non ha risposte)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Questionario eliminato con successo"),
      @ApiResponse(responseCode = "401", description = "Accesso non autorizzato - Token JWT "
          + "richiesto"),
      @ApiResponse(responseCode = "403", description = "Accesso vietato - ruolo ADMIN o "
          + "SUPER_ADMIN richiesto"),
      @ApiResponse(responseCode = "404", description = "Questionario non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "Conflitto - Questionario non eliminabile "
          + "perché ha già risposte",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Void> deleteSurvey(
      @Parameter(description = "ID del questionario da eliminare", required = true, example =
          "uuid-survey-123")
      @PathVariable String id) {
    logger.info("Richiesta eliminazione questionario con ID: {}", id);
    surveyService.deleteSurvey(id);
    logger.info("Questionario eliminato con successo con ID: {}", id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Recupera le statistiche generali.
   *
   * @return Un oggetto contenente le statistiche generali sui questionari (numero totale,
   *     distribuzioni per stato, medie valutazioni, etc.)
   * @apiNote GET - Endpoint per statistiche generali - ADMIN/SUPER_ADMIN TRACCIA: [NON
   *     SPECIFICATO/RICHIESTO NELLA TRACCIA] NOTA: Solo amministratori per visualizzazione
   *     statistiche questionari del sistema
   * @see it.unimol.microserviceassessmentfeedback.service.TeacherSurveyService
   *     #getGeneralStatistics()
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping("/statistics")
  @PreAuthorize("hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni statistiche generali",
      description = "Recupera statistiche generali sui questionari del sistema")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Statistiche recuperate con successo"),
      @ApiResponse(responseCode = "401", description = "Accesso non autorizzato"),
      @ApiResponse(responseCode = "403", description = "Accesso vietato - Solo amministratori"),
      @ApiResponse(responseCode = "500", description = "Errore interno del server")
  })
  public ResponseEntity<Object> getGeneralStatistics() {
    logger.info("Richiesta statistiche generali questionari");
    Object statistics = surveyService.getGeneralStatistics();
    logger.info("Statistiche generali recuperate");
    return ResponseEntity.ok(statistics);
  }
}