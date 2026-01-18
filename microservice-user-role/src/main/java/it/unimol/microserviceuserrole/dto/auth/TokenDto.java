package it.unimol.microserviceuserrole.dto.auth;

/**
 * DTO per rappresentare un token JWT.
 *
 * @param token il token JWT generato
 * @param type il tipo di token (es. "Bearer")
 * @param expiresIn il tempo di scadenza in millisecondi
 */
public record TokenDto(
    String token,
    String type,
    long expiresIn
) {

  /**
   * Costruttore con solo il token.
   *
   * @param token il token JWT
   */
  public TokenDto(String token) {
    this(token, "Bearer", 0L);
  }

  /**
   * Costruttore con token e scadenza.
   *
   * @param token il token JWT
   * @param expiresIn il tempo di scadenza in millisecondi
   */
  public TokenDto(String token, long expiresIn) {
    this(token, "Bearer", expiresIn);
  }
}