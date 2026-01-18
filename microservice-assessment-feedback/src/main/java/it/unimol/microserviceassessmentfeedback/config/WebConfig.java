package it.unimol.microserviceassessmentfeedback.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configurazione Web MVC per l'applicazione. Registra gli interceptor HTTP, includendo la
 * validazione JWT su tutti gli endpoint ad eccezione di quelli pubblici (health check, Swagger,
 * Actuator).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired
  private JwtInterceptor jwtInterceptor;

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  /**
   * Registra gli interceptor HTTP nell'applicazione. Applica il JWT interceptor a tutti i path
   * tranne quelli esclusi esplicitamente.
   *
   * @param registry il registro degli interceptor
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/health",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html"
        );
  }
}