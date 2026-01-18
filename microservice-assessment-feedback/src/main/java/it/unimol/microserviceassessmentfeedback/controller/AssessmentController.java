package it.unimol.microserviceassessmentfeedback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unimol.microserviceassessmentfeedback.common.exception.ErrorResponse;
import it.unimol.microserviceassessmentfeedback.common.util.JwtRequestHelper;
import it.unimol.microserviceassessmentfeedback.dto.AssessmentDto;
import it.unimol.microserviceassessmentfeedback.enums.RoleType;
import it.unimol.microserviceassessmentfeedback.service.AssessmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per la gestione delle valutazioni (Assessment). Fornisce endpoint per creare,
 * leggere, aggiornare ed eliminare valutazioni, con controllo degli accessi basato sui ruoli.
 */
@RestController
@RequestMapping("/api/v1/assessments")
@Tag(name = "Assessment Controller", description = "API per la gestione di Assessment "
    + "(Valutazioni)")
@SecurityRequirement(name = "bearerAuth")
public class AssessmentController {

  private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);
  private final AssessmentService assessmentService;
  @Autowired
  private JwtRequestHelper jwtRequestHelper;

  // ============ Costruttore ============

  @Autowired
  public AssessmentController(AssessmentService assessmentService) {
    this.assessmentService = assessmentService;
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  /**
   * Ottiene tutte le valutazioni presenti nel sistema.
   *
   * @return Una lista di oggetti {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto}
   *     che rappresentano tutte le valutazioni presenti.
   * @apiNote GET - getAllAssessments - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: Implicito per gestione
   *     valutazioni da parte docenti NOTA: ADMIN/SUPER_ADMIN aggiunti per coerenza architetturale e
   *     supervisione amministrativa
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService#getAllAssessments()
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni tutte le valutazioni",
      description = "Ottieni tutte le valutazioni (accessibili a TEACHER e ADMIN)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Valutazioni trovate con successo",
          content = @Content(schema = @Schema(implementation = AssessmentDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - ruolo TEACHER o ADMIN richiesto"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<AssessmentDto>> getAllAssessments() {
    logger.info("Richiesta per ottenere tutte le valutazioni");
    return ResponseEntity.ok(assessmentService.getAllAssessments());
  }

  /**
   * Ottiene una valutazione specifica tramite il suo ID.
   *
   * @param id L'ID univoco della valutazione da recuperare.
   * @return Un oggetto {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto} che
   *     rappresenta la valutazione richiesta.
   * @apiNote GET - getAssessmentById - TEACHER/ADMIN/SUPER_ADMIN o STUDENT (solo per le proprie)
   *     TRACCIA: "Studenti - Visualizzazione del feedback ricevuto" + gestione docenti
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService
   *     #getAssessmentById(String)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   **/
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_STUDENT + "') "
      + "or hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni valutazione tramite ID",
      description = "Ottiene una specifica valutazione tramite il suo ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Valutazione trovata con successo",
          content = @Content(schema = @Schema(implementation = AssessmentDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - autorizzazione insufficiente"),
      @ApiResponse(responseCode = "404", description = "Valutazione non trovata",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<AssessmentDto> getAssessmentById(@PathVariable String id) {
    logger.info("Richiesta per ottenere valutazione con ID: {}", id);
    return ResponseEntity.ok(assessmentService.getAssessmentById(id));
  }

  /**
   * Ottiene tutte le valutazioni associate a uno specifico compito.
   *
   * @param id L'ID univoco del compito di cui recuperare le valutazioni.
   * @return Una lista di oggetti {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto}
   *     che rappresentano le valutazioni per il compito specificato.
   * @apiNote GET - getAssessmentsByAssignment - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: Gestione
   *     valutazioni da parte docenti (per compiti) NOTA: ADMIN/SUPER_ADMIN per supervisione
   *     amministrativa
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService
   *     #getAssessmentsByAssignment(String)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   **/
  @GetMapping("/assignment/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni valutazioni per compito",
      description = "Ottiene tutte le valutazioni per uno specifico compito")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Valutazioni trovate con successo",
          content = @Content(schema = @Schema(implementation = AssessmentDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - ruolo TEACHER o ADMIN richiesto"),
      @ApiResponse(responseCode = "404", description = "Compito non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<AssessmentDto>> getAssessmentsByAssignment(
      @PathVariable String id) {
    logger.info("Richiesta per ottenere valutazioni per compito con ID: {}", id);
    return ResponseEntity.ok(assessmentService.getAssessmentsByAssignment(id));
  }

  /**
   * Ottiene tutte le valutazioni associate a uno specifico esame.
   *
   * @param id L'ID univoco dell'esame di cui recuperare le valutazioni.
   * @return Una lista di oggetti {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto}
   *     che rappresentano le valutazioni per l'esame specificato.
   * @apiNote GET - getAssessmentsByExam - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: "Docenti - Fornitura
   *     di feedback dettagliato sui compiti e sugli esami" NOTA: ADMIN/SUPER_ADMIN per supervisione
   *     amministrativa
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService#getAssessmentsByExam(
   *String)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   **/
  @GetMapping("/exam/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni valutazioni per esame",
      description = "Ottiene tutte le valutazioni per uno specifico esame")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Valutazioni trovate con successo",
          content = @Content(schema = @Schema(implementation = AssessmentDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - ruolo TEACHER o ADMIN richiesto"),
      @ApiResponse(responseCode = "404", description = "Esame non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<AssessmentDto>> getAssessmentsByExam(@PathVariable String id) {
    logger.info("Richiesta per ottenere valutazioni per esame con ID: {}", id);
    return ResponseEntity.ok(assessmentService.getAssessmentsByExam(id));
  }

  /**
   * Ottiene tutte le valutazioni associate a uno specifico studente.
   *
   * @param id L'ID univoco dello studente di cui recuperare le valutazioni.
   * @return Una lista di oggetti {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto}
   *     che rappresenta le valutazioni per lo studente specificato.
   * @apiNote GET - getAssessmentsByStudent - TEACHER/ADMIN/SUPER_ADMIN o STUDENT (solo per se
   *     stesso) TRACCIA: "Studenti - Visualizzazione del feedback ricevuto" + gestione docenti
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService
   *     #getAssessmentsByStudentId(String)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   **/
  @GetMapping("/student/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_STUDENT + "') "
      + "or hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni valutazioni per studente",
      description = "Ottiene tutte le valutazioni per uno specifico studente")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Valutazioni trovate con successo",
          content = @Content(schema = @Schema(implementation = AssessmentDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - autorizzazione insufficiente"),
      @ApiResponse(responseCode = "404", description = "Studente non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<AssessmentDto>> getAssessmentsByStudent(@PathVariable String id) {
    logger.info("Richiesta per ottenere valutazioni per studente con ID: {}", id);
    return ResponseEntity.ok(assessmentService.getAssessmentsByStudentId(id));
  }

  /**
   * Ottiene tutte le valutazioni associate a uno specifico corso.
   *
   * @param id L'ID univoco del corso di cui recuperare le valutazioni.
   * @return Una lista di oggetti {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto}
   *     che rappresentano le valutazioni per il corso specificato.
   * @apiNote GET - getAssessmentsByCourse - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: Gestione valutazioni
   *     da parte docenti (per corso) NOTA: ADMIN/SUPER_ADMIN per supervisione amministrativa
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService
   *     #getAssessmentsByCourse(String)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   **/
  @GetMapping("/course/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni valutazioni per corso",
      description = "Ottiene tutte le valutazioni per uno specifico corso")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Valutazioni trovate con successo",
          content = @Content(schema = @Schema(implementation = AssessmentDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - autorizzazione insufficiente"),
      @ApiResponse(responseCode = "404", description = "Corso non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<AssessmentDto>> getAssessmentsByCourse(@PathVariable String id) {
    logger.info("Richiesta per ottenere valutazioni per corso con ID: {}", id);
    return ResponseEntity.ok(assessmentService.getAssessmentsByCourse(id));
  }

  /**
   * Ottiene tutte le valutazioni personali dello studente autenticato.
   *
   * @param request L'oggetto {@link jakarta.servlet.http.HttpServletRequest} contenente le
   *                informazioni della richiesta, utilizzato per estrarre l'ID dello studente
   *                autenticato.
   * @return Una lista di oggetti {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto}
   *     che rappresenta tutte le valutazioni associate allo studente autenticato.
   * @apiNote GET - getPersonalAssessments - STUDENT/ADMIN/SUPER_ADMIN TRACCIA:
   *     "Studenti - Visualizzazione del feedback ricevuto" NOTA: ADMIN/SUPER_ADMIN per
   *     supervisione amministrativa.
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService
   *     #getAssessmentsByStudentId(String)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   * @see JwtRequestHelper#getUsernameFromRequest(HttpServletRequest)
   * @see JwtRequestHelper#extractStudentIdFromRequest(HttpServletRequest)
   */
  @GetMapping("/personal")
  @PreAuthorize("hasRole('" + RoleType.ROLE_STUDENT + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni valutazioni personali",
      description = "Ottiene tutte le valutazioni per lo studente autenticato")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Valutazioni personali trovate con successo",
          content = @Content(schema = @Schema(implementation = AssessmentDto.class))),
      @ApiResponse(responseCode = "401", description = "Accesso non autorizzato"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - ruolo STUDENT richiesto"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<AssessmentDto>> getPersonalAssessments(HttpServletRequest request) {
    String username = jwtRequestHelper.getUsernameFromRequest(request);
    logger.info("Richiesta per ottenere valutazioni personali per utente: {}", username);

    String studentId = jwtRequestHelper.extractStudentIdFromRequest(request);
    return ResponseEntity.ok(assessmentService.getAssessmentsByStudentId(studentId));
  }

  /**
   * Ottiene i dettagli di una valutazione personale specifica.
   *
   * @param id L'ID univoco della valutazione di cui recuperare i dettagli. Per gli studenti, questo
   *           ID deve corrispondere a una delle proprie valutazioni.
   * @return Un oggetto {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto} che
   *     rappresenta i dettagli della valutazione richiesta.
   * @apiNote GET - getPersonalAssessmentDetails - STUDENT/ADMIN/SUPER_ADMIN TRACCIA: "Studenti -
   *     Visualizzazione del feedback ricevuto" (dettagli specifici) NOTA: ADMIN/SUPER_ADMIN per
   *     supervisione amministrativa
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService#getAssessmentById(
   *String)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   **/
  @GetMapping("/personal/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_STUDENT + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni dettagli valutazione personale",
      description = "Ottieni dettagli di una specifica valutazione per lo studente autenticato")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Valutazione personale trovata con successo",
          content = @Content(schema = @Schema(implementation = AssessmentDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - Token JWT richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - valutazione non accessibile"),
      @ApiResponse(responseCode = "404", description = "Valutazione non trovata",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<AssessmentDto> getPersonalAssessmentDetails(@PathVariable String id) {
    logger.info("Richiesta per ottenere dettagli valutazione personale con ID: {}", id);
    return ResponseEntity.ok(assessmentService.getAssessmentById(id));
  }

  /**
   * Crea una nuova valutazione.
   *
   * @param assessmentDto Un oggetto
   *                      {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto}
   *                      contenente i dati della nuova valutazione da creare.
   * @param request       L'oggetto {@link jakarta.servlet.http.HttpServletRequest} utilizzato per
   *                      estrarre l'ID del docente autenticato.
   * @return Un {@link org.springframework.http.ResponseEntity} contenente l'oggetto
   *     {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto} della valutazione appena
   *     creata, con stato HTTP 201 (Created).
   * @apiNote POST - createAssessment - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: "Docenti - Fornitura di
   *     feedback dettagliato sui compiti e sugli esami" NOTA: ADMIN/SUPER_ADMIN per gestione
   *     amministrativa delle valutazioni
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService#createAssessment(
   *AssessmentDto)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   * @see JwtRequestHelper#getUsernameFromRequest(HttpServletRequest)
   * @see JwtRequestHelper#extractTeacherIdFromRequest(HttpServletRequest)
   */
  @PostMapping
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Crea valutazione",
      description = "Crea una nuova valutazione")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Valutazione creata con successo",
          content = @Content(schema = @Schema(implementation = AssessmentDto.class))),
      @ApiResponse(responseCode = "400", description = "Dati richiesta non validi",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "Accesso non autorizzato"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - ruolo TEACHER richiesto"
              + " o non autorizzato per questo corso"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<AssessmentDto> createAssessment(
      @Valid @RequestBody AssessmentDto assessmentDto,
      HttpServletRequest request) {
    String username = jwtRequestHelper.getUsernameFromRequest(request);
    logger.info("Richiesta per creare nuova valutazione da utente: {}", username);

    String teacherId = jwtRequestHelper.extractTeacherIdFromRequest(request);
    assessmentDto.setTeacherId(teacherId);

    AssessmentDto createdAssessment = assessmentService.createAssessment(assessmentDto);
    logger.info("Valutazione creata con successo con ID: {}", createdAssessment.getId());

    return new ResponseEntity<>(createdAssessment, HttpStatus.CREATED);
  }

  /**
   * Aggiorna una valutazione esistente.
   *
   * @param id            L'ID univoco della valutazione da aggiornare.
   * @param assessmentDto Un oggetto
   *                      {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto}
   *                      contenente i dati aggiornati per la valutazione.
   * @return Un {@link org.springframework.http.ResponseEntity} contenente l'oggetto
   *     {@link it.unimol.microserviceassessmentfeedback.dto.AssessmentDto} della valutazione
   *     aggiornata, con stato HTTP 200 (OK).
   * @apiNote PUT - updateAssessment - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: Gestione e modifica
   *     feedback da parte docenti NOTA: ADMIN/SUPER_ADMIN per correzioni amministrative
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService#updateAssessment(
   *String, AssessmentDto)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Aggiorna valutazione",
      description = "Aggiorna una valutazione esistente")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Valutazione aggiornata con successo",
          content = @Content(schema = @Schema(implementation = AssessmentDto.class))),
      @ApiResponse(responseCode = "400", description = "Dati richiesta non validi",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "Accesso non autorizzato"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - non autorizzato a modificare questa valutazione"),
      @ApiResponse(responseCode = "404", description = "Valutazione non trovata",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<AssessmentDto> updateAssessment(@PathVariable String id,
      @Valid @RequestBody AssessmentDto assessmentDto) {
    logger.info("Richiesta per aggiornare valutazione con ID: {}", id);
    AssessmentDto updatedAssessment = assessmentService.updateAssessment(id, assessmentDto);
    logger.info("Valutazione aggiornata con successo con ID: {}", id);

    return ResponseEntity.ok(updatedAssessment);
  }

  /**
   * Elimina una valutazione esistente.
   *
   * @param id L'ID univoco della valutazione da eliminare.
   * @return Un {@link org.springframework.http.ResponseEntity} con stato HTTP 204 (No Content) se
   *     la valutazione è stata eliminata con successo.
   * @apiNote DELETE - deleteAssessment - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: Gestione valutazioni da
   *     parte docenti NOTA: ADMIN/SUPER_ADMIN per eliminazioni amministrative
   * @see it.unimol.microserviceassessmentfeedback.service.AssessmentService#deleteAssessment(
   *String)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Elimina valutazione",
      description = "Elimina una valutazione")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Valutazione eliminata con successo"),
      @ApiResponse(responseCode = "401", description = "Accesso non autorizzato"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - non autorizzato a eliminare questa valutazione"),
      @ApiResponse(responseCode = "404", description = "Valutazione non trovata",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Void> deleteAssessment(@PathVariable String id) {
    logger.info("Richiesta per eliminare valutazione con ID: {}", id);
    assessmentService.deleteAssessment(id);
    logger.info("Valutazione eliminata con successo con ID: {}", id);

    return ResponseEntity.noContent().build();
  }
}