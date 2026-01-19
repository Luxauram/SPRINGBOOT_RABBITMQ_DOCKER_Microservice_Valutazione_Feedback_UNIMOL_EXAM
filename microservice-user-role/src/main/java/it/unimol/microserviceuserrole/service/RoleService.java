package it.unimol.microserviceuserrole.service;

import it.unimol.microserviceuserrole.dto.converter.RoleConverter;
import it.unimol.microserviceuserrole.dto.role.RoleDto;
import it.unimol.microserviceuserrole.enums.RoleType;
import it.unimol.microserviceuserrole.exceptions.InvalidRequestException;
import it.unimol.microserviceuserrole.exceptions.UnknownUserException;
import it.unimol.microserviceuserrole.model.Role;
import it.unimol.microserviceuserrole.model.User;
import it.unimol.microserviceuserrole.repository.RoleRepository;
import it.unimol.microserviceuserrole.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servizio per la gestione dei ruoli utente.
 * Fornisce funzionalità per la creazione, assegnazione e verifica dei ruoli.
 */
@Service
public class RoleService {

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RoleConverter roleConverter;

  @Autowired
  private TokenJwtService tokenService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MessageService messageService;

  /**
   * Restituisce tutti i ruoli presenti nel sistema.
   *
   * @return Una lista di RoleDto che rappresentano i ruoli.
   */
  public List<RoleDto> getAllRoles() {
    List<Role> roles = roleRepository.findAll();
    return roles.stream()
        .map(roleConverter::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Trova un role per ID nel database.
   *
   * @param roleId L'ID del role da cercare.
   * @return Un RoleDto se il role esiste, altrimenti null.
   */
  public RoleDto findById(String roleId) {
    Optional<Role> role = roleRepository.findById(roleId);
    return role.map(roleConverter::toDto).orElse(null);
  }

  /**
   * Trova un ruolo per nome nel database.
   *
   * @param roleName Il nome del ruolo da cercare.
   * @return Un RoleDTO se il ruolo esiste, altrimenti null.
   */
  public RoleDto findByName(String roleName) {
    Optional<Role> role = roleRepository.findByName(roleName);
    return role.map(roleConverter::toDto).orElse(null);
  }

  /**
   * Crea un ruolo se non esiste già nel database.
   *
   * @param id          L'ID del ruolo da creare.
   * @param name        Il nome del ruolo.
   * @param description La descrizione del ruolo.
   */
  private void createRoleIfNotExists(String id, String name, String description) {
    if (!roleRepository.existsById(id)) {
      Role role = new Role(id, name, description);
      roleRepository.save(role);
    }
  }

  /**
   * Inizializza i ruoli di base nel database. Questo metodo viene chiamato in automatico una volta
   * all'avvio dell'applicazione.
   */
  @PostConstruct
  public void initializeRoles() {
    for (RoleType roleType : RoleType.values()) {
      String description = switch (roleType) {
        case SUPER_ADMIN -> "Amministratore di sistema con tutti i privilegi";
        case ADMIN -> "Amministratore con privilegi di gestione utenti";
        case TEACHER -> "Ruolo con permessi aggiuntivi per i docenti";
        case STUDENT -> "Ruolo base, riservato agli studenti";
      };

      createRoleIfNotExists(roleType.getRoleId(), roleType.getRoleName(), description);
    }
  }

  /**
   * Controlla se l'utente ha il role richiesto per eseguire un'operazione.
   *
   * @param token        Il token JWT dell'utente.
   * @param requiredRole Il role richiesto per l'operazione.
   * @throws SecurityException Se l'utente non esiste o il token non è valido.
   */
  public void checkRole(String token, RoleType requiredRole) {
    if (!tokenService.isTokenValid(token)) {
      throw new SecurityException("Token non valido o scaduto");
    }

    String userRoleName = tokenService.extractRole(token);

    try {
      RoleType userRole = RoleType.fromRoleName(userRoleName);

      if (!userRole.hasMinimumLevel(requiredRole)) {
        throw new SecurityException("Permessi insufficienti per questa operazione. "
            + "Richiesto: " + requiredRole.getRoleName()
            + ", Posseduto: " + userRole.getRoleName());
      }
    } catch (IllegalArgumentException e) {
      throw new SecurityException("Ruolo utente non riconosciuto: " + userRoleName);
    }
  }

  /**
   * Assegna un role a un utente.
   *
   * @param userId L'ID dell'utente a cui assegnare il role.
   * @param roleId L'ID del role da assegnare.
   * @return true se il role è stato assegnato, false se l'utente ha già quel role.
   * @throws IllegalArgumentException Se l'utente o il role non esistono.
   */
  public boolean assignRole(String userId, String roleId)
      throws UnknownUserException, InvalidRequestException {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UnknownUserException("Utente con ID '" + userId + "' non trovato"));

    Role role = roleRepository.findById(roleId)
        .orElseThrow(
            () -> new InvalidRequestException("Ruolo con ID '" + roleId + "' non trovato"));

    if (user.getRole() != null && user.getRole().getId().equals(roleId)) {
      return false;
    }

    user.setRole(role);
    userRepository.save(user);

    messageService.publishRoleAssigned(userId, roleId);

    return true;
  }

  /**
   * Rimuove il ruolo da un utente (imposta il ruolo a null).
   *
   * @param userId L'ID dell'utente da cui rimuovere il ruolo.
   * @return true se il ruolo è stato rimosso, false se l'utente non aveva un ruolo.
   * @throws UnknownUserException Se l'utente non esiste.
   */
  public boolean removeRole(String userId) throws UnknownUserException {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UnknownUserException("Utente con ID '" + userId + "' non trovato"));

    if (user.getRole() == null) {
      return false;
    }

    user.setRole(null);
    userRepository.save(user);

    return true;
  }

  /**
   * Verifica se un ruolo esiste nel sistema.
   *
   * @param roleId L'ID del ruolo da verificare.
   * @return true se il ruolo esiste, false altrimenti.
   */
  public boolean roleExists(String roleId) {
    return roleRepository.existsById(roleId);
  }
}