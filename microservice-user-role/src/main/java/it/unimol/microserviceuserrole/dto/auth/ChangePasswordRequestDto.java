package it.unimol.microserviceuserrole.dto.auth;

/**
 * DTO per la richiesta di cambio password.
 *
 * @param currentPassword la password attuale dell'utente
 * @param newPassword la nuova password desiderata
 */
public record ChangePasswordRequestDto(
    String currentPassword,
    String newPassword
) {

}