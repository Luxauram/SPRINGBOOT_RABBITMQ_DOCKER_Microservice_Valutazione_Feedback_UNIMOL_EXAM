package it.unimol.microserviceuserrole.service;

import it.unimol.microserviceuserrole.dto.auth.TokenDto;
import it.unimol.microserviceuserrole.dto.converter.UserConverter;
import it.unimol.microserviceuserrole.dto.user.UserDto;
import it.unimol.microserviceuserrole.exceptions.AuthException;
import it.unimol.microserviceuserrole.exceptions.UnknownUserException;
import it.unimol.microserviceuserrole.model.User;
import it.unimol.microserviceuserrole.repository.UserRepository;
import it.unimol.microserviceuserrole.util.PasswordUtils;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servizio per la gestione dell'autenticazione degli utenti.
 * Fornisce funzionalità di registrazione, login, logout e refresh del token JWT.
 */
@Service
public class AuthService {

  @Autowired
  private TokenJwtService tokenService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserConverter userConverter;
  @Autowired
  private MessageService messageService;

  /**
   * Registra un nuovo utente nel sistema.
   *
   * @param user L'utente da registrare.
   * @throws AuthException Se l'utente esiste già o se viene tentato di registrare un super admin.
   */
  public void register(User user) throws AuthException {
    try {
      if (user.getRole().getId().equals("sadmin")) {
        throw new AuthException("Ehh, volevi!");
      }

      String password = PasswordUtils.hashPassword(user.getPassword());
      user.setPassword(password);
      userRepository.save(user);

      UserDto userDto = userConverter.toDto(user);
      messageService.publishUserCreated(userDto);
    } catch (Exception e) {
      throw new AuthException(e.getMessage());
    }
  }

  /**
   * Effettua il login di un utente nel sistema.
   *
   * @param username L'username dell'utente.
   * @param password La password dell'utente.
   * @return Un oggetto TokenJWTDto contenente il token JWT generato.
   * @throws AuthException        Se l'autenticazione fallisce a causa di credenziali non valide.
   * @throws UnknownUserException Se l'utente non esiste nel sistema.
   */
  public TokenDto login(String username, String password)
      throws AuthException, UnknownUserException {
    Optional<User> existsUser = userRepository.findByUsername(username);
    if (existsUser.isPresent()) {
      User user = existsUser.get();
      if (PasswordUtils.verificaPassword(user.getPassword(), password)) {
        user.setLastLogin(LocalDateTime.now(ZoneId.systemDefault()));

        userRepository.save(user);
        return tokenService.generateToken(user.getId(), user.getUsername(), user.getRole().getId());
      }
    }
    throw new AuthException("Username o password non valida");
  }

  /**
   * Effettua il logout dell'utente invalidando il token.
   *
   * @param token Il token JWT da invalidare.
   */
  public void logout(String token) {
    tokenService.invalidateToken(token);
  }

  /**
   * Rinnova il token JWT dell'utente.
   *
   * @param token Il token JWT da rinnovare.
   * @return Un oggetto TokenJWTDto contenente il nuovo token JWT.
   * @throws RuntimeException Se il rinnovo del token fallisce.
   */
  public TokenDto refreshToken(String token) throws RuntimeException {
    return tokenService.refreshToken(token);
  }

  /**
   * Aggiorna l'ultimo accesso dell'utente.
   *
   * @param userId L'ID dell'utente di cui aggiornare l'ultimo accesso.
   */
  public void updateLastLogin(String userId) {
    userRepository.findById(userId).ifPresent(user -> {
      user.updateLastLogin();
      userRepository.save(user);
    });
  }
}