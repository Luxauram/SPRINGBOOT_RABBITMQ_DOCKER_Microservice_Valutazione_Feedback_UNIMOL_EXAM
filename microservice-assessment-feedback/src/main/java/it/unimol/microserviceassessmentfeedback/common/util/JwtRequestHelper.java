package it.unimol.microserviceassessmentfeedback.common.util;

import it.unimol.microserviceassessmentfeedback.enums.RoleType;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper per l'estrazione di informazioni JWT dalle richieste HTTP. Fornisce metodi di utilità per
 * recuperare username, userId, ruoli e verificare permessi.
 */
@Component
public class JwtRequestHelper {

  private static final Logger logger = LoggerFactory.getLogger(JwtRequestHelper.class);

  @Autowired
  private JwtValidationService jwtValidationService;

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  /**
   * Estrae username dalla richiesta HTTP.
   *
   * @param request La richiesta HTTP contenente gli attributi utente
   * @return Username dell'utente autenticato
   * @throws IllegalStateException se l'username non è presente nella richiesta
   */
  public String getUsernameFromRequest(HttpServletRequest request) {
    String username = (String) request.getAttribute("username");
    if (username == null) {
      throw new IllegalStateException(
          "Username non trovato nella request - verificare configurazione JWT interceptor");
    }
    return username;
  }

  /**
   * Estrae l'ID utente dalla richiesta HTTP.
   *
   * @param request La richiesta HTTP contenente gli attributi utente
   * @return L'ID dell'utente autenticato
   * @throws IllegalStateException se l'userId non è presente nella richiesta
   */
  public String getUserIdFromRequest(HttpServletRequest request) {
    String userId = (String) request.getAttribute("userId");
    if (userId == null) {
      throw new IllegalStateException(
          "UserId non trovato nella request - verificare configurazione JWT interceptor");
    }
    return userId;
  }

  /**
   * Estrae il ruolo utente dalla richiesta HTTP.
   *
   * @param request La richiesta HTTP contenente gli attributi utente
   * @return Il ruolo dell'utente autenticato
   * @throws IllegalStateException se il ruolo non è presente nella richiesta
   */
  public String getUserRoleFromRequest(HttpServletRequest request) {
    String userRole = (String) request.getAttribute("userRole");
    if (userRole == null) {
      throw new IllegalStateException(
          "UserRole non trovato nella request - verificare configurazione JWT interceptor");
    }
    return userRole;
  }

  /**
   * Verifica se l'utente corrente ha il ruolo specificato.
   *
   * @param request La richiesta HTTP contenente gli attributi utente
   * @param role    Il ruolo da verificare
   * @return true se l'utente ha il ruolo specificato, false altrimenti
   */
  public boolean hasRole(HttpServletRequest request, String role) {
    try {
      String userRole = getUserRoleFromRequest(request);
      return role != null && role.equals(userRole);
    } catch (Exception e) {
      logger.warn("Impossibile verificare il ruolo utente", e);
      return false;
    }
  }

  /**
   * Verifica se l'utente corrente è uno studente.
   *
   * @param request La richiesta HTTP contenente gli attributi utente
   * @return true se l'utente è uno studente, false altrimenti
   */
  public boolean isStudent(HttpServletRequest request) {
    return hasRole(request, RoleType.ROLE_STUDENT);
  }

  /**
   * Verifica se l'utente corrente è un docente.
   *
   * @param request La richiesta HTTP contenente gli attributi utente
   * @return true se l'utente è un docente, false altrimenti
   */
  public boolean isTeacher(HttpServletRequest request) {
    return hasRole(request, RoleType.ROLE_TEACHER);
  }

  /**
   * Verifica se l'utente corrente è un admin.
   *
   * @param request La richiesta HTTP contenente gli attributi utente
   * @return true se l'utente è un admin, false altrimenti
   */
  public boolean isAdmin(HttpServletRequest request) {
    return hasRole(request, RoleType.ROLE_ADMIN);
  }

  /**
   * Verifica se l'utente corrente è un super admin.
   *
   * @param request La richiesta HTTP contenente gli attributi utente
   * @return true se l'utente è un super admin, false altrimenti
   */
  public boolean isSuperAdmin(HttpServletRequest request) {
    return hasRole(request, RoleType.ROLE_SUPER_ADMIN);
  }

  // ============ Metodi di Classe ============

  /**
   * Estrae l'ID dello studente dalla richiesta HTTP. Utilizza diversi metodi di fallback per
   * recuperare l'informazione.
   *
   * @param request La richiesta HTTP contenente gli attributi o header JWT
   * @return L'ID dello studente
   * @throws IllegalStateException se l'ID dello studente non può essere estratto
   */
  public String extractStudentIdFromRequest(HttpServletRequest request) {
    try {
      Object studentIdAttr = request.getAttribute("studentId");
      if (studentIdAttr != null) {
        return studentIdAttr.toString();
      }

      String userRole = getUserRoleFromRequest(request);
      if (RoleType.ROLE_STUDENT.equals(userRole)) {
        String userId = getUserIdFromRequest(request);
        if (userId != null) {
          return userId;
        }
      }

      String authHeader = request.getHeader("Authorization");
      if (authHeader != null) {
        String token = jwtValidationService.extractTokenFromHeader(authHeader);
        return jwtValidationService.extractStudentId(token);
      }

      throw new IllegalStateException("StudentId non trovato nella request");

    } catch (Exception e) {
      logger.error("Errore nell'estrazione dello studentId dalla request", e);
      throw new IllegalStateException("Errore nell'estrazione dello studentId dalla request", e);
    }
  }

  /**
   * Estrae l'ID del docente dalla richiesta HTTP. Utilizza diversi metodi di fallback per
   * recuperare l'informazione.
   *
   * @param request La richiesta HTTP contenente gli attributi o header JWT
   * @return L'ID del docente
   * @throws IllegalStateException se l'ID del docente non può essere estratto
   */
  public String extractTeacherIdFromRequest(HttpServletRequest request) {
    try {
      Object teacherIdAttr = request.getAttribute("teacherId");
      if (teacherIdAttr != null) {
        return teacherIdAttr.toString();
      }

      String userRole = getUserRoleFromRequest(request);
      if (RoleType.ROLE_TEACHER.equals(userRole)) {
        String userId = getUserIdFromRequest(request);
        if (userId != null) {
          return userId;
        }
      }

      String authHeader = request.getHeader("Authorization");
      if (authHeader != null) {
        String token = jwtValidationService.extractTokenFromHeader(authHeader);
        return jwtValidationService.extractTeacherId(token);
      }

      throw new IllegalStateException("TeacherId non trovato nella request");

    } catch (Exception e) {
      logger.error("Errore nell'estrazione del teacherId dalla request", e);
      throw new IllegalStateException("Errore nell'estrazione del teacherId dalla request", e);
    }
  }
}