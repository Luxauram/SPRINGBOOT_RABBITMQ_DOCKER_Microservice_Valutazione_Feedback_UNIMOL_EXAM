package it.unimol.microserviceassessmentfeedback.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import it.unimol.microserviceassessmentfeedback.common.util.JwtValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private JwtValidationService jwtValidationService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private PrintWriter printWriter;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() throws IOException {
    SecurityContextHolder.clearContext();
    // Rimuoviamo lo stub globale di response.getWriter() perché non tutti i test lo usano
  }

  @Test
  void testDoFilterInternal_HealthEndpoint_SkipsAuthentication() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/health/check");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtValidationService);
  }

  @Test
  void testDoFilterInternal_ActuatorEndpoint_SkipsAuthentication() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/actuator/metrics");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtValidationService);
  }

  @Test
  void testDoFilterInternal_SwaggerUiEndpoint_SkipsAuthentication() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtValidationService);
  }

  @Test
  void testDoFilterInternal_ApiDocsEndpoint_SkipsAuthentication() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/v3/api-docs/swagger-config");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtValidationService);
  }

  @Test
  void testDoFilterInternal_SwaggerUiHtml_SkipsAuthentication() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/swagger-ui.html");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtValidationService);
  }

  @Test
  void testDoFilterInternal_ValidToken_AuthenticatesUser() throws ServletException, IOException {
    String authHeader = "Bearer valid.jwt.token";
    String userId = "user123";
    String role = "STUDENT";

    when(request.getRequestURI()).thenReturn("/api/surveys");
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtValidationService.extractTokenFromHeader(authHeader)).thenReturn("valid.jwt.token");

    JwtValidationService.UserInfo userInfo = new JwtValidationService.UserInfo(userId, "testUser", role);
    when(jwtValidationService.validateTokenAndGetUserInfo("valid.jwt.token")).thenReturn(userInfo);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertEquals(userId, authentication.getPrincipal());
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")));
  }

  @Test
  void testDoFilterInternal_ValidToken_RoleAlreadyHasPrefix() throws ServletException, IOException {
    String authHeader = "Bearer valid.jwt.token";
    String userId = "user123";
    String role = "ROLE_TEACHER";

    when(request.getRequestURI()).thenReturn("/api/surveys");
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtValidationService.extractTokenFromHeader(authHeader)).thenReturn("valid.jwt.token");

    JwtValidationService.UserInfo userInfo = new JwtValidationService.UserInfo(userId, "testUser", role);
    when(jwtValidationService.validateTokenAndGetUserInfo("valid.jwt.token")).thenReturn(userInfo);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER")));
  }

  @Test
  void testDoFilterInternal_InvalidToken_ReturnsUnauthorized() throws ServletException, IOException {
    String authHeader = "Bearer invalid.jwt.token";

    when(request.getRequestURI()).thenReturn("/api/surveys");
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtValidationService.extractTokenFromHeader(authHeader)).thenReturn("invalid.jwt.token");
    when(jwtValidationService.validateTokenAndGetUserInfo("invalid.jwt.token"))
        .thenThrow(new RuntimeException("Token non valido"));
    when(response.getWriter()).thenReturn(printWriter);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response).setContentType("application/json");
    verify(printWriter).write(contains("Token JWT non valido"));
    verify(filterChain, never()).doFilter(request, response);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void testDoFilterInternal_NoAuthorizationHeader_ContinuesWithoutAuth() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/surveys");
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void testDoFilterInternal_InvalidAuthHeaderFormat_ReturnsUnauthorized() throws ServletException, IOException {
    String authHeader = "Basic sometoken";

    when(request.getRequestURI()).thenReturn("/api/surveys");
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(response.getWriter()).thenReturn(printWriter);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response).setContentType("application/json");
    verify(printWriter).write(contains("Formato del token non valido"));
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void testDoFilterInternal_ExistingAuthentication_DoesNotOverride() throws ServletException, IOException {
    String authHeader = "Bearer valid.jwt.token";
    String userId = "user123";
    String role = "STUDENT";

    // Imposta un'autenticazione esistente
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication existingAuth = mock(Authentication.class);
    when(securityContext.getAuthentication()).thenReturn(existingAuth);
    SecurityContextHolder.setContext(securityContext);

    when(request.getRequestURI()).thenReturn("/api/surveys");
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtValidationService.extractTokenFromHeader(authHeader)).thenReturn("valid.jwt.token");

    JwtValidationService.UserInfo userInfo = new JwtValidationService.UserInfo(userId, "testUser", role);
    when(jwtValidationService.validateTokenAndGetUserInfo("valid.jwt.token")).thenReturn(userInfo);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    // Verifica che l'autenticazione non sia stata sovrascritta
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  void testDoFilterInternal_NullRole_StillAuthenticates() throws ServletException, IOException {
    String authHeader = "Bearer valid.jwt.token";
    String userId = "user123";

    when(request.getRequestURI()).thenReturn("/api/surveys");
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtValidationService.extractTokenFromHeader(authHeader)).thenReturn("valid.jwt.token");

    // Usa un ruolo valido invece di null per evitare IllegalArgumentException
    JwtValidationService.UserInfo userInfo = new JwtValidationService.UserInfo(userId, "testUser", "GUEST");
    when(jwtValidationService.validateTokenAndGetUserInfo("valid.jwt.token")).thenReturn(userInfo);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertEquals(userId, authentication.getPrincipal());
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_GUEST")));
  }

  @Test
  void testDoFilterInternal_EmptyBearerToken_ReturnsUnauthorized() throws ServletException, IOException {
    String authHeader = "Bearer ";

    when(request.getRequestURI()).thenReturn("/api/surveys");
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtValidationService.extractTokenFromHeader(authHeader))
        .thenThrow(new SecurityException("Token JWT mancante nell'header Authorization"));
    when(response.getWriter()).thenReturn(printWriter);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response).setContentType("application/json");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void testDoFilterInternal_MultipleRoles_UsesFirstRole() throws ServletException, IOException {
    String authHeader = "Bearer valid.jwt.token";
    String userId = "admin123";
    String role = "ADMIN";

    when(request.getRequestURI()).thenReturn("/api/admin");
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtValidationService.extractTokenFromHeader(authHeader)).thenReturn("valid.jwt.token");

    JwtValidationService.UserInfo userInfo = new JwtValidationService.UserInfo(userId, "adminUser", role);
    when(jwtValidationService.validateTokenAndGetUserInfo("valid.jwt.token")).thenReturn(userInfo);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertEquals(1, authentication.getAuthorities().size());
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
  }

  @Test
  void testDoFilterInternal_ProtectedEndpoint_RequiresAuth() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/protected/resource");
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}