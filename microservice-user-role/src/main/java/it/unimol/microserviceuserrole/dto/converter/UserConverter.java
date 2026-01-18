package it.unimol.microserviceuserrole.dto.converter;

import it.unimol.microserviceuserrole.dto.user.CreateUserDto;
import it.unimol.microserviceuserrole.dto.user.UpdateUserProfileDto;
import it.unimol.microserviceuserrole.dto.user.UserDto;
import it.unimol.microserviceuserrole.dto.user.UserProfileDto;
import it.unimol.microserviceuserrole.model.Role;
import it.unimol.microserviceuserrole.model.User;
import it.unimol.microserviceuserrole.util.PasswordUtils;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Convertitore per trasformare entità User in DTO e viceversa.
 * Gestisce la conversione tra il modello di dominio utente e i vari DTO disponibili.
 */
@Component
public class UserConverter {

  @Autowired
  private RoleConverter roleConverter;

  /**
   * Converte un CreateUserDto in un'entità User.
   *
   * @param dto il DTO con i dati del nuovo utente
   * @param role il ruolo da assegnare all'utente
   * @return l'entità User creata
   */
  public User toEntity(CreateUserDto dto, Role role) {
    if (dto == null) {
      return null;
    }

    String userId = UUID.randomUUID().toString();
    String hashedPassword = PasswordUtils.hashPassword(dto.password());

    return new User(
        userId,
        dto.username(),
        dto.email(),
        dto.name(),
        dto.surname(),
        hashedPassword,
        role
    );
  }

  /**
   * Converte un'entità User in un UserDto completo.
   *
   * @param user l'entità User da convertire
   * @return il UserDto con tutti i dati dell'utente
   */
  public UserDto toDto(User user) {
    if (user == null) {
      return null;
    }

    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getName(),
        user.getSurname(),
        user.getCreatedAt(),
        user.getLastLogin(),
        roleConverter.toDto(user.getRole())
    );
  }

  /**
   * Converte un'entità User in un UserProfileDto pubblico.
   *
   * @param user l'entità User da convertire
   * @return il UserProfileDto con i dati pubblici dell'utente
   */
  public UserProfileDto toProfileDto(User user) {
    if (user == null) {
      return null;
    }

    return new UserProfileDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getName(),
        user.getSurname(),
        user.getRoleName(),
        user.getCreatedAt(),
        user.getLastLogin()
    );
  }

  /**
   * Aggiorna un'entità User con i dati di un UpdateUserProfileDto.
   *
   * @param user l'entità User da aggiornare
   * @param dto il DTO contenente i nuovi dati
   */
  public void updateEntity(User user, UpdateUserProfileDto dto) {
    if (user == null || dto == null) {
      return;
    }

    if (dto.username() != null && !dto.username().isBlank()) {
      user.setUsername(dto.username());
    }
    if (dto.email() != null && !dto.email().isBlank()) {
      user.setEmail(dto.email());
    }
    if (dto.name() != null && !dto.name().isBlank()) {
      user.setName(dto.name());
    }
    if (dto.surname() != null && !dto.surname().isBlank()) {
      user.setSurname(dto.surname());
    }
  }

  /**
   * Aggiorna selettivamente un'entità User solo con i campi non nulli.
   *
   * @param user l'entità User da aggiornare
   * @param dto il DTO contenente i nuovi dati
   */
  public void updateEntitySelective(User user, UpdateUserProfileDto dto) {
    updateEntity(user, dto);
  }
}