package it.unimol.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale dell'applicazione API Gateway.
 * Questa classe funge da punto di ingresso per l'applicazione Spring Boot.
 */
@SpringBootApplication
public class ApiGatewayApplication {

  /**
   * Metodo main per avviare l'applicazione Spring Boot.
   *
   * @param args argomenti della riga di comando
   */
  public static void main(String[] args) {
    SpringApplication.run(ApiGatewayApplication.class, args);
  }

}