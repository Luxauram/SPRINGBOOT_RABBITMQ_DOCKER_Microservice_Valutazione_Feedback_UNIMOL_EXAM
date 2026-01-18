package it.unimol.microserviceassessmentfeedback.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione per la documentazione OpenAPI (Swagger). Definisce le informazioni dell'API, la
 * licenza e la sicurezza JWT.
 */
@Configuration
public class OpenApiConfig {

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  /**
   * Configura l'oggetto OpenAPI con le informazioni dell'applicazione e la sicurezza JWT.
   *
   * @param name        il nome dell'applicazione
   * @param version     la versione dell'applicazione
   * @param description la descrizione dell'applicazione
   * @return l'oggetto OpenAPI configurato
   */
  @Bean
  public OpenAPI customOpenApi(
      @Value("${spring.application.name}") String name,
      @Value("${app.version}") String version,
      @Value("${app.description}") String description
  ) {
    return new OpenAPI()
        .info(new Info()
            .title(name)
            .version(version)
            .description(description)
            .termsOfService("https://github.com/Luxauram/SPRINGBOOT-UNIMOL-MS-Valutazione-Feedback")
            .license(new License()
                .name("Apache License, Version 2.0")
                .identifier("Apache-2.0")
                .url("https://opensource.org/license/apache-2-0/")))
        .components(new Components()
            .addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}