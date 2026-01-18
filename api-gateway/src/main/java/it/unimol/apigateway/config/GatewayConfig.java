package it.unimol.apigateway.config;

import it.unimol.apigateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configurazione delle route per l'API Gateway.
 * Definisce il routing delle richieste HTTP verso i microservizi appropriati,
 * distinguendo tra route pubbliche e protette tramite autenticazione JWT.
 */
@Configuration
public class GatewayConfig {

  @Autowired
  private Environment environment;

  @Autowired
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * Configura il route locator personalizzato per l'API Gateway.
   * Definisce tutte le route disponibili, sia pubbliche che protette,
   * e gestisce il routing verso i microservizi in base all'ambiente di esecuzione.
   *
   * @param builder il builder per la costruzione delle route
   * @return il route locator configurato
   */
  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    String userServiceUri;
    String assessmentServiceUri;

    if (isDockerProfile()) {
      userServiceUri = "http://unimol-microservice-user-role:8081";
      assessmentServiceUri = "http://unimol-microservice-assessment-feedback:8082";
    } else {
      userServiceUri = "http://localhost:8081";
      assessmentServiceUri = "http://localhost:8082";
    }

    return builder.routes()
        // ===============================
        // ROUTE PUBBLICHE (SENZA JWT)
        // ===============================

        // Auth endpoints (login, refresh, init)
        .route("auth-public", r -> r.path("/api/v1/auth/login", "/api/v1/auth/refresh-token",
                "/api/v1/users/superadmin/init")
            .filters(f -> f.stripPrefix(0))
            .uri(userServiceUri))

        // Health-Check endpoints
        .route("user-service-health", r -> r.path("/api/user-service/actuator/**")
            .filters(f -> f.rewritePath("/api/user-service/actuator/(?<segment>.*)",
                "/actuator/${segment}"))
            .uri(userServiceUri))

        .route("assessment-service-health", r -> r.path("/api/assessment-service/actuator/**")
            .filters(f -> f.rewritePath("/api/assessment-service/actuator/(?<segment>.*)",
                "/actuator/${segment}"))
            .uri(assessmentServiceUri))

        // Gateway actuator
        .route("gateway-actuator", r -> r.path("/actuator/**")
            .filters(f -> f.stripPrefix(0))
            .uri("http://localhost:8080"))

        // Swagger/OpenAPI - User Service
        .route("user-service-openapi", r -> r.path("/user-service/v3/api-docs/**")
            .filters(f -> f.rewritePath("/user-service/v3/api-docs(?<segment>.*)",
                "/v3/api-docs${segment}"))
            .uri(userServiceUri))

        .route("user-service-swagger-ui", r -> r.path("/user-service/swagger-ui/**")
            .filters(f -> f.rewritePath("/user-service/swagger-ui(?<segment>.*)",
                "/swagger-ui${segment}"))
            .uri(userServiceUri))

        // Swagger/OpenAPI - Assessment Service
        .route("assessment-service-openapi", r -> r.path("/assessment-service/v3/api-docs/**")
            .filters(f -> f.rewritePath("/assessment-service/v3/api-docs(?<segment>.*)",
                "/v3/api-docs${segment}"))
            .uri(assessmentServiceUri))

        .route("assessment-service-swagger-ui", r -> r.path("/assessment-service/swagger-ui/**")
            .filters(f -> f.rewritePath("/assessment-service/swagger-ui(?<segment>.*)",
                "/swagger-ui${segment}"))
            .uri(assessmentServiceUri))

        // ===============================
        // ROUTE PROTETTE (CON JWT)
        // ===============================

        // MS User Role
        .route("user-service-auth-protected", r -> r.path("/api/v1/auth/**")
            .and().not(p -> p.path("/api/v1/auth/login", "/api/v1/auth/refresh-token"))
            .filters(f -> f
                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                .stripPrefix(0))
            .uri(userServiceUri))

        .route("user-service-users", r -> r.path("/api/v1/users/**")
            .and().not(p -> p.path("/api/v1/users/superadmin/init"))
            .filters(f -> f
                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                .stripPrefix(0))
            .uri(userServiceUri))

        .route("user-service-roles", r -> r.path("/api/v1/roles/**")
            .filters(f -> f
                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                .stripPrefix(0))
            .uri(userServiceUri))

        // MS Assessment-Feedback
        .route("assessment-service-assessments", r -> r.path("/api/v1/assessments/**")
            .filters(f -> f
                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                .stripPrefix(0))
            .uri(assessmentServiceUri))

        .route("assessment-service-feedback", r -> r.path("/api/v1/feedback/**")
            .filters(f -> f
                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                .stripPrefix(0))
            .uri(assessmentServiceUri))

        .route("assessment-service-teacher-surveys", r -> r.path("/api/v1/teacher-surveys/**")
            .filters(f -> f
                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                .stripPrefix(0))
            .uri(assessmentServiceUri))

        .route("assessment-service-surveys", r -> r.path("/api/v1/surveys/**")
            .filters(f -> f
                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                .stripPrefix(0))
            .uri(assessmentServiceUri))

        .build();
  }

  /**
   * Verifica se il profilo Docker è attivo.
   *
   * @return true se il profilo "docker" è attivo, false altrimenti
   */
  private boolean isDockerProfile() {
    String[] activeProfiles = environment.getActiveProfiles();
    for (String profile : activeProfiles) {
      if ("docker".equals(profile)) {
        return true;
      }
    }
    return false;
  }
}