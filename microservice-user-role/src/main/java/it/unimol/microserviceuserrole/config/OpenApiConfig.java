package it.unimol.microserviceuserrole.config;

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
 * Configurazione OpenAPI/Swagger per il microservizio User-Role.
 * Definisce le informazioni generali dell'API e configura lo schema
 * di autenticazione JWT per la documentazione Swagger.
 */
@Configuration
public class OpenApiConfig {

  /**
   * Crea e configura l'istanza OpenAPI personalizzata.
   * Include informazioni sull'applicazione, licenza e schema di sicurezza JWT.
   *
   * @param name il nome dell'applicazione
   * @param version la versione dell'applicazione
   * @param description la descrizione dell'applicazione
   * @return l'istanza OpenAPI configurata
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