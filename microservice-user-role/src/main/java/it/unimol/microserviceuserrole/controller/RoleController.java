package it.unimol.microserviceuserrole.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unimol.microserviceuserrole.dto.role.AssignRoleDto;
import it.unimol.microserviceuserrole.dto.role.RoleDto;
import it.unimol.microserviceuserrole.enums.RoleType;
import it.unimol.microserviceuserrole.exceptions.InvalidRequestException;
import it.unimol.microserviceuserrole.exceptions.UnknownUserException;
import it.unimol.microserviceuserrole.service.RoleService;
import it.unimol.microserviceuserrole.service.TokenJwtService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per la gestione dei ruoli utente.
 * Fornisce endpoint per ottenere, assegnare e rimuovere ruoli.
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "API per la gestione dei ruoli")
public class RoleController {

  @Autowired
  private RoleService roleService;

  @Autowired
  private TokenJwtService tokenService;

  /**
   * Ottiene la lista di tutti i ruoli disponibili.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   * @return la lista dei ruoli o un messaggio di errore
   */
  @Operation(
      summary = "Ottieni tutti i ruoli",
      description = "Restituisce la lista di tutti i ruoli disponibili. Richiede privilegi di "
          + "amministratore."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista ruoli ottenuta con successo",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              RoleDto.class))),
      @ApiResponse(responseCode = "401", description = "Token non valido o scaduto"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti - richiesto ruolo"
          + " ADMIN"),
      @ApiResponse(responseCode = "500", description = "Errore interno del server")
  })
  @GetMapping
  public ResponseEntity<?> getAllRoles(@RequestHeader("Authorization") String authHeader) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      roleService.checkRole(token, RoleType.ADMIN);

      List<RoleDto> roles = roleService.getAllRoles();
      return ResponseEntity.ok(roles);

    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "Accesso negato", "message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Errore interno del server", "message", e.getMessage()));
    }
  }

  /**
   * Ottiene i dettagli di un ruolo specifico.
   *
   * @param roleId l'ID del ruolo da cercare
   * @param authHeader l'header Authorization contenente il token JWT
   * @return i dettagli del ruolo o un messaggio di errore
   */
  @Operation(
      summary = "Ottieni ruolo per ID",
      description = "Restituisce i dettagli di un ruolo specifico. Richiede privilegi di "
          + "amministratore."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Ruolo trovato",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              RoleDto.class))),
      @ApiResponse(responseCode = "401", description = "Token non valido o scaduto"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
      @ApiResponse(responseCode = "404", description = "Ruolo non trovato"),
      @ApiResponse(responseCode = "500", description = "Errore interno del server")
  })
  @GetMapping("/{roleId}")
  public ResponseEntity<?> getRoleById(
      @PathVariable String roleId,
      @RequestHeader("Authorization") String authHeader) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      roleService.checkRole(token, RoleType.ADMIN);

      RoleDto role = roleService.findById(roleId);
      if (role == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Ruolo non trovato", "roleId", roleId));
      }

      return ResponseEntity.ok(role);

    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "Accesso negato", "message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Errore interno del server", "message", e.getMessage()));
    }
  }

  /**
   * Assegna un ruolo a un utente specifico.
   *
   * @param userId l'ID dell'utente a cui assegnare il ruolo
   * @param assignRoleDto i dati contenenti l'ID del ruolo da assegnare
   * @param authHeader l'header Authorization contenente il token JWT
   * @return un messaggio di conferma o di errore
   */
  @Operation(
      summary = "Assegna ruolo a utente",
      description = "Assegna un ruolo specifico a un utente. Richiede privilegi di super "
          + "amministratore."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Ruolo assegnato con successo"),
      @ApiResponse(responseCode = "400", description = "Richiesta non valida"),
      @ApiResponse(responseCode = "401", description = "Token non valido o scaduto"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti - richiesto "
          + "SUPER_ADMIN"),
      @ApiResponse(responseCode = "404", description = "Utente o ruolo non trovato"),
      @ApiResponse(responseCode = "409", description = "L'utente ha già questo ruolo"),
      @ApiResponse(responseCode = "500", description = "Errore interno del server")
  })
  @PostMapping("/assign/{userId}")
  public ResponseEntity<?> assignRole(
      @PathVariable String userId,
      @RequestBody AssignRoleDto assignRoleDto,
      @RequestHeader("Authorization") String authHeader) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      roleService.checkRole(token, RoleType.SUPER_ADMIN);

      boolean assigned = roleService.assignRole(userId, assignRoleDto.roleId());

      if (assigned) {
        return ResponseEntity.ok(Map.of(
            "message", "Ruolo assegnato con successo",
            "userId", userId,
            "roleId", assignRoleDto.roleId()
        ));
      } else {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("message", "L'utente ha già questo ruolo"));
      }

    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "Accesso negato", "message", e.getMessage()));
    } catch (UnknownUserException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Utente non trovato", "userId", userId));
    } catch (InvalidRequestException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Ruolo non trovato", "roleId", assignRoleDto.roleId()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Errore interno del server", "message", e.getMessage()));
    }
  }

  /**
   * Rimuove il ruolo assegnato a un utente.
   *
   * @param userId l'ID dell'utente da cui rimuovere il ruolo
   * @param authHeader l'header Authorization contenente il token JWT
   * @return un messaggio di conferma o di errore
   */
  @Operation(
      summary = "Rimuovi ruolo da utente",
      description = "Rimuove il ruolo assegnato a un utente. Richiede privilegi di super "
          + "amministratore."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Ruolo rimosso con successo"),
      @ApiResponse(responseCode = "401", description = "Token non valido o scaduto"),
      @ApiResponse(responseCode = "403", description = "Privilegi insufficienti - richiesto "
          + "SUPER_ADMIN"),
      @ApiResponse(responseCode = "404", description = "Utente non trovato"),
      @ApiResponse(responseCode = "409", description = "L'utente non ha un ruolo assegnato"),
      @ApiResponse(responseCode = "500", description = "Errore interno del server")
  })
  @DeleteMapping("/remove/{userId}")
  public ResponseEntity<?> removeRole(
      @PathVariable String userId,
      @RequestHeader("Authorization") String authHeader) {
    try {
      String token = tokenService.extractTokenFromHeader(authHeader);
      roleService.checkRole(token, RoleType.SUPER_ADMIN);

      boolean removed = roleService.removeRole(userId);

      if (removed) {
        return ResponseEntity.ok(Map.of(
            "message", "Ruolo rimosso con successo",
            "userId", userId
        ));
      } else {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("message", "L'utente non ha un ruolo assegnato"));
      }

    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "Accesso negato", "message", e.getMessage()));
    } catch (UnknownUserException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Utente non trovato", "userId", userId));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Errore interno del server", "message", e.getMessage()));
    }
  }

}
