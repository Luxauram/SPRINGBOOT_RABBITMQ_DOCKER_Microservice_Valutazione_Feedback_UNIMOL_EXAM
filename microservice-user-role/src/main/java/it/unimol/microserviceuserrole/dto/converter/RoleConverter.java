package it.unimol.microserviceuserrole.dto.converter;

import it.unimol.microserviceuserrole.dto.role.RoleDto;
import it.unimol.microserviceuserrole.model.Role;
import org.springframework.stereotype.Component;

/**
 * Convertitore per trasformare entità Role in DTO e viceversa.
 * Gestisce la conversione bidirezionale tra il modello di dominio e i DTO.
 */
@Component
public class RoleConverter {

  /**
   * Converte un'entità Role in un RoleDto.
   *
   * @param role l'entità Role da convertire
   * @return il RoleDto corrispondente
   */
  public RoleDto toDto(Role role) {
    if (role == null) {
      return null;
    }

    return new RoleDto(
        role.getId(),
        role.getName(),
        role.getDescription()
    );
  }

  /**
   * Converte un RoleDto in un'entità Role.
   *
   * @param roleDto il RoleDto da convertire
   * @return l'entità Role corrispondente
   */
  public Role toEntity(RoleDto roleDto) {
    if (roleDto == null) {
      return null;
    }

    return new Role(
        roleDto.id(),
        roleDto.name(),
        roleDto.description()
    );
  }
}