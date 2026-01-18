package it.unimol.microserviceuserrole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale dell'applicazione Microservice User Role.
 * Questa classe funge da punto di ingresso per l'applicazione Spring Boot
 * dedicata alla gestione di utenti e ruoli.
 */
@SpringBootApplication
public class MicroserviceUserRoleApplication {

  /**
   * Metodo main per avviare l'applicazione Spring Boot.
   *
   * @param args argomenti della riga di comando
   */
  public static void main(String[] args) {
    SpringApplication.run(MicroserviceUserRoleApplication.class, args);
  }

}