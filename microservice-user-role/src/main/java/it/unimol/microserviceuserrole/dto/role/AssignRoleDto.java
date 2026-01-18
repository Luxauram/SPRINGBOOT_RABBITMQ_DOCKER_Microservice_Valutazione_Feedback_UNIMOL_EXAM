package it.unimol.microserviceuserrole.dto.role;

/**
 * DTO per l'assegnazione di un ruolo a un utente.
 *
 * @param roleId l'ID del ruolo da assegnare
 */
public record AssignRoleDto(
    String roleId
) {

}