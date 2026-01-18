package it.unimol.microserviceuserrole.dto.user;

import java.time.LocalDateTime;

/**
 * DTO pubblico che rappresenta il profilo di un utente.
 * Versione semplificata di UserDto con solo le informazioni essenziali.
 *
 * @param id l'identificativo univoco dell'utente
 * @param username il nome utente
 * @param email l'indirizzo email
 * @param name il nome dell'utente
 * @param surname il cognome dell'utente
 * @param roleName il nome del ruolo dell'utente
 * @param createdAt la data e ora di creazione dell'account
 * @param lastLogin la data e ora dell'ultimo accesso
 */
public record UserProfileDto(
    String id,
    String username,
    String email,
    String name,
    String surname,
    String roleName,
    LocalDateTime createdAt,
    LocalDateTime lastLogin
) {

}