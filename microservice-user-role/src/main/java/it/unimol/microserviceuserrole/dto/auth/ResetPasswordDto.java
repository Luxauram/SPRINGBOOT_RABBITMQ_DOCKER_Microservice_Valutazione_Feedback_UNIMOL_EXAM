package it.unimol.microserviceuserrole.dto.auth;

/**
 * DTO per la richiesta di reset password.
 *
 * @param email l'indirizzo email dell'utente
 */
public record ResetPasswordDto(
    String email
) {

}