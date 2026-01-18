package it.unimol.microserviceuserrole.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * Post-processore per caricare le variabili d'ambiente dal file .env.
 * Questa classe viene eseguita durante l'inizializzazione dell'applicazione Spring Boot
 * per caricare le configurazioni dal file .env prima che l'applicazione venga avviata.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final String DOTENV_SOURCE_NAME = "dotenvProperties";

  /**
   * Definisce l'ordine di esecuzione del post-processore.
   *
   * @return la priorità di esecuzione
   */
  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 10;
  }

  /**
   * Elabora l'ambiente configurabile caricando le variabili dal file .env.
   * Cerca il file .env in diverse posizioni possibili e carica le variabili
   * nell'ambiente Spring.
   *
   * @param environment l'ambiente configurabile di Spring
   * @param application l'applicazione Spring Boot
   */
  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment,
      SpringApplication application) {
    System.out.println("MS-USER: EnvironmentPostProcessor - Tentativo di caricamento file .env...");
    System.out.println("MS-USER: Working Directory: " + System.getProperty("user.dir"));
    System.out.println("MS-USER: Java Class Path: " + System.getProperty("java.class.path"));

    Properties props = new Properties();
    Dotenv dotenv = null;
    boolean envLoaded = false;

    String[] paths = {
        "../",
        ".",
        "../../",
        System.getProperty("user.dir") + "/../",
        "./microservice-user-role/",
        "./env",
        System.getProperty("user.dir").replace("\\microservice-user-role", "")
    };

    for (String path : paths) {
      try {
        File dir = new File(path);
        String absolutePath = dir.getAbsolutePath();
        System.out.println(
            "MS-USER: Tentativo caricamento da: " + path + " (assoluto: " + absolutePath + ")");

        File envFile = new File(dir, ".env");
        System.out.println("MS-USER: Cercando file: " + envFile.getAbsolutePath() + " - Esiste: "
            + envFile.exists());

        if (envFile.exists() && envFile.canRead()) {
          dotenv = Dotenv.configure()
              .directory(path)
              .ignoreIfMalformed()
              .load();

          if (dotenv != null && !dotenv.entries().isEmpty()) {
            System.out.println("MS-USER: File .env trovato e caricato da: " + absolutePath);
            envLoaded = true;
            break;
          }
        }
      } catch (Exception e) {
        System.out.println("MS-USER: Errore caricamento da " + path + ": " + e.getMessage());
      }
    }

    if (!envLoaded) {
      try {
        String workingDir = System.getProperty("user.dir");
        String projectRoot = null;

        if (workingDir.contains("microservice-user-role")) {
          projectRoot = workingDir.substring(0, workingDir.lastIndexOf("microservice-user-role"));
        } else {
          projectRoot = new File(workingDir).getParent();
        }

        if (projectRoot != null) {
          File envFile = new File(projectRoot + File.separator + ".env");
          System.out.println(
              "MS-USER: Tentativo percorso progetto: " + envFile.getAbsolutePath() + " - Esiste: "
                  + envFile.exists());

          if (envFile.exists() && envFile.canRead()) {
            dotenv = Dotenv.configure()
                .directory(projectRoot)
                .ignoreIfMalformed()
                .load();

            if (dotenv != null && !dotenv.entries().isEmpty()) {
              System.out.println(
                  "MS-USER: File .env caricato dalla root del progetto: " + projectRoot);
              envLoaded = true;
            }
          }
        }
      } catch (Exception e) {
        System.out.println("MS-USER: Errore con percorso dinamico: " + e.getMessage());
      }
    }

    if (envLoaded && dotenv != null) {
      System.out.println("MS-USER: Variabili trovate nel file .env:");
      dotenv.entries().forEach(entry -> {
        props.put(entry.getKey(), entry.getValue());
        String value = entry.getKey().contains("KEY") ? "[HIDDEN]" : entry.getValue();
        System.out.println("MS-USER: Caricata variabile: " + entry.getKey() + " = " + value);
      });

      environment.getPropertySources()
          .addFirst(new PropertiesPropertySource(DOTENV_SOURCE_NAME, props));
      System.out.println(
          "MS-USER: File .env caricato con successo! Totale variabili: " + props.size());
    } else {
      System.out.println("MS-USER: ATTENZIONE - Nessun file .env trovato o caricato!");
      System.out.println("MS-USER: Verifica che il file .env esista in uno di questi percorsi:");
      for (String path : paths) {
        File dir = new File(path);
        File envFile = new File(dir, ".env");
        System.out.println("  - " + envFile.getAbsolutePath());
      }
    }

    System.out.println("MS-USER: Property sources disponibili:");
    environment.getPropertySources().forEach(ps -> {
      System.out.println(
          "  - " + ps.getName() + " (classe: " + ps.getClass().getSimpleName() + ")");
    });
  }
}