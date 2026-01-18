package it.unimol.microserviceuserrole.dto.converter;

import it.unimol.microserviceuserrole.dto.role.RoleDto;
import it.unimol.microserviceuserrole.dto.user.CreateUserDto;
import it.unimol.microserviceuserrole.model.User;
import it.unimol.microserviceuserrole.repository.UserRepository;
import it.unimol.microserviceuserrole.service.RoleService;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Convertitore Spring per la creazione di entità User da CreateUserDto.
 * Genera automaticamente un ID utente univoco e gestisce l'assegnazione del ruolo.
 */
@Component
public class UserCreationConverter implements Converter<CreateUserDto, User> {

  private static final Random random = new Random();
  @Autowired
  private RoleConverter roleConverter;
  @Autowired
  private RoleService roleService;
  @Autowired
  private UserRepository userRepository;

  /**
   * Converte un CreateUserDto in un'entità User.
   * Genera un ID univoco a 6 cifre e assegna il ruolo specificato.
   *
   * @param source il DTO con i dati del nuovo utente
   * @return l'entità User creata
   */
  @Override
  public User convert(@NonNull CreateUserDto source) {

    RoleDto ruolo = roleService.findById(source.roleId());
    String randomId;
    do {
      randomId = String.valueOf(100000 + random.nextInt(900000));
    } while (userRepository.findById(randomId).isPresent());

    return new User(randomId, source.username(), source.email(), source.name(), source.surname(),
        source.password(), roleConverter.toEntity(ruolo));
  }
}