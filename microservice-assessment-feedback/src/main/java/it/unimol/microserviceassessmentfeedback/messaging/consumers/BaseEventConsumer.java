package it.unimol.microserviceassessmentfeedback.messaging.consumers;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Classe astratta base per i consumer di eventi.
 * Fornisce funzionalità comuni per il logging, l’accesso al nome del servizio
 * e la gestione standard del processamento dei messaggi evento.
 * Le classi derivate devono implementare la logica specifica di gestione
 * dell’evento.
 */
public abstract class BaseEventConsumer {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${spring.application.name:microservice-assessment-feedback}")
  protected String serviceName;

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============
  protected String getStringValue(Map<String, Object> message, String key) {
    Object value = message.get(key);
    return value != null ? value.toString() : null;
  }

  protected Integer getIntegerValue(Map<String, Object> message, String key) {
    Object value = message.get(key);
    if (value instanceof Integer i) {
      return i;
    } else if (value instanceof String s) {
      try {
        return Integer.valueOf(s);
      } catch (NumberFormatException e) {
        logger.warn("Cannot parse {} as Integer: {}", key, value);
        return null;
      }
    }
    return null;
  }

  protected Long getLongValue(Map<String, Object> message, String key) {
    Object value = message.get(key);
    if (value instanceof Long l) {
      return l;
    } else if (value instanceof Integer i) {
      return i.longValue();
    } else if (value instanceof String s) {
      try {
        return Long.valueOf(s);
      } catch (NumberFormatException e) {
        logger.warn("Cannot parse {} as Long: {}", key, value);
        return null;
      }
    }
    return null;
  }

  protected Boolean getBooleanValue(Map<String, Object> message, String key) {
    Object value = message.get(key);
    if (value instanceof Boolean b) {
      return b;
    } else if (value instanceof String s) {
      return Boolean.valueOf(s);
    }
    return null;
  }

  // ============ Metodi di Classe ============

  protected void processMessage(Map<String, Object> message, String messageType) {
    if (message == null || message.isEmpty()) {
      logger.warn("Received empty message for type: {}", messageType);
      return;
    }

    try {
      String eventType = (String) message.get("eventType");
      String sourceService = (String) message.get("serviceName");
      Long timestamp = (Long) message.get("timestamp");

      logger.info("Processing {} event from service: {} at timestamp: {}",
          eventType, sourceService, timestamp);

      handleMessage(message, messageType);

      logger.info("{} event processed successfully", eventType);

    } catch (Exception e) {
      logger.error("Error processing {} message: {}", messageType, e.getMessage(), e);
      throw e;
    }
  }

  protected abstract void handleMessage(Map<String, Object> message, String messageType);
}
