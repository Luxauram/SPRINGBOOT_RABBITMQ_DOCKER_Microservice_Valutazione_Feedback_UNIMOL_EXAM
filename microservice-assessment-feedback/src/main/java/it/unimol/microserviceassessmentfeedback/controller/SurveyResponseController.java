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
import it.unimol.microserviceassessmentfeedback.dto.SurveyResponseDto;
import it.unimol.microserviceassessmentfeedback.enums.RoleType;
import it.unimol.microserviceassessmentfeedback.service.SurveyResponseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per la gestione delle risposte ai questionari (SurveyResponse). Fornisce
 * endpoint per gestire le risposte degli studenti ai questionari di feedback sui docenti, con
 * controllo degli accessi basato sui ruoli.
 */
@RestController
@RequestMapping("/api/v1/surveys")
@Tag(name = "SurveyResponse Controller", description = "API per la gestione di SurveyResponse "
    + "(Risposte Questionario)")
@SecurityRequirement(name = "bearerAuth")
public class SurveyResponseController {

  private static final Logger logger = LoggerFactory.getLogger(SurveyResponseController.class);
  private final SurveyResponseService responseService;
  @Autowired
  private JwtRequestHelper jwtRequestHelper;

  // ============ Costruttore ============

  @Autowired
  public SurveyResponseController(SurveyResponseService responseService) {
    this.responseService = responseService;
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  /**
   * Ottiene tutte le risposte associate a un questionario specifico.
   *
   * @param id L'ID univoco del questionario di cui recuperare le risposte.
   * @param request L'oggetto HttpServletRequest per estrarre l'ID utente.
   * @return Una lista di oggetti {@link SurveyResponseDto} che rappresentano le risposte al
   *     questionario specificato.
   * @apiNote GET - getResponsesBySurveyId - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: Implicito per
   *     gestione questionari feedback docenti da parte amministrativa NOTA: TEACHER per
   *     visualizzazione feedback ricevuti, ADMIN/SUPER_ADMIN per supervisione
   * @see it.unimol.microserviceassessmentfeedback.service.SurveyResponseService
   *     #getResponsesBySurveyId(String, String)
   * @see JwtRequestHelper#getUserIdFromRequest(HttpServletRequest)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping("/{id}/responses")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni risposte tramite ID questionario",
      description = "Recupera tutte le risposte per un questionario specifico (solo docenti del "
          + "corso)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Risposte trovate con successo",
          content = @Content(schema = @Schema(implementation = SurveyResponseDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - JWT token richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - Ruolo TEACHER/ADMIN richiesto o non sei docente "
              + "di questo corso"),
      @ApiResponse(responseCode = "404", description = "Questionario non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<SurveyResponseDto>> getResponsesBySurveyId(
      @Parameter(description = "ID Questionario", required = true, example = "uuid-questionario-1")
      @PathVariable String id,
      HttpServletRequest request) {
    String userId = jwtRequestHelper.getUserIdFromRequest(request);
    logger.info("Richiesta per ottenere risposte questionario con ID: {} da utente: {}", id,
        userId);
    return ResponseEntity.ok(responseService.getResponsesBySurveyId(id, userId));
  }

  /**
   * Ottiene tutti i commenti associati a un questionario specifico.
   *
   * @param id L'ID univoco del questionario di cui recuperare i commenti.
   * @param request L'oggetto HttpServletRequest per estrarre l'ID utente.
   * @return Una lista di oggetti {@link SurveyResponseDto} che rappresentano i commenti al
   *     questionario specificato.
   * @apiNote GET - getSurveyComments - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: Implicito per gestione
   *     feedback dettagliato da parte docenti e amministrativi NOTA: TEACHER per visualizzazione
   *     commenti ricevuti, ADMIN/SUPER_ADMIN per supervisione
   * @see it.unimol.microserviceassessmentfeedback.service.SurveyResponseService
   *     #getSurveyComments(String, String)
   * @see JwtRequestHelper#getUserIdFromRequest(HttpServletRequest)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping("/{id}/comments")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni commenti del questionario",
      description = "Recupera tutti i commenti per un questionario specifico (solo docenti del "
          + "corso)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Commenti trovati con successo",
          content = @Content(schema = @Schema(implementation = SurveyResponseDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - JWT token richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - Ruolo TEACHER/ADMIN richiesto o non sei docente "
              + "di questo corso"),
      @ApiResponse(responseCode = "404", description = "Questionario non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<SurveyResponseDto>> getSurveyComments(
      @Parameter(description = "ID Questionario", required = true, example = "uuid-questionario-1")
      @PathVariable String id,
      HttpServletRequest request) {
    String userId = jwtRequestHelper.getUserIdFromRequest(request);
    logger.info("Richiesta per ottenere commenti questionario con ID: {} da utente: {}", id,
        userId);
    return ResponseEntity.ok(responseService.getSurveyComments(id, userId));
  }

  /**
   * Ottiene i risultati aggregati di un questionario specifico.
   *
   * @param id L'ID univoco del questionario di cui recuperare i risultati aggregati.
   * @param request L'oggetto HttpServletRequest per estrarre l'ID utente.
   * @return Una mappa contenente i risultati aggregati del questionario specificato.
   * @apiNote GET - getSurveyResults - TEACHER/ADMIN/SUPER_ADMIN TRACCIA: Implicito per gestione e
   *     analisi feedback sui docenti da parte amministrativa NOTA: TEACHER per visualizzazione
   *     risultati propri, ADMIN/SUPER_ADMIN per analisi amministrativa
   * @see it.unimol.microserviceassessmentfeedback.service.SurveyResponseService
   *     #getSurveyResults(String, String)
   * @see JwtRequestHelper#getUserIdFromRequest(HttpServletRequest)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping("/{id}/results")
  @PreAuthorize("hasRole('" + RoleType.ROLE_TEACHER + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "') "
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni risultati aggregati del questionario",
      description = "Recupera risultati aggregati per un questionario specifico (solo docenti del"
          + " corso)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Risultati del questionario trovati con successo",
          content = @Content(schema = @Schema(implementation = Map.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - JWT token richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - Ruolo TEACHER/ADMIN richiesto o non sei docente "
              + "di questo corso"),
      @ApiResponse(responseCode = "404", description = "Questionario non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Double>> getSurveyResults(
      @Parameter(description = "ID Questionario", required = true, example = "uuid-questionario-1")
      @PathVariable String id,
      HttpServletRequest request) {
    String userId = jwtRequestHelper.getUserIdFromRequest(request);
    logger.info("Richiesta per ottenere risultati questionario con ID: {} da utente: {}", id,
        userId);
    return ResponseEntity.ok(responseService.getSurveyResults(id, userId));
  }

  /**
   * Invia le risposte di uno studente a un questionario.
   *
   * @param surveyId L'ID univoco del questionario per cui inviare le risposte.
   * @param responseDtos Una lista di oggetti {@link SurveyResponseDto} contenenti le risposte al
   *     questionario.
   * @param request L'oggetto {@link jakarta.servlet.http.HttpServletRequest} utilizzato per
   *     estrarre l'ID dello studente autenticato.
   * @return Un {@link org.springframework.http.ResponseEntity} contenente la lista delle risposte
   *     inviate, con stato HTTP 201 (Created).
   * @apiNote POST - submitSurveyResponses - STUDENT/ADMIN/SUPER_ADMIN TRACCIA: "Studenti -
   *     Compilazione del questionario di feedback sui docenti" NOTA: ADMIN/SUPER_ADMIN per test e
   *     supervisione amministrativa
   * @see it.unimol.microserviceassessmentfeedback.service.SurveyResponseService
   *     #submitSurveyResponses(String, List, String)
   * @see JwtRequestHelper#getUserIdFromRequest(HttpServletRequest)
   * @see JwtRequestHelper#getUsernameFromRequest(HttpServletRequest)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @PostMapping("/{surveyId}/responses")
  @PreAuthorize("hasRole('" + RoleType.ROLE_STUDENT + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "')"
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Invia le risposte del questionario",
      description = "Invia multiple risposte per un questionario specifico (solo studenti "
          + "iscritti al corso)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201",
          description = "Risposte del questionario inviate con successo",
          content = @Content(schema = @Schema(implementation = SurveyResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Dati richiesta non validi",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - JWT token richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - Ruolo STUDENT richiesto o non sei iscritto al corso"),
      @ApiResponse(responseCode = "404", description = "Questionario non trovato",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "Hai già compilato questo questionario",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<SurveyResponseDto>> submitSurveyResponses(
      @Parameter(description = "ID Questionario", required = true, example = "uuid-questionario-1")
      @PathVariable String surveyId,
      @Parameter(description = "Lista di risposte al questionario", required = true)
      @Valid @RequestBody List<SurveyResponseDto> responseDtos,
      HttpServletRequest request) {

    String authenticatedUserId = jwtRequestHelper.getUserIdFromRequest(request);
    String username = jwtRequestHelper.getUsernameFromRequest(request);
    logger.info("Richiesta per inviare risposte questionario con ID: {} da utente: {}", surveyId,
        username);

    responseDtos.forEach(dto -> dto.setStudentId(authenticatedUserId));

    List<SurveyResponseDto> submittedResponses = responseService.submitSurveyResponses(surveyId,
        responseDtos, authenticatedUserId);
    logger.info("Risposte questionario inviate con successo per questionario: {} da utente: {}",
        surveyId, username);

    return new ResponseEntity<>(submittedResponses, HttpStatus.CREATED);
  }

  /**
   * Ottiene tutte le risposte personali dello studente autenticato.
   *
   * @param request L'oggetto {@link jakarta.servlet.http.HttpServletRequest} utilizzato per
   *     estrarre l'ID dello studente autenticato.
   * @return Una lista di oggetti {@link SurveyResponseDto} che rappresenta tutte le risposte
   *     inviate dallo studente autenticato.
   * @apiNote GET - getMyResponses - STUDENT/ADMIN/SUPER_ADMIN TRACCIA: Implicito per
   *     visualizzazione storico compilazioni questionari da parte studenti NOTA: ADMIN/SUPER_ADMIN
   *     per supervisione amministrativa
   * @see it.unimol.microserviceassessmentfeedback.service.SurveyResponseService
   *     #getResponsesByStudentId(String)
   * @see JwtRequestHelper#getUserIdFromRequest(HttpServletRequest)
   * @see JwtRequestHelper#getUsernameFromRequest(HttpServletRequest)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping("/my-responses")
  @PreAuthorize("hasRole('" + RoleType.ROLE_STUDENT + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "')"
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni le mie risposte ai questionari",
      description = "Recupera tutte le risposte inviate dall'utente autenticato")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Risposte trovate con successo",
          content = @Content(schema = @Schema(implementation = SurveyResponseDto.class))),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - JWT token richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - Ruolo STUDENT richiesto"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<SurveyResponseDto>> getMyResponses(HttpServletRequest request) {
    String userId = jwtRequestHelper.getUserIdFromRequest(request);
    String username = jwtRequestHelper.getUsernameFromRequest(request);
    logger.info("Richiesta per ottenere risposte personali da utente: {}", username);

    return ResponseEntity.ok(responseService.getResponsesByStudentId(userId));
  }

  /**
   * Ottiene tutti i questionari disponibili per lo studente autenticato.
   *
   * @param request L'oggetto {@link jakarta.servlet.http.HttpServletRequest} utilizzato per
   *     estrarre l'ID dello studente autenticato.
   * @return Una lista di questionari disponibili per lo studente autenticato.
   * @apiNote GET - getAvailableSurveys - STUDENT/ADMIN/SUPER_ADMIN TRACCIA: "Studenti -
   *     Compilazione del questionario di feedback sui docenti" (prerequisito: visualizzazione
   *     questionari disponibili) NOTA: SUPER_ADMIN per supervisione amministrativa
   * @see it.unimol.microserviceassessmentfeedback.service.SurveyResponseService
   *     #getAvailableSurveysForStudent(String)
   * @see JwtRequestHelper#getUserIdFromRequest(HttpServletRequest)
   * @see JwtRequestHelper#getUsernameFromRequest(HttpServletRequest)
   * @see it.unimol.microserviceassessmentfeedback.enums.RoleType
   */
  @GetMapping("/available")
  @PreAuthorize("hasRole('" + RoleType.ROLE_STUDENT + "') "
      + "or hasRole('" + RoleType.ROLE_ADMIN + "')"
      + "or hasRole('" + RoleType.ROLE_SUPER_ADMIN + "')")
  @Operation(summary = "Ottieni questionari disponibili",
      description = "Recupera tutti i questionari attivi per i corsi a cui lo studente è iscritto")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Questionari disponibili trovati con successo"),
      @ApiResponse(responseCode = "401",
          description = "Accesso non autorizzato - JWT token richiesto"),
      @ApiResponse(responseCode = "403",
          description = "Accesso vietato - Ruolo STUDENT richiesto"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> getAvailableSurveys(HttpServletRequest request) {
    String userId = jwtRequestHelper.getUserIdFromRequest(request);
    String username = jwtRequestHelper.getUsernameFromRequest(request);
    logger.info("Richiesta per ottenere questionari disponibili da utente: {}", username);

    return ResponseEntity.ok(responseService.getAvailableSurveysForStudent(userId));
  }
}