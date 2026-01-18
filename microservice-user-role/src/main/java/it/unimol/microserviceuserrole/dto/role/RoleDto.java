package it.unimol.microserviceuserrole.dto.role;

/**
 * DTO che rappresenta un ruolo nel sistema.
 *
 * @param id l'identificativo univoco del ruolo
 * @param name il nome del ruolo
 * @param description la descrizione del ruolo
 */
public record RoleDto(
    String id,
    String name,
    String description
) {

}