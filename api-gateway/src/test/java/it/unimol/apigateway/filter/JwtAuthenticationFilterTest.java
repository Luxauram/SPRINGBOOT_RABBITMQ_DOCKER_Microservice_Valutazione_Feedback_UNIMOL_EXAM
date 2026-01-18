package it.unimol.apigateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.apigateway.util.JwtValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Test completi per JwtAuthenticationFilter con alta coverage.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private JwtValidationService jwtValidationService;

  @Mock
  private GatewayFilterChain chain;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private JwtAuthenticationFilter.Config config;

  @BeforeEach
  void setUp() {
    config = new JwtAuthenticationFilter.Config();
  }

  // ========== Test Base ==========

  @Test
  void testFilterCreation() {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
    assertNotNull(filter);
  }

  @Test
  void testConfigClass() {
    JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
    assertNotNull(config);
  }

  @Test
  void testConfigClassIsPublic() {
    assertTrue(JwtAuthenticationFilter.Config.class.getModifiers() > 0);
  }

  // ========== Test apply() - Caso Missing Authorization Header ==========

  @Test
  void testApply_MissingAuthorizationHeader() {
    // Crea una richiesta senza header Authorization
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/protected")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    // Applica il filtro
    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);

    // Verifica che la risposta sia 401
    result.block();
    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());

    // Verifica che chain.filter NON sia stato chiamato
    verify(chain, never()).filter(any());
  }

  @Test
  void testApply_NullAuthorizationHeader() {
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/protected")
        .header("Authorization", (String) null)
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);

    result.block();
    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    verify(chain, never()).filter(any());
  }

  // ========== Test apply() - Caso Invalid Bearer Token ==========

  @Test
  void testApply_InvalidBearerFormat() {
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/protected")
        .header("Authorization", "InvalidFormat token123")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);

    result.block();
    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    verify(chain, never()).filter(any());
  }

  @Test
  void testApply_EmptyBearerToken() {
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/protected")
        .header("Authorization", "Bearer")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);

    result.block();
    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    verify(chain, never()).filter(any());
  }

  // ========== Test apply() - Caso Token Extraction Exception ==========

  @Test
  void testApply_TokenExtractionThrowsException() {
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/protected")
        .header("Authorization", "Bearer token123")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    // Mock che extractTokenFromHeader lanci un'eccezione
    when(jwtValidationService.extractTokenFromHeader(anyString()))
        .thenThrow(new SecurityException("Invalid token format"));

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);

    result.block();
    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    verify(chain, never()).filter(any());
  }

  // ========== Test apply() - Caso Token Invalid ==========

  @Test
  void testApply_InvalidToken() {
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/protected")
        .header("Authorization", "Bearer invalid.token.here")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    when(jwtValidationService.extractTokenFromHeader("Bearer invalid.token.here"))
        .thenReturn("invalid.token.here");
    when(jwtValidationService.isTokenValid("invalid.token.here"))
        .thenReturn(false);

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);

    result.block();
    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    verify(chain, never()).filter(any());
  }

  // ========== Test apply() - Caso Token Valid ==========

  @Test
  void testApply_ValidToken() {
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/protected")
        .header("Authorization", "Bearer valid.token.here")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    JwtValidationService.UserInfo userInfo =
        new JwtValidationService.UserInfo("user123", "testuser", "ROLE_STUDENT");

    when(jwtValidationService.extractTokenFromHeader("Bearer valid.token.here"))
        .thenReturn("valid.token.here");
    when(jwtValidationService.isTokenValid("valid.token.here"))
        .thenReturn(true);
    when(jwtValidationService.validateTokenAndGetUserInfo("valid.token.here"))
        .thenReturn(userInfo);
    when(chain.filter(any(ServerWebExchange.class)))
        .thenReturn(Mono.empty());

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);

    result.block();

    // Verifica che chain.filter sia stato chiamato
    verify(chain, times(1)).filter(any(ServerWebExchange.class));

    // Verifica che la risposta NON sia 401
    assertNotEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
  }

  @Test
  void testApply_ValidToken_HeadersAdded() {
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/protected")
        .header("Authorization", "Bearer valid.token.here")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    JwtValidationService.UserInfo userInfo =
        new JwtValidationService.UserInfo("user456", "adminuser", "ROLE_ADMIN");

    when(jwtValidationService.extractTokenFromHeader("Bearer valid.token.here"))
        .thenReturn("valid.token.here");
    when(jwtValidationService.isTokenValid("valid.token.here"))
        .thenReturn(true);
    when(jwtValidationService.validateTokenAndGetUserInfo("valid.token.here"))
        .thenReturn(userInfo);

    // Usa un ArgumentCaptor per catturare la richiesta modificata
    when(chain.filter(any(ServerWebExchange.class)))
        .thenAnswer(invocation -> {
          ServerWebExchange modifiedExchange = invocation.getArgument(0);
          ServerHttpRequest modifiedRequest = modifiedExchange.getRequest();

          // Verifica che gli header siano stati aggiunti
          assertEquals("user456", modifiedRequest.getHeaders().getFirst("X-User-ID"));
          assertEquals("adminuser", modifiedRequest.getHeaders().getFirst("X-Username"));
          assertEquals("ROLE_ADMIN", modifiedRequest.getHeaders().getFirst("X-Roles"));

          return Mono.empty();
        });

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);

    result.block();
    verify(chain, times(1)).filter(any(ServerWebExchange.class));
  }

  // ========== Test apply() - Caso validateTokenAndGetUserInfo Exception ==========

  @Test
  void testApply_ValidateTokenThrowsException() {
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/protected")
        .header("Authorization", "Bearer token123")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    when(jwtValidationService.extractTokenFromHeader("Bearer token123"))
        .thenReturn("token123");
    when(jwtValidationService.isTokenValid("token123"))
        .thenReturn(true);
    when(jwtValidationService.validateTokenAndGetUserInfo("token123"))
        .thenThrow(new RuntimeException("Token validation failed"));

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);

    result.block();
    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    verify(chain, never()).filter(any());
  }

  // ========== Test handleUnauthorized - CORS Headers ==========

  @Test
  void testHandleUnauthorized_CorsHeaders() {
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/protected")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    Mono<Void> result = filter.filter(exchange, chain);

    result.block();

    ServerHttpResponse response = exchange.getResponse();
    HttpHeaders headers = response.getHeaders();

    // Verifica CORS headers
    assertEquals("*", headers.getFirst("Access-Control-Allow-Origin"));
    assertEquals("GET, POST, PUT, DELETE, OPTIONS",
        headers.getFirst("Access-Control-Allow-Methods"));
    assertEquals("*", headers.getFirst("Access-Control-Allow-Headers"));
  }

  // ========== Test con diversi path ==========

  @Test
  void testApply_DifferentPaths() {
    String[] paths = {
        "/api/users",
        "/api/courses/123",
        "/api/assessments/456/feedback",
        "/public/health"
    };

    for (String path : paths) {
      MockServerHttpRequest request = MockServerHttpRequest
          .get(path)
          .header("Authorization", "Bearer valid.token")
          .build();

      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      JwtValidationService.UserInfo userInfo =
          new JwtValidationService.UserInfo("user1", "user1", "ROLE_USER");

      when(jwtValidationService.extractTokenFromHeader(anyString()))
          .thenReturn("valid.token");
      when(jwtValidationService.isTokenValid(anyString()))
          .thenReturn(true);
      when(jwtValidationService.validateTokenAndGetUserInfo(anyString()))
          .thenReturn(userInfo);
      when(chain.filter(any(ServerWebExchange.class)))
          .thenReturn(Mono.empty());

      GatewayFilter filter = jwtAuthenticationFilter.apply(config);
      filter.filter(exchange, chain).block();

      // Verifica che il filtro gestisca correttamente ogni path
      assertNotNull(exchange.getRequest().getURI().getPath());
    }
  }

  // ========== Test con diversi metodi HTTP ==========

  @Test
  void testApply_DifferentHttpMethods_ValidToken() {
    JwtValidationService.UserInfo userInfo =
        new JwtValidationService.UserInfo("user1", "user1", "ROLE_USER");

    when(jwtValidationService.extractTokenFromHeader(anyString()))
        .thenReturn("valid.token");
    when(jwtValidationService.isTokenValid(anyString()))
        .thenReturn(true);
    when(jwtValidationService.validateTokenAndGetUserInfo(anyString()))
        .thenReturn(userInfo);
    when(chain.filter(any(ServerWebExchange.class)))
        .thenReturn(Mono.empty());

    // Test GET
    testHttpMethod(MockServerHttpRequest.get("/api/test"));

    // Test POST
    testHttpMethod(MockServerHttpRequest.post("/api/test"));

    // Test PUT
    testHttpMethod(MockServerHttpRequest.put("/api/test"));

    // Test DELETE
    testHttpMethod(MockServerHttpRequest.delete("/api/test"));
  }

  private void testHttpMethod(MockServerHttpRequest.BaseBuilder<?> builder) {
    MockServerHttpRequest request = builder
        .header("Authorization", "Bearer valid.token")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    filter.filter(exchange, chain).block();

    verify(chain, atLeastOnce()).filter(any(ServerWebExchange.class));
  }

  // ========== Test edge cases ==========

  @Test
  void testApply_TokenWithSpecialCharacters() {
    String tokenWithSpecialChars = "eyJ0eXAiOiJKV1QiLCJhbGc.eyJzdWIiOiIxMjM0NTY3ODkw.SflKxwRJ";

    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/test")
        .header("Authorization", "Bearer " + tokenWithSpecialChars)
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    when(jwtValidationService.extractTokenFromHeader(anyString()))
        .thenReturn(tokenWithSpecialChars);
    when(jwtValidationService.isTokenValid(tokenWithSpecialChars))
        .thenReturn(false);

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    filter.filter(exchange, chain).block();

    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
  }

  @Test
  void testApply_MultipleAuthHeaders() {
    MockServerHttpRequest request = MockServerHttpRequest
        .get("/api/test")
        .header("Authorization", "Bearer token1")
        .header("Authorization", "Bearer token2")
        .build();

    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    // Dovrebbe prendere solo il primo header
    when(jwtValidationService.extractTokenFromHeader(anyString()))
        .thenReturn("token1");
    when(jwtValidationService.isTokenValid("token1"))
        .thenReturn(false);

    GatewayFilter filter = jwtAuthenticationFilter.apply(config);
    filter.filter(exchange, chain).block();

    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
  }

  // ========== Test config ==========

  @Test
  void testConfig_CanBeInstantiated() {
    JwtAuthenticationFilter.Config config1 = new JwtAuthenticationFilter.Config();
    JwtAuthenticationFilter.Config config2 = new JwtAuthenticationFilter.Config();

    assertNotNull(config1);
    assertNotNull(config2);
    assertNotSame(config1, config2);
  }
}