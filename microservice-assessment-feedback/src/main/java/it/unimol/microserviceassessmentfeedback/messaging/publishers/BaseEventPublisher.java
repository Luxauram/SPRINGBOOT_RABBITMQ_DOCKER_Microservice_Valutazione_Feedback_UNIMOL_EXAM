package it.unimol.microserviceassessmentfeedback.messaging.publishers;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * Classe astratta base per la pubblicazione di eventi su RabbitMQ.
 * Fornisce funzionalità comuni per la costruzione dei messaggi,
 * la gestione del retry e l’invio degli eventi verso l’exchange configurato.
 */
public abstract class BaseEventPublisher {

  protected static final Logger logger = LoggerFactory.getLogger(BaseEventPublisher.class);

  @Autowired
  protected RabbitTemplate rabbitTemplate;

  @Value("${rabbitmq.exchange.assessments}")
  protected String assessmentsExchange;

  @Value("${spring.application.name:microservice-assessment-feedback}")
  protected String serviceName;

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  /**
   * Pubblica un messaggio su RabbitMQ con retry automatico in caso di fallimento.
   * Utilizza la configurazione di retry definita dall'annotazione {@link Retryable}.
   *
   * @param routingKey la chiave di routing per l'invio del messaggio
   * @param message la mappa contenente i dati del messaggio
   * @param entityType il tipo di entità associata al messaggio
   *                   (es. "assessment", "feedback")
   * @param entityId l'identificativo dell'entità associata al messaggio
   * @throws AmqpException in caso di errore nella pubblicazione su RabbitMQ
   */
  @Retryable(value = {AmqpException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
  protected void publishMessage(String routingKey, Map<String, Object> message, String entityType,
      String entityId) {
    try {
      rabbitTemplate.convertAndSend(assessmentsExchange, routingKey, message);
      logger.info("{} event published successfully for {} ID: {}",
          message.get("eventType"), entityType, entityId);
    } catch (Exception e) {
      logger.error("Error publishing {} event for {} ID: {}",
          message.get("eventType"), entityType, entityId, e);
      throw e;
    }
  }

  /**
   * Aggiunge i campi base comuni a tutti i messaggi pubblicati.
   *
   * @param message la mappa del messaggio a cui aggiungere i campi
   * @param eventType il tipo di evento da associare al messaggio
   */
  protected void addBaseMessageFields(Map<String, Object> message, String eventType) {
    message.put("eventType", eventType);
    message.put("serviceName", serviceName);
    message.put("timestamp", System.currentTimeMillis());
  }
}