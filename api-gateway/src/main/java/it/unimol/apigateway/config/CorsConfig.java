package it.unimol.apigateway.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Configurazione CORS per l'API Gateway.
 * Gestisce le politiche di Cross-Origin Resource Sharing per permettere
 * le richieste da domini diversi in modo sicuro.
 */
@Configuration
public class CorsConfig {

  private final Environment environment;
  @Value("${cors.allowed-origins:*}")
  private String allowedOrigins;
  @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
  private String allowedMethods;
  @Value("${cors.allowed-headers:*}")
  private String allowedHeaders;
  @Value("${cors.allow-credentials:true}")
  private Boolean allowCredentials;
  @Value("${cors.max-age:3600}")
  private Long maxAge;

  /**
   * Costruttore per l'inizializzazione della configurazione CORS.
   *
   * @param environment l'ambiente Spring per la gestione dei profili attivi
   */
  public CorsConfig(Environment environment) {
    this.environment = environment;
  }

  /**
   * Crea e configura il filtro CORS per l'API Gateway.
   * In ambiente di produzione applica restrizioni specifiche sulle origini permesse,
   * mentre in sviluppo utilizza configurazioni più permissive.
   *
   * @return il filtro CORS configurato
   */
  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();

    if (isProductionProfile()) {
      corsConfig.setAllowedOriginPatterns(Arrays.asList(
          "https://big-unimol-durello.it",
          "https://*.big-unimol-durello.it"
      ));
    } else {
      if ("*".equals(allowedOrigins.trim())) {
        corsConfig.setAllowedOriginPatterns(Arrays.asList("*"));
      } else {
        List<String> originsList = Arrays.asList(allowedOrigins.split(","));
        originsList.replaceAll(String::trim);
        corsConfig.setAllowedOrigins(originsList);
      }
    }

    List<String> methodsList = Arrays.asList(allowedMethods.split(","));
    methodsList.replaceAll(String::trim);
    corsConfig.setAllowedMethods(methodsList);

    if ("*".equals(allowedHeaders.trim())) {
      corsConfig.setAllowedHeaders(Arrays.asList("*"));
    } else {
      List<String> headersList = Arrays.asList(allowedHeaders.split(","));
      headersList.replaceAll(String::trim);
      corsConfig.setAllowedHeaders(headersList);
    }

    corsConfig.setExposedHeaders(Arrays.asList(
        "Access-Control-Allow-Origin",
        "Access-Control-Allow-Credentials",
        "Authorization",
        "X-Total-Count",
        "X-User-ID",
        "X-Roles"
    ));

    corsConfig.setAllowCredentials(allowCredentials);
    corsConfig.setMaxAge(maxAge);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }

  /**
   * Verifica se il profilo attivo è di produzione.
   *
   * @return true se il profilo attivo è "prod" o "production", false altrimenti
   */
  private boolean isProductionProfile() {
    String[] activeProfiles = environment.getActiveProfiles();
    return Arrays.asList(activeProfiles).contains("prod")
        || Arrays.asList(activeProfiles).contains("production");
  }
}