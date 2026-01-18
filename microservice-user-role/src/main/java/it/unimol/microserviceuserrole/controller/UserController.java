package it.unimol.microserviceuserrole.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unimol.microserviceuserrole.dto.auth.ChangePasswordRequestDto;
import it.unimol.microserviceuserrole.dto.role.AssignRoleDto;
import it.unimol.microserviceuserrole.dto.user.CreateUserDto;
import it.unimol.microserviceuserrole.dto.user.UpdateUserProfileDto;
import it.unimol.microserviceuserrole.dto.user.UserDto;
import it.unimol.microserviceuserrole.dto.user.UserProfileDto;
import it.unimol.microserviceuserrole.enums.RoleType;
import it.unimol.microserviceuserrole.exceptions.InvalidRequestException;
import it.unimol.microserviceuserrole.exceptions.UnknownUserException;
import it.unimol.microserviceuserrole.service.RoleService;
import it.unimol.microserviceuserrole.service.TokenJwtService;
import it.unimol.microserviceuserrole.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per la gestione degli utenti.
 * Fornisce endpoint per CRUD utenti, gestione profili e password.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "API per la gestione degli utenti")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private RoleService roleService;

  @Autowired
  private TokenJwtService tokenService;

  // ============= ENDPOINTS PUBBLICI =============

  /**
   * Crea il SuperAdmin iniziale del sistema.
   *
   * @param request i dati per la creazione del SuperAdmin
   * @return i dati del SuperAdmin creato
   */
  @Operation(summary = "Crea SuperAdmin iniziale", description = "Crea il primo SuperAdmin del "
      + "sistema se non esiste già")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "SuperAdmin creato con successo"),
      @ApiResponse(responseCode = "409", description = "SuperAdmin già esistente"),
      @ApiResponse(responseCode = "400", description = "Dati non validi")
  })
  @PostMapping("/superadmin/init")
  public ResponseEntity<UserDto> createSuperAdmin(@Valid @RequestBody CreateUserDto request) {
    try {
      UserDto superAdmin = userService.createSuperAdminIfNotExists(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(superAdmin);
    } catch (InvalidRequestException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // ============= ENDPOINTS ADMIN =============

  /**
   * Crea un nuovo utente nel sistema.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @param request i dati del nuovo utente
   * @return i dati dell'utente creato
   */
  @Operation(summary = "Crea nuovo utente", description = "Crea un nuovo utente nel sistema")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Utente creato con successo"),
      @ApiResponse(responseCode = "400", description = "Dati non validi"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
      @ApiResponse(responseCode = "409", description = "Username o email già esistente")
  })
  @PostMapping
  public ResponseEntity<UserDto> createUser(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody CreateUserDto request) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      roleService.checkRole(token, RoleType.ADMIN);

      UserDto user = userService.createUser(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(user);
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (InvalidRequestException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Ottiene la lista di tutti gli utenti registrati.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @return la lista dei profili utente
   */
  @Operation(summary = "Ottieni tutti gli utenti", description = "Restituisce la lista di tutti "
      + "gli utenti")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista utenti recuperata con successo"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti")
  })
  @GetMapping
  public ResponseEntity<List<UserProfileDto>> getAllUsers(
      @RequestHeader("Authorization") String authHeader) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      roleService.checkRole(token, RoleType.ADMIN);

      List<UserProfileDto> users = userService.getAllUsers();
      return ResponseEntity.ok(users);
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Ottiene i dettagli di un utente specifico.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @param id l'ID dell'utente da cercare
   * @return i dati dell'utente o un messaggio di errore
   */
  @Operation(summary = "Ottieni utente per ID", description = "Restituisce i dettagli di un "
      + "utente specifico")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Utente trovato"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
      @ApiResponse(responseCode = "404", description = "Utente non trovato")
  })
  @GetMapping("/{id}")
  public ResponseEntity<?> getUserById(
      @RequestHeader("Authorization") String authHeader,
      @PathVariable String id) {

    String token = null;

    try {
      // STEP 1: Estrazione token
      System.out.println("=== STEP 1: Estrazione Token ===");
      token = tokenService.extractTokenFromHeader(authHeader);
      System.out.println("Token estratto con successo");

    } catch (Exception e) {
      System.out.println("ERRORE in extractTokenFromHeader: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Errore estrazione token", "message", e.getMessage()));
    }

    try {
      // STEP 2: Check Role
      System.out.println("=== STEP 2: Check Role ===");
      roleService.checkRole(token, RoleType.ADMIN);
      System.out.println("Check role passato con successo");

    } catch (SecurityException e) {
      System.out.println("ERRORE SecurityException: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "Privilegi insufficienti", "message", e.getMessage()));
    }

    try {
      // STEP 3: Find User
      System.out.println("=== STEP 3: Find User ===");
      System.out.println("Cercando utente con ID: " + id);
      UserDto user = userService.findById(id);
      System.out.println("Utente trovato con successo");

      return ResponseEntity.ok(user);

    } catch (UnknownUserException e) {
      System.out.println("ERRORE UnknownUserException: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Utente non trovato", "message", e.getMessage()));
    } catch (Exception e) {
      System.out.println(
          "ERRORE in findById: " + e.getClass().getSimpleName() + " - " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Errore ricerca utente", "message", e.getMessage(), "type",
              e.getClass().getSimpleName()));
    }
  }

  /**
   * Aggiorna i dati di un utente esistente.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @param id l'ID dell'utente da aggiornare
   * @param request i nuovi dati dell'utente
   * @return i dati aggiornati dell'utente
   */
  @Operation(summary = "Aggiorna utente", description = "Aggiorna i dati di un utente esistente")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Utente aggiornato con successo"),
      @ApiResponse(responseCode = "400", description = "Dati non validi"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
      @ApiResponse(responseCode = "404", description = "Utente non trovato")
  })
  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(
      @RequestHeader("Authorization") String authHeader,
      @PathVariable String id,
      @Valid @RequestBody UpdateUserProfileDto request) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      roleService.checkRole(token, RoleType.ADMIN);

      UserDto updatedUser = userService.updateUser(id, request);
      return ResponseEntity.ok(updatedUser);
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (UnknownUserException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Elimina un utente dal sistema.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @param id l'ID dell'utente da eliminare
   * @return una risposta vuota o un codice di errore
   */
  @Operation(summary = "Elimina utente", description = "Elimina un utente dal sistema")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Utente eliminato con successo"),
      @ApiResponse(responseCode = "400", description = "Non puoi eliminare te stesso"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
      @ApiResponse(responseCode = "404", description = "Utente non trovato")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(
      @RequestHeader("Authorization") String authHeader,
      @PathVariable String id) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      roleService.checkRole(token, RoleType.ADMIN);

      // Impedisce all'admin di eliminare se stesso
      if (tokenService.extractUserId(token).equals(id)) {
        return ResponseEntity.badRequest().build();
      }

      boolean deleted = userService.deleteUser(id);
      if (deleted) {
        return ResponseEntity.noContent().build();
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // ============= ENDPOINTS PROFILO UTENTE =============

  /**
   * Ottiene il profilo dell'utente autenticato corrente.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @return il profilo dell'utente
   */
  @Operation(summary = "Ottieni profilo utente corrente", description = "Restituisce il profilo "
      + "dell'utente autenticato")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Profilo ottenuto con successo",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              UserProfileDto.class))),
      @ApiResponse(responseCode = "401", description = "Token non valido"),
      @ApiResponse(responseCode = "404", description = "Utente non trovato")
  })
  @GetMapping("/profile")
  public ResponseEntity<UserProfileDto> getUserProfile(
      @RequestHeader("Authorization") String authHeader) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      UserProfileDto profile = userService.getCurrentUserProfile(token);
      return ResponseEntity.ok(profile);
    } catch (UnknownUserException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  /**
   * Aggiorna il profilo dell'utente autenticato corrente.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @param request i nuovi dati del profilo
   * @return il profilo aggiornato
   */
  @Operation(summary = "Aggiorna profilo utente corrente", description = "Aggiorna il profilo "
      + "dell'utente autenticato")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Profilo aggiornato con successo"),
      @ApiResponse(responseCode = "400", description = "Dati non validi"),
      @ApiResponse(responseCode = "401", description = "Token non valido"),
      @ApiResponse(responseCode = "404", description = "Utente non trovato")
  })
  @PutMapping("/profile")
  public ResponseEntity<UserProfileDto> updateUserProfile(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody UpdateUserProfileDto request) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      UserProfileDto profile = userService.updateCurrentUserProfile(token, request);
      return ResponseEntity.ok(profile);
    } catch (UnknownUserException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  // ============= ENDPOINTS GESTIONE PASSWORD =============

  /**
   * Cambia la password dell'utente autenticato.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @param request i dati contenenti la password attuale e quella nuova
   * @return una risposta vuota o un codice di errore
   */
  @Operation(summary = "Cambia password", description = "Cambia la password dell'utente corrente")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Password cambiata con successo"),
      @ApiResponse(responseCode = "400", description = "Password attuale errata o nuova password "
          + "non valida"),
      @ApiResponse(responseCode = "401", description = "Token non valido")
  })
  @PutMapping("/change-password")
  public ResponseEntity<Void> changePassword(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody ChangePasswordRequestDto request) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      boolean success = userService.changePassword(token, request.currentPassword(),
          request.newPassword());

      if (success) {
        return ResponseEntity.ok().build();
      } else {
        return ResponseEntity.badRequest().build();
      }
    } catch (UnknownUserException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Resetta la password dell'utente a una temporanea.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @param request i dati contenenti la password attuale
   * @return la password temporanea generata
   */
  @Operation(summary = "Reset password", description = "Resetta la password dell'utente corrente "
      + "a una temporanea")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Password resettata con successo"),
      @ApiResponse(responseCode = "400", description = "Password attuale errata"),
      @ApiResponse(responseCode = "401", description = "Token non valido")
  })
  @PostMapping("/reset-password")
  public ResponseEntity<String> resetPassword(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody ChangePasswordRequestDto request) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      String tempPassword = userService.resetPassword(token, request.currentPassword());
      return ResponseEntity.ok(tempPassword);
    } catch (SecurityException e) {
      return ResponseEntity.badRequest().build();
    } catch (UnknownUserException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // ============= ENDPOINTS GESTIONE RUOLI =============

  /**
   * Assegna un ruolo a un utente specifico.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @param id l'ID dell'utente
   * @param roleRequest i dati del ruolo da assegnare
   * @return una risposta vuota o un codice di errore
   */
  @Operation(summary = "Assegna ruolo a utente", description = "Assegna un ruolo specifico a un "
      + "utente")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Ruolo assegnato con successo"),
      @ApiResponse(responseCode = "400", description = "Dati non validi"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
      @ApiResponse(responseCode = "404", description = "Utente non trovato")
  })
  @PostMapping("/{id}/roles")
  public ResponseEntity<Void> assignRole(
      @RequestHeader("Authorization") String authHeader,
      @PathVariable String id,
      @Valid @RequestBody AssignRoleDto roleRequest) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      roleService.checkRole(token, RoleType.ADMIN);

      boolean success = roleService.assignRole(id, roleRequest.roleId());
      if (success) {
        return ResponseEntity.ok().build();
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Aggiorna il ruolo di un utente specifico.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @param id l'ID dell'utente
   * @param roleRequest i dati del nuovo ruolo
   * @return una risposta vuota o un codice di errore
   */
  @Operation(summary = "Aggiorna ruolo utente", description = "Aggiorna il ruolo di un utente")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Ruolo aggiornato con successo"),
      @ApiResponse(responseCode = "400", description = "Dati non validi"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
      @ApiResponse(responseCode = "404", description = "Utente non trovato")
  })
  @PutMapping("/{id}/roles")
  public ResponseEntity<Void> updateUserRole(
      @RequestHeader("Authorization") String authHeader,
      @PathVariable String id,
      @Valid @RequestBody AssignRoleDto roleRequest) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      roleService.checkRole(token, RoleType.ADMIN);

      boolean success = roleService.assignRole(id, roleRequest.roleId());
      if (success) {
        return ResponseEntity.ok().build();
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

}
