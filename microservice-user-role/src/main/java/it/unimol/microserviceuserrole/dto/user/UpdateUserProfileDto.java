package it.unimol.microserviceuserrole.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO per l'aggiornamento del profilo utente.
 * Tutti i campi sono opzionali; solo i campi forniti verranno aggiornati.
 *
 * @param username il nuovo nome utente (3-50 caratteri)
 * @param email il nuovo indirizzo email
 * @param name il nuovo nome (max 100 caratteri)
 * @param surname il nuovo cognome (max 100 caratteri)
 */
public record UpdateUserProfileDto(
    @Size(min = 3, max = 50, message = "Username deve essere tra 3 e 50 caratteri")
    String username,

    @Email(message = "Email deve essere valida")
    String email,

    @Size(max = 100, message = "Nome non può superare 100 caratteri")
    String name,

    @Size(max = 100, message = "Cognome non può superare 100 caratteri")
    String surname
) {

}