package it.unimol.microserviceuserrole.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumerazione dei tipi di ruolo utente disponibili nel sistema.
 * Ogni ruolo ha un livello di autorizzazione crescente: STUDENT (0), TEACHER (1),
 * ADMIN (2), SUPER_ADMIN (3).
 */
@Schema(description = "Tipo di Utente esistente (e.g., STUDENT, TEACHER, ADMIN, SUPER_ADMIN)")
public enum RoleType {
  STUDENT("STUDENT", "STUDENT", 0),
  TEACHER("TEACHER", "TEACHER", 1),
  ADMIN("ADMIN", "ADMIN", 2),
  SUPER_ADMIN("SUPER_ADMIN", "SUPER_ADMIN", 3);

  public static final String ROLE_STUDENT = "STUDENT";
  public static final String ROLE_TEACHER = "TEACHER";
  public static final String ROLE_ADMIN = "ADMIN";
  public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
  private final String roleId;
  private final String roleName;
  private final int level;

  RoleType(String roleId, String roleName, int level) {
    this.roleId = roleId;
    this.roleName = roleName;
    this.level = level;
  }

  /**
   * Trova un RoleType a partire dall'ID del ruolo.
   *
   * @param roleId l'ID del ruolo da cercare
   * @return il RoleType corrispondente
   * @throws IllegalArgumentException se il ruolo non viene trovato
   */
  public static RoleType fromRoleId(String roleId) {
    for (RoleType role : RoleType.values()) {
      if (role.roleId.equalsIgnoreCase(roleId)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Ruolo con ID '" + roleId + "' non trovato.");
  }

  /**
   * Trova un RoleType a partire dal nome del ruolo.
   *
   * @param roleName il nome del ruolo da cercare
   * @return il RoleType corrispondente
   * @throws IllegalArgumentException se il ruolo non viene trovato
   */
  public static RoleType fromRoleName(String roleName) {
    for (RoleType role : RoleType.values()) {
      if (role.roleName.equalsIgnoreCase(roleName)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Ruolo con nome '" + roleName + "' non trovato.");
  }

  /**
   * Restituisce l'ID del ruolo.
   *
   * @return l'ID del ruolo
   */
  public String getRoleId() {
    return roleId;
  }

  /**
   * Restituisce il nome del ruolo.
   *
   * @return il nome del ruolo
   */
  public String getRoleName() {
    return roleName;
  }

  /**
   * Restituisce il livello di autorizzazione del ruolo.
   *
   * @return il livello numerico (0-3)
   */
  public int getLevel() {
    return level;
  }

  /**
   * Verifica se questo ruolo ha almeno il livello richiesto.
   *
   * @param requiredRole il ruolo richiesto da confrontare
   * @return true se il livello di questo ruolo è maggiore o uguale a quello richiesto
   */
  public boolean hasMinimumLevel(RoleType requiredRole) {
    return this.level >= requiredRole.level;
  }
}