package it.unimol.microserviceassessmentfeedback.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * Classe per la gestione dei file env.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final String DOTENV_SOURCE_NAME = "dotenvProperties";

  // ============ Costruttore ============

  // ============ Metodi Override ============
  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 10;
  }

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment,
      SpringApplication application) {
    System.out.println(
        "MS-ASSESSMENT: EnvironmentPostProcessor - Tentativo di caricamento file .env...");
    System.out.println("MS-ASSESSMENT: Working Directory: " + System.getProperty("user.dir"));
    System.out.println("MS-ASSESSMENT: Java Class Path: " + System.getProperty("java.class.path"));

    Properties props = new Properties();
    Dotenv dotenv = null;
    boolean envLoaded = false;

    String[] paths = {
        "../",
        ".",
        "../../",
        System.getProperty("user.dir") + "/../",
        "./microservice-assessment-feedback/",
        "./env",
        System.getProperty("user.dir").replace("\\microservice-assessment-feedback", "")
    };

    for (String path : paths) {
      try {
        File dir = new File(path);
        String absolutePath = dir.getAbsolutePath();
        System.out.println(
            "MS-ASSESSMENT: Tentativo caricamento da: " + path + " (assoluto: " + absolutePath
                + ")");

        File envFile = new File(dir, ".env");
        System.out.println(
            "MS-ASSESSMENT: Cercando file: " + envFile.getAbsolutePath() + " - Esiste: "
                + envFile.exists());

        if (envFile.exists() && envFile.canRead()) {
          dotenv = Dotenv.configure()
              .directory(path)
              .ignoreIfMalformed()
              .load();

          if (dotenv != null && !dotenv.entries().isEmpty()) {
            System.out.println("MS-ASSESSMENT: File .env trovato e caricato da: " + absolutePath);
            envLoaded = true;
            break;
          }
        }
      } catch (Exception e) {
        System.out.println("MS-ASSESSMENT: Errore caricamento da " + path + ": " + e.getMessage());
      }
    }

    if (!envLoaded) {
      try {
        String workingDir = System.getProperty("user.dir");
        String projectRoot = null;

        if (workingDir.contains("microservice-assessment-feedback")) {
          projectRoot = workingDir.substring(0,
              workingDir.lastIndexOf("microservice-assessment-feedback"));
        } else {
          projectRoot = new File(workingDir).getParent();
        }

        if (projectRoot != null) {
          File envFile = new File(projectRoot + File.separator + ".env");
          System.out.println(
              "MS-ASSESSMENT: Tentativo percorso progetto: " + envFile.getAbsolutePath()
                  + " - Esiste: " + envFile.exists());

          if (envFile.exists() && envFile.canRead()) {
            dotenv = Dotenv.configure()
                .directory(projectRoot)
                .ignoreIfMalformed()
                .load();

            if (dotenv != null && !dotenv.entries().isEmpty()) {
              System.out.println(
                  "MS-ASSESSMENT: File .env caricato dalla root del progetto: " + projectRoot);
              envLoaded = true;
            }
          }
        }
      } catch (Exception e) {
        System.out.println("MS-ASSESSMENT: Errore con percorso dinamico: " + e.getMessage());
      }
    }

    if (envLoaded && dotenv != null) {
      System.out.println("MS-ASSESSMENT: Variabili trovate nel file .env:");
      dotenv.entries().forEach(entry -> {
        props.put(entry.getKey(), entry.getValue());
        String value = entry.getKey().contains("KEY") ? "[HIDDEN]" : entry.getValue();
        System.out.println("MS-ASSESSMENT: Caricata variabile: " + entry.getKey() + " = " + value);
      });

      environment.getPropertySources()
          .addFirst(new PropertiesPropertySource(DOTENV_SOURCE_NAME, props));
      System.out.println(
          "MS-ASSESSMENT: File .env caricato con successo! Totale variabili: " + props.size());
    } else {
      System.out.println("MS-ASSESSMENT: ATTENZIONE - Nessun file .env trovato o caricato!");
      System.out.println(
          "MS-ASSESSMENT: Verifica che il file .env esista in uno di questi percorsi:");
      for (String path : paths) {
        File dir = new File(path);
        File envFile = new File(dir, ".env");
        System.out.println("  - " + envFile.getAbsolutePath());
      }
    }

    System.out.println("MS-ASSESSMENT: Property sources disponibili:");
    environment.getPropertySources().forEach(ps -> {
      System.out.println(
          "  - " + ps.getName() + " (classe: " + ps.getClass().getSimpleName() + ")");
    });
  }

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============
}