package it.unimol.microserviceassessmentfeedback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale di avvio del microservizio Assessment Feedback.
 * Avvia l'applicazione Spring Boot e inizializza il contesto
 * dell'applicazione.
 */
@SpringBootApplication
public class MicroserviceAssessmentFeedbackApplication {

  public static void main(String[] args) {
    SpringApplication.run(MicroserviceAssessmentFeedbackApplication.class, args);
  }

}
