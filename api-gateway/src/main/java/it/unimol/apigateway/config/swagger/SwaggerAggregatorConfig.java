package it.unimol.apigateway.config.swagger;

import java.util.HashSet;
import java.util.Set;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configurazione per l'aggregazione delle documentazioni Swagger dei microservizi.
 * Permette di visualizzare in un'unica interfaccia Swagger la documentazione
 * dell'API Gateway e di tutti i microservizi collegati.
 */
@Configuration
public class SwaggerAggregatorConfig {

  /**
   * Configura l'interfaccia Swagger UI aggregata.
   * Raccoglie le documentazioni OpenAPI di tutti i microservizi e le rende
   * disponibili attraverso un'unica interfaccia utente.
   *
   * @return la configurazione Swagger UI con tutti i servizi aggregati
   */
  @Bean
  @Primary
  public SwaggerUiConfigProperties swaggerUiConfig() {
    final SwaggerUiConfigProperties config = new SwaggerUiConfigProperties();
    Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();

    // API Gateway
    urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
        "API Gateway", "/v3/api-docs", "api-gateway"));

    // MS User Role
    urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
        "User & Role Service", "/user-service/v3/api-docs", "microservice-user-role"));

    // MS Assessment Feedback
    urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
        "Assessment & Feedback Service", "/assessment-service/v3/api-docs",
        "microservice-assessment-feedback"));

    config.setUrls(urls);
    config.setOperationsSorter("alpha");

    return config;
  }

}