package it.unimol.microserviceuserrole.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unimol.microserviceuserrole.dto.auth.LoginDto;
import it.unimol.microserviceuserrole.dto.auth.TokenDto;
import it.unimol.microserviceuserrole.exceptions.AuthException;
import it.unimol.microserviceuserrole.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per la gestione dell'autenticazione degli utenti.
 * Fornisce endpoint per login, logout e refresh del token JWT.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API per l'autenticazione degli utenti")
public class AuthController {

  @Autowired
  private AuthService authService;

  /**
   * Effettua il login di un utente.
   *
   * @param loginRequest i dati di login (username e password)
   * @return il token JWT se l'autenticazione ha successo
   */
  @Operation(
      summary = "Autenticazione utente",
      description = "Effettua il login di un utente e restituisce un token JWT per l'autenticazione"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Login effettuato con successo",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = TokenDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Credenziali non valide",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Richiesta non valida",
          content = @Content
      )
  })
  @PostMapping("/login")
  public ResponseEntity<TokenDto> login(@RequestBody LoginDto loginRequest) {
    try {
      TokenDto tokenDto = authService.login(loginRequest.username(), loginRequest.password());
      return ResponseEntity.ok(tokenDto);
    } catch (AuthException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (Exception e) {
      System.err.println(
          "Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Effettua il logout invalidando il token JWT.
   *
   * @param authHeader l'header Authorization contenente il token JWT
   */
  @Operation(
      summary = "Logout utente",
      description = "Invalida il token JWT dell'utente"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Logout effettuato con successo"),
      @ApiResponse(responseCode = "401", description = "Token non valido"),
      @ApiResponse(responseCode = "400", description = "Header Authorization mancante o malformato")
  })
  @PostMapping("/logout")
  public void logout(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    authService.logout(token);
  }

  /**
   * Rinnova il token JWT generandone uno nuovo.
   *
   * @param authHeader l'header Authorization contenente il token JWT da rinnovare
   * @return il nuovo token JWT
   */
  @Operation(
      summary = "Refresh token",
      description = "Genera un nuovo token JWT a partire da uno esistente"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Token rinnovato con successo",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              TokenDto.class))),
      @ApiResponse(responseCode = "401", description = "Token scaduto o non valido"),
      @ApiResponse(responseCode = "400", description = "Header Authorization mancante o malformato")
  })
  @PostMapping("/refresh-token")
  public ResponseEntity<TokenDto> refreshToken(@RequestHeader("Authorization") String authHeader) {
    try {
      String token = authHeader.replace("Bearer ", "");
      TokenDto newToken = authService.refreshToken(token);
      return ResponseEntity.ok(newToken);
    } catch (Exception e) {
      System.err.println(
          "Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }
}
