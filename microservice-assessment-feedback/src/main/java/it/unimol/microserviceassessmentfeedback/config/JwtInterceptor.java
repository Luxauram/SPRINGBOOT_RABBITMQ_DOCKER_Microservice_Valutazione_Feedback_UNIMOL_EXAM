package it.unimol.microserviceassessmentfeedback.config;

import it.unimol.microserviceassessmentfeedback.common.util.JwtValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor per JWT.
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

  @Autowired
  private JwtValidationService jwtValidationService;

  // ============ Costruttore ============

  // ============ Metodi Override ============
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String path = request.getRequestURI();

    if (path.startsWith("/health")
        || path.startsWith("/actuator")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs")
        || path.equals("/swagger-ui.html")) {
      return true;
    }

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Token mancante o formato non valido\"}");
      return false;
    }

    try {
      String token = jwtValidationService.extractTokenFromHeader(authHeader);
      JwtValidationService.UserInfo userInfo = jwtValidationService.validateTokenAndGetUserInfo(
          token);

      request.setAttribute("userId", userInfo.userId());
      request.setAttribute("username", userInfo.username());
      request.setAttribute("userRole", userInfo.role());

      return true;
    } catch (SecurityException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
      return false;
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Token non valido: " + e.getMessage() + "\"}");
      return false;
    }
  }

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============


}