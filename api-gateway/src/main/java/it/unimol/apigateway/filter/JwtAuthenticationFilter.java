package it.unimol.apigateway.filter;

import it.unimol.apigateway.util.JwtValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtro Gateway per l'autenticazione JWT.
 * Intercetta le richieste HTTP, valida i token JWT presenti negli header Authorization
 * e arricchisce le richieste con informazioni sull'utente autenticato.
 */
@Component
public class JwtAuthenticationFilter extends
    AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

  @Autowired
  private JwtValidationService jwtValidationService;

  /**
   * Costruttore del filtro JWT.
   * Inizializza il filtro con la configurazione di tipo Config.
   */
  public JwtAuthenticationFilter() {
    super(Config.class);
  }

  /**
   * Applica il filtro di autenticazione JWT alle richieste.
   * Valida il token JWT presente nell'header Authorization e arricchisce
   * la richiesta con informazioni sull'utente (ID, username, ruoli).
   *
   * @param config la configurazione del filtro
   * @return il GatewayFilter configurato
   */
  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();
      String path = request.getURI().getPath();

      System.out.println("=== JWT FILTER DEBUG ===");
      System.out.println("Request path: " + path);
      System.out.println("Request method: " + request.getMethod());
      System.out.println("========================");

      String authHeader = request.getHeaders().getFirst("Authorization");
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        System.out.println("Missing or invalid Authorization header for protected path: " + path);
        return handleUnauthorized(exchange);
      }

      try {
        String token = jwtValidationService.extractTokenFromHeader(authHeader);

        if (jwtValidationService.isTokenValid(token)) {
          JwtValidationService.UserInfo userInfo = jwtValidationService.validateTokenAndGetUserInfo(
              token);

          ServerHttpRequest modifiedRequest = request.mutate()
              .header("X-User-ID", userInfo.userId())
              .header("X-Username", userInfo.username())
              .header("X-Roles", userInfo.role())
              .build();

          System.out.println("JWT validation successful for user: " + userInfo.username());
          return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } else {
          System.out.println("Invalid JWT token for path: " + path);
          return handleUnauthorized(exchange);
        }
      } catch (Exception e) {
        System.err.println("JWT validation error for path " + path + ": " + e.getMessage());
        e.printStackTrace();
        return handleUnauthorized(exchange);
      }
    };
  }

  /**
   * Gestisce le richieste non autorizzate.
   * Imposta lo status HTTP 401 Unauthorized e i relativi header CORS.
   *
   * @param exchange lo scambio server web corrente
   * @return un Mono vuoto che completa la risposta
   */
  private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);

    response.getHeaders().add("Access-Control-Allow-Origin", "*");
    response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    response.getHeaders().add("Access-Control-Allow-Headers", "*");

    return response.setComplete();
  }

  /**
   * Classe di configurazione per il filtro JWT.
   * Attualmente non contiene parametri configurabili ma è necessaria
   * per l'estensione di AbstractGatewayFilterFactory.
   */
  public static class Config {

  }
}