package it.unimol.microserviceassessmentfeedback.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumerazione dei tipi di ruolo utente nel sistema.
 * Definisce i ruoli disponibili: STUDENT, TEACHER, ADMIN, SUPER_ADMIN con relativi livelli.
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

  // ============ Costruttore ============

  /**
   * Costruttore.
   */
  RoleType(String roleId, String roleName, int level) {
    this.roleId = roleId;
    this.roleName = roleName;
    this.level = level;
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============
  public String getRoleId() {
    return roleId;
  }

  public String getRoleName() {
    return roleName;
  }

  public int getLevel() {
    return level;
  }

  public boolean hasMinimumLevel(RoleType requiredRole) {
    return this.level >= requiredRole.level;
  }

  // ============ Metodi di Classe ============
  /**
   * Trova un RoleType dato il suo ID.
   *
   * @param roleId L'ID del ruolo da cercare
   * @return Il RoleType corrispondente all'ID fornito
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
   * Trova un RoleType dato il suo nome.
   *
   * @param roleName Il nome del ruolo da cercare
   * @return Il RoleType corrispondente al nome fornito
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
}