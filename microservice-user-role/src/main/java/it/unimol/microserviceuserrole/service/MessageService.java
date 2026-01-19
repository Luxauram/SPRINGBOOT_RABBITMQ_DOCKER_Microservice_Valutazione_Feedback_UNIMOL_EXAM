package it.unimol.microserviceuserrole.service;

import it.unimol.microserviceuserrole.dto.user.UserDto;
import it.unimol.microserviceuserrole.dto.user.UserProfileDto;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Servizio per la pubblicazione di messaggi RabbitMQ relativi agli eventi utente e ruoli.
 * Gestisce la pubblicazione di eventi USER_CREATED, USER_UPDATED, USER_DELETED,
 * ROLE_ASSIGNED e PROFILE_UPDATED attraverso un'architettura unificata.
 */
@Service
public class MessageService {

  private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
  // ===================================================================
  //  ROUTING KEYS CONSTANTS
  // ===================================================================
  private static final String USER_CREATED_ROUTING_KEY = "user.created";
  private static final String USER_UPDATED_ROUTING_KEY = "user.updated";
  private static final String USER_DELETED_ROUTING_KEY = "user.deleted";
  private static final String ROLE_ASSIGNED_ROUTING_KEY = "role.assigned";
  // ===================================================================
  //  EVENT TYPES CONSTANTS
  // ===================================================================
  private static final String USER_CREATED_EVENT = "USER_CREATED";
  private static final String USER_UPDATED_EVENT = "USER_UPDATED";
  private static final String USER_DELETED_EVENT = "USER_DELETED";
  private static final String ROLE_ASSIGNED_EVENT = "ROLE_ASSIGNED";
  private static final String PROFILE_UPDATED_EVENT = "PROFILE_UPDATED";
  @Autowired
  private RabbitTemplate rabbitTemplate;
  @Value("${rabbitmq.exchange.main:unimol.exchange}")
  private String mainExchange;
  @Value("${spring.application.name:user-role-service}")
  private String serviceName;

  // ===================================================================
  //  PUBLIC METHODS
  // ===================================================================

  /**
   * Pubblica un messaggio USER_CREATED quando viene creato un nuovo utente.
   *
   * @param user Il DTO dell'utente creato.
   */
  @Retryable(
      value = {AmqpException.class, MessageConversionException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public void publishUserCreated(UserDto user) {
    final String correlationId = generateCorrelationId();

    logger.info("🚀 === INIZIO PUBBLICAZIONE USER_CREATED UNIFICATA === [ID: {}]", correlationId);
    logger.info("📤 Preparazione messaggio per utente: {} (ID: {})", user.username(), user.id());
    logger.debug("📋 Dettagli utente - Email: {}, Nome: {} {}, Ruolo: {} ({})",
        user.email(), user.name(), user.surname(), user.role().name(), user.role().id());
    logger.info("🌐 UNIFIED: Utilizzo exchange '{}' e routing key '{}'", mainExchange,
        USER_CREATED_ROUTING_KEY);

    try {
      Map<String, Object> message = createUserMessage(user, USER_CREATED_EVENT, correlationId);
      publishUnifiedMessage(USER_CREATED_ROUTING_KEY, message, correlationId);

      logger.info("✅ Messaggio USER_CREATED inviato con successo! [ID: {}]", correlationId);
      logger.info("🎯 UNIFIED Target: Exchange '{}' -> Routing Key '{}'", mainExchange,
          USER_CREATED_ROUTING_KEY);
      logger.info("🏁 === FINE PUBBLICAZIONE USER_CREATED UNIFICATA === [ID: {}]", correlationId);

    } catch (Exception e) {
      handleUnifiedPublishError("USER_CREATED", correlationId, USER_CREATED_ROUTING_KEY, e,
          Map.of("username", user.username(), "userId", user.id()));
      throw new RuntimeException("Errore nell'invio messaggio USER_CREATED UNIFICATO", e);
    }
  }

  /**
   * Pubblica un messaggio USER_UPDATED quando viene aggiornato un utente.
   *
   * @param user Il DTO dell'utente aggiornato.
   */
  @Retryable(
      value = {AmqpException.class, MessageConversionException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public void publishUserUpdated(UserDto user) {
    final String correlationId = generateCorrelationId();

    logger.info("🚀 === INIZIO PUBBLICAZIONE USER_UPDATED UNIFICATA === [ID: {}]", correlationId);
    logger.info("📤 Preparazione messaggio per aggiornamento utente: {} (ID: {})", user.username(),
        user.id());
    logger.debug("📋 Dettagli aggiornamento - Email: {}, Nome: {} {}, Ruolo: {} ({})",
        user.email(), user.name(), user.surname(), user.role().name(), user.role().id());
    logger.info("🌐 UNIFIED: Utilizzo exchange '{}' e routing key '{}'", mainExchange,
        USER_UPDATED_ROUTING_KEY);

    try {
      Map<String, Object> message = createUserMessage(user, USER_UPDATED_EVENT, correlationId);
      publishUnifiedMessage(USER_UPDATED_ROUTING_KEY, message, correlationId);

      logger.info("✅ Messaggio USER_UPDATED inviato con successo! [ID: {}]", correlationId);
      logger.info("🎯 UNIFIED Target: Exchange '{}' -> Routing Key '{}'", mainExchange,
          USER_UPDATED_ROUTING_KEY);
      logger.info("🏁 === FINE PUBBLICAZIONE USER_UPDATED UNIFICATA === [ID: {}]", correlationId);

    } catch (Exception e) {
      handleUnifiedPublishError("USER_UPDATED", correlationId, USER_UPDATED_ROUTING_KEY, e,
          Map.of("username", user.username(), "userId", user.id()));
      throw new RuntimeException("Errore nell'invio messaggio USER_UPDATED UNIFICATO", e);
    }
  }

  /**
   * Pubblica un messaggio USER_DELETED quando viene eliminato un utente.
   *
   * @param userId L'ID dell'utente eliminato.
   */
  @Retryable(
      value = {AmqpException.class, MessageConversionException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public void publishUserDeleted(String userId) {
    final String correlationId = generateCorrelationId();

    logger.info("🚀 === INIZIO PUBBLICAZIONE USER_DELETED UNIFICATA === [ID: {}]", correlationId);
    logger.info("🗑️ Preparazione messaggio per cancellazione utente ID: {}", userId);
    logger.info("🌐 UNIFIED: Utilizzo exchange '{}' e routing key '{}'", mainExchange,
        USER_DELETED_ROUTING_KEY);

    try {
      Map<String, Object> message = createBasicMessage(USER_DELETED_EVENT, correlationId);
      message.put("userId", userId);

      publishUnifiedMessage(USER_DELETED_ROUTING_KEY, message, correlationId);

      logger.info("✅ Messaggio USER_DELETED inviato con successo! [ID: {}]", correlationId);
      logger.info("🎯 UNIFIED Target: Exchange '{}' -> Routing Key '{}'", mainExchange,
          USER_DELETED_ROUTING_KEY);
      logger.info("🏁 === FINE PUBBLICAZIONE USER_DELETED UNIFICATA === [ID: {}]", correlationId);

    } catch (Exception e) {
      handleUnifiedPublishError("USER_DELETED", correlationId, USER_DELETED_ROUTING_KEY, e,
          Map.of("userId", userId));
      throw new RuntimeException("Errore nell'invio messaggio USER_DELETED UNIFICATO", e);
    }
  }

  /**
   * Pubblica un messaggio ROLE_ASSIGNED quando viene assegnato un ruolo a un utente.
   *
   * @param userId L'ID dell'utente a cui è stato assegnato il ruolo.
   * @param roleId L'ID del ruolo assegnato.
   */
  @Retryable(
      value = {AmqpException.class, MessageConversionException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public void publishRoleAssigned(String userId, String roleId) {
    final String correlationId = generateCorrelationId();

    logger.info("🚀 === INIZIO PUBBLICAZIONE ROLE_ASSIGNED UNIFICATA === [ID: {}]", correlationId);
    logger.info("🎭 Preparazione messaggio per assegnazione ruolo - User ID: {}, Role ID: {}",
        userId, roleId);
    logger.info("🌐 UNIFIED: Utilizzo exchange '{}' e routing key '{}'", mainExchange,
        ROLE_ASSIGNED_ROUTING_KEY);

    try {
      Map<String, Object> message = createBasicMessage(ROLE_ASSIGNED_EVENT, correlationId);
      message.put("userId", userId);
      message.put("roleId", roleId);

      publishUnifiedMessage(ROLE_ASSIGNED_ROUTING_KEY, message, correlationId);

      logger.info("✅ Messaggio ROLE_ASSIGNED inviato con successo! [ID: {}]", correlationId);
      logger.info("🎯 UNIFIED Target: Exchange '{}' -> Routing Key '{}'", mainExchange,
          ROLE_ASSIGNED_ROUTING_KEY);
      logger.info("🏁 === FINE PUBBLICAZIONE ROLE_ASSIGNED UNIFICATA === [ID: {}]", correlationId);

    } catch (Exception e) {
      handleUnifiedPublishError("ROLE_ASSIGNED", correlationId, ROLE_ASSIGNED_ROUTING_KEY, e,
          Map.of("userId", userId, "roleId", roleId));
      throw new RuntimeException("Errore nell'invio messaggio ROLE_ASSIGNED UNIFICATO", e);
    }
  }

  /**
   * Pubblica un messaggio PROFILE_UPDATED quando viene aggiornato il profilo di un utente.
   *
   * @param profile Il DTO del profilo utente aggiornato.
   */
  @Retryable(
      value = {AmqpException.class, MessageConversionException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public void publishProfileUpdated(UserProfileDto profile) {
    final String correlationId = generateCorrelationId();

    logger.info("🚀 === INIZIO PUBBLICAZIONE PROFILE_UPDATED UNIFICATA === [ID: {}]",
        correlationId);
    logger.info("👤 Preparazione messaggio per aggiornamento profilo - User: {} (ID: {})",
        profile.username(), profile.id());
    logger.debug("📋 Dettagli profilo - Email: {}, Nome: {} {}",
        profile.email(), profile.name(), profile.surname());
    logger.info("🌐 UNIFIED: Utilizzo exchange '{}' e routing key '{}'", mainExchange,
        USER_UPDATED_ROUTING_KEY);

    try {
      Map<String, Object> message = createBasicMessage(PROFILE_UPDATED_EVENT, correlationId);
      message.put("userId", profile.id());
      message.put("username", profile.username());
      message.put("email", profile.email());
      message.put("name", profile.name());
      message.put("surname", profile.surname());

      publishUnifiedMessage(USER_UPDATED_ROUTING_KEY, message, correlationId);

      logger.info("✅ Messaggio PROFILE_UPDATED inviato con successo! [ID: {}]", correlationId);
      logger.info("🎯 UNIFIED Target: Exchange '{}' -> Routing Key '{}'", mainExchange,
          USER_UPDATED_ROUTING_KEY);
      logger.info("🏁 === FINE PUBBLICAZIONE PROFILE_UPDATED UNIFICATA === [ID: {}]",
          correlationId);

    } catch (Exception e) {
      handleUnifiedPublishError("PROFILE_UPDATED", correlationId, USER_UPDATED_ROUTING_KEY, e,
          Map.of("username", profile.username(), "userId", profile.id()));
      throw new RuntimeException("Errore nell'invio messaggio PROFILE_UPDATED UNIFICATO", e);
    }
  }

  // ===================================================================
  //  PRIVATE UTILITY METHODS
  // ===================================================================
  @SuppressWarnings("JavaUtilDate")
  private void publishUnifiedMessage(String routingKey, Map<String, Object> message,
      String correlationId) {
    logger.info("📡 Invio messaggio UNIFICATO a RabbitMQ... [ID: {}]", correlationId);
    logger.debug("📦 Messaggio UNIFICATO creato con successo [ID: {}]:", correlationId);
    logger.debug("   📧 UNIFIED Exchange: {}", mainExchange);
    logger.debug("   🔑 Routing Key: {}", routingKey);
    logger.debug("   📄 Event Type: {}", message.get("eventType"));
    logger.debug("   🆔 Correlation ID: {}", correlationId);
    logger.debug("   ⏰ Timestamp: {}", message.get("timestamp"));
    logger.debug("   🏢 Source Service: {}", message.get("sourceService"));

    // Configurazione delle proprietà del messaggio
    MessageProperties properties = new MessageProperties();
    properties.setCorrelationId(correlationId);
    properties.setMessageId(correlationId);
    properties.setTimestamp(new Date((Long) message.get("timestamp")));
    properties.setContentType("application/json");

    // Header personalizzati per il tracciamento UNIFICATO
    properties.setHeader("source-service", serviceName);
    properties.setHeader("event-type", message.get("eventType"));
    properties.setHeader("correlation-id", correlationId);
    properties.setHeader("unified-architecture", "unimol");
    properties.setHeader("routing-pattern", "unified");

    rabbitTemplate.convertAndSend(mainExchange, routingKey, message, msg -> {
      msg.getMessageProperties().setCorrelationId(correlationId);
      msg.getMessageProperties().setMessageId(correlationId);
      msg.getMessageProperties().setTimestamp(new Date((Long) message.get("timestamp")));
      msg.getMessageProperties().setHeader("unified-exchange", mainExchange);
      return msg;
    });
  }

  private Map<String, Object> createUserMessage(UserDto user, String eventType,
      String correlationId) {
    logger.debug("🔧 Creazione messaggio standard per evento: {} [ID: {}]", eventType,
        correlationId);
    logger.debug("📋 Dati utente ricevuti - ID: {}, Username: {}, Email: {}",
        user.id(), user.username(), user.email());

    Map<String, Object> message = createBasicMessage(eventType, correlationId);
    message.put("userId", user.id());
    message.put("username", user.username());
    message.put("email", user.email());
    message.put("name", user.name());
    message.put("surname", user.surname());
    message.put("roleId", user.role().id());
    message.put("roleName", user.role().name());

    logger.debug("✅ Messaggio standard creato con {} campi [ID: {}]", message.size(),
        correlationId);
    logger.trace("📄 Contenuto completo messaggio [ID: {}]: {}", correlationId, message);

    return message;
  }

  private Map<String, Object> createBasicMessage(String eventType, String correlationId) {
    logger.debug("🔧 Creazione messaggio base per evento: {} [ID: {}]", eventType, correlationId);

    Map<String, Object> message = new HashMap<>();
    message.put("eventType", eventType);
    message.put("correlationId", correlationId);
    message.put("timestamp", System.currentTimeMillis());
    message.put("sourceService", serviceName);
    message.put("version", "1.0");

    logger.debug("✅ Messaggio base creato con {} campi standard [ID: {}]", message.size(),
        correlationId);
    logger.trace("📄 Messaggio base [ID: {}]: {}", correlationId, message);

    return message;
  }

  private String generateCorrelationId() {
    String correlationId = UUID.randomUUID().toString();
    logger.trace("🔗 Generato nuovo Correlation ID: {}", correlationId);
    return correlationId;
  }

  private void handleUnifiedPublishError(String eventType, String correlationId, String routingKey,
      Exception e, Map<String, Object> context) {
    logger.error("❌ === ERRORE PUBBLICAZIONE {} UNIFICATA === [ID: {}]", eventType, correlationId);
    logger.error("💥 Errore durante l'invio del messaggio {} UNIFICATO [ID: {}]", eventType,
        correlationId);
    logger.error("📧 UNIFIED Exchange: {}, Routing Key: {}", mainExchange, routingKey);

    // Log del contesto dell'errore
    context.forEach((key, value) ->
        logger.error("📋 Context - {}: {}", key, value));

    logger.error("🔥 Dettagli errore UNIFICATO [ID: {}]: {}", correlationId, e.getMessage(), e);

    // Log per identificare il tipo di eccezione
    if (e instanceof AmqpException) {
      logger.error(
          "🐰 AMQP Exception - Problema di connessione/configurazione RabbitMQ UNIFICATO [ID: {}]",
          correlationId);
      logger.error(
          "💡 Suggerimento: Verificare che RabbitMQ sia in esecuzione e che l'exchange UNIFICATO "
              + "'{}' esista",
          mainExchange);
    } else if (e instanceof MessageConversionException) {
      logger.error(
          "🔄 Message Conversion Exception - Problema di serializzazione messaggio UNIFICATO [ID:"
              + " {}]",
          correlationId);
      logger.error("💡 Suggerimento: Verificare che il messaggio sia serializzabile in JSON");
    } else {
      logger.error("⚠️ Eccezione generica durante la pubblicazione UNIFICATA [ID: {}]",
          correlationId);
      logger.error("💡 Suggerimento: Verificare i log precedenti per maggiori dettagli");
    }

    // Statistiche per il debugging UNIFICATO
    logger.error("📊 Statistiche errore UNIFICATO [ID: {}]:", correlationId);
    logger.error("   - Event Type: {}", eventType);
    logger.error("   - Routing Key: {}", routingKey);
    logger.error("   - UNIFIED Exchange: {}", mainExchange);
    logger.error("   - Service: {}", serviceName);
    logger.error("   - Exception Type: {}", e.getClass().getSimpleName());
    logger.error("   - Thread: {}", Thread.currentThread().getName());
    logger.error("   - Architecture: UNIFIED");

    logger.error(
        "🔄 Il meccanismo di retry tenterà automaticamente di reinviare il messaggio UNIFICATO "
            + "[ID: {}]",
        correlationId);
    logger.error("🏁 === FINE GESTIONE ERRORE {} UNIFICATA === [ID: {}]", eventType, correlationId);
  }
}