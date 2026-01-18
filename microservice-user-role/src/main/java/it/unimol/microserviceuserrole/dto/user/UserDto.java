package it.unimol.microserviceuserrole.dto.user;

import it.unimol.microserviceuserrole.dto.role.RoleDto;
import java.time.LocalDateTime;

/**
 * DTO completo che rappresenta un utente nel sistema.
 * Include tutte le informazioni dell'utente, compreso il ruolo completo.
 *
 * @param id l'identificativo univoco dell'utente
 * @param username il nome utente
 * @param email l'indirizzo email
 * @param name il nome dell'utente
 * @param surname il cognome dell'utente
 * @param createdAt la data e ora di creazione dell'account
 * @param lastLogin la data e ora dell'ultimo accesso
 * @param role il ruolo completo dell'utente
 */
public record UserDto(
    String id,
    String username,
    String email,
    String name,
    String surname,
    LocalDateTime createdAt,
    LocalDateTime lastLogin,
    RoleDto role
) {

  /**
   * Restituisce il nome del ruolo dell'utente.
   *
   * @return il nome del ruolo, o null se il ruolo non è definito
   */
  public String getRoleName() {
    return role != null ? role.name() : null;
  }
}