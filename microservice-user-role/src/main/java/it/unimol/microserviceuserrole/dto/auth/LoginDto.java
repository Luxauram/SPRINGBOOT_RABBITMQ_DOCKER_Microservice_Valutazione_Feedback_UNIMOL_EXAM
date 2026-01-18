package it.unimol.microserviceuserrole.dto.auth;

/**
 * DTO per la richiesta di login.
 *
 * @param username il nome utente
 * @param password la password
 */
public record LoginDto(
    String username,
    String password
) {

}