package it.unimol.microserviceuserrole.config;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione RabbitMQ per il microservizio User-Role.
 * Definisce gli exchange, le code, i binding e la configurazione avanzata
 * per la gestione della messaggistica asincrona tramite RabbitMQ.
 */
@Configuration
public class RabbitMqConfig {

  private static final Logger logger = LoggerFactory.getLogger(RabbitMqConfig.class);

  // ===================================================================
  //  EXCHANGE CONFIGURATION
  // ===================================================================
  @Value("${rabbitmq.exchange.main:unimol.exchange}")
  private String mainExchange;

  @Value("${rabbitmq.exchange.dlx:unimol.dlx}")
  private String deadLetterExchange;

  // ===================================================================
  //  QUEUE CONFIGURATION
  // ===================================================================
  @Value("${rabbitmq.queue.userCreated:user.created.queue}")
  private String userCreatedQueue;

  @Value("${rabbitmq.queue.userUpdated:user.updated.queue}")
  private String userUpdatedQueue;

  @Value("${rabbitmq.queue.userDeleted:user.deleted.queue}")
  private String userDeletedQueue;

  @Value("${rabbitmq.queue.roleAssigned:user.role.assigned.queue}")
  private String roleAssignedQueue;

  @Value("${rabbitmq.queue.dlq:unimol.dlq}")
  private String deadLetterQueue;

  // ===================================================================
  //  MESSAGE CONFIGURATION
  // ===================================================================
  @Value("${rabbitmq.message.ttl:86400000}")
  private int messageTtl;

  @Value("${rabbitmq.message.maxRetries:3}")
  private int maxRetries;

  @Value("${rabbitmq.message.retryDelay:5000}")
  private int retryDelay;

  // ===================================================================
  //  EXCHANGE BEANS
  // ===================================================================

  /**
   * Crea l'exchange principale di tipo Topic.
   * Questo exchange viene utilizzato per il routing dei messaggi verso le varie code.
   *
   * @return l'exchange principale configurato
   */
  @Bean
  public TopicExchange mainExchange() {
    logger.info("🔧 Creating UNIFIED TopicExchange with name: {}", mainExchange);
    TopicExchange exchange = new TopicExchange(mainExchange, true, false);
    logger.info("✅ UNIFIED TopicExchange '{}' created successfully", mainExchange);
    return exchange;
  }

  /**
   * Crea l'exchange per la gestione delle Dead Letter (messaggi non consegnabili).
   * Questo exchange riceve i messaggi che non possono essere elaborati correttamente.
   *
   * @return l'exchange Dead Letter configurato
   */
  @Bean
  public DirectExchange deadLetterExchange() {
    logger.info("🔧 Creating UNIFIED Dead Letter Exchange with name: {}", deadLetterExchange);
    DirectExchange exchange = new DirectExchange(deadLetterExchange, true, false);
    logger.info("✅ UNIFIED Dead Letter Exchange '{}' created successfully", deadLetterExchange);
    return exchange;
  }

  // ===================================================================
  //  DEAD LETTER QUEUE
  // ===================================================================

  /**
   * Crea la coda per i messaggi Dead Letter.
   * Questa coda conserva i messaggi che hanno fallito l'elaborazione.
   *
   * @return la coda Dead Letter configurata
   */
  @Bean
  public Queue deadLetterQueue() {
    logger.info("🔧 Creating UNIFIED Dead Letter Queue with name: {}", deadLetterQueue);

    Queue queue = QueueBuilder.durable(deadLetterQueue)
        .build();

    logger.info("✅ UNIFIED Dead Letter Queue '{}' created successfully without TTL",
        deadLetterQueue);
    return queue;
  }

  /**
   * Crea il binding tra la Dead Letter Queue e il Dead Letter Exchange.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding deadLetterBinding() {
    logger.info("🔗 Creating UNIFIED Dead Letter Binding");
    Binding binding = BindingBuilder.bind(deadLetterQueue())
        .to(deadLetterExchange())
        .with("dlq.#");
    logger.info("✅ UNIFIED Dead Letter Binding created successfully");
    return binding;
  }

  // ===================================================================
  //  MAIN QUEUES WITH DLQ CONFIGURATION
  // ===================================================================

  /**
   * Crea la coda per gli eventi di creazione utente.
   *
   * @return la coda userCreated configurata con supporto DLQ
   */
  @Bean
  public Queue userCreatedQueue() {
    logger.info("🔧 Creating userCreatedQueue with name: {}", userCreatedQueue);

    Map<String, Object> args = createUnifiedQueueArguments();
    Queue queue = QueueBuilder.durable(userCreatedQueue)
        .withArguments(args)
        .build();

    logger.info("✅ Queue '{}' created successfully with UNIFIED DLQ configuration",
        userCreatedQueue);
    logQueueConfiguration(userCreatedQueue, args);
    return queue;
  }

  /**
   * Crea la coda per gli eventi di aggiornamento utente.
   *
   * @return la coda userUpdated configurata con supporto DLQ
   */
  @Bean
  public Queue userUpdatedQueue() {
    logger.info("🔧 Creating userUpdatedQueue with name: {}", userUpdatedQueue);

    Map<String, Object> args = createUnifiedQueueArguments();
    Queue queue = QueueBuilder.durable(userUpdatedQueue)
        .withArguments(args)
        .build();

    logger.info("✅ Queue '{}' created successfully with UNIFIED DLQ configuration",
        userUpdatedQueue);
    logQueueConfiguration(userUpdatedQueue, args);
    return queue;
  }

  /**
   * Crea la coda per gli eventi di eliminazione utente.
   *
   * @return la coda userDeleted configurata con supporto DLQ
   */
  @Bean
  public Queue userDeletedQueue() {
    logger.info("🔧 Creating userDeletedQueue with name: {}", userDeletedQueue);

    Map<String, Object> args = createUnifiedQueueArguments();
    Queue queue = QueueBuilder.durable(userDeletedQueue)
        .withArguments(args)
        .build();

    logger.info("✅ Queue '{}' created successfully with UNIFIED DLQ configuration",
        userDeletedQueue);
    logQueueConfiguration(userDeletedQueue, args);
    return queue;
  }

  /**
   * Crea la coda per gli eventi di assegnazione ruolo.
   *
   * @return la coda roleAssigned configurata con supporto DLQ
   */
  @Bean
  public Queue roleAssignedQueue() {
    logger.info("🔧 Creating roleAssignedQueue with name: {}", roleAssignedQueue);

    Map<String, Object> args = createUnifiedQueueArguments();
    Queue queue = QueueBuilder.durable(roleAssignedQueue)
        .withArguments(args)
        .build();

    logger.info("✅ Queue '{}' created successfully with UNIFIED DLQ configuration",
        roleAssignedQueue);
    logQueueConfiguration(roleAssignedQueue, args);
    return queue;
  }

  // ===================================================================
  //  QUEUE BINDINGS
  // ===================================================================

  /**
   * Crea il binding tra la coda userCreated e l'exchange principale.
   *
   * @return il binding configurato con routing key "users.created"
   */
  @Bean
  public Binding userCreatedBinding() {
    logger.info("🔗 Creating UNIFIED Binding for USER_CREATED");
    logger.debug("📋 Binding details - Queue: '{}', Exchange: '{}', RoutingKey: 'users.created'",
        userCreatedQueue, mainExchange);

    Binding binding = BindingBuilder.bind(userCreatedQueue())
        .to(mainExchange())
        .with("users.created");

    logger.info(
        "✅ UNIFIED Binding USER_CREATED created: Queue '{}' -> Exchange '{}' with routing key "
            + "'users.created'",
        userCreatedQueue, mainExchange);
    return binding;
  }

  /**
   * Crea il binding tra la coda userUpdated e l'exchange principale.
   *
   * @return il binding configurato con routing key "users.updated"
   */
  @Bean
  public Binding userUpdatedBinding() {
    logger.info("🔗 Creating UNIFIED Binding for USER_UPDATED");
    logger.debug("📋 Binding details - Queue: '{}', Exchange: '{}', RoutingKey: 'users.updated'",
        userUpdatedQueue, mainExchange);

    Binding binding = BindingBuilder.bind(userUpdatedQueue())
        .to(mainExchange())
        .with("users.updated");

    logger.info(
        "✅ UNIFIED Binding USER_UPDATED created: Queue '{}' -> Exchange '{}' with routing key "
            + "'users.updated'",
        userUpdatedQueue, mainExchange);
    return binding;
  }

  /**
   * Crea il binding tra la coda userDeleted e l'exchange principale.
   *
   * @return il binding configurato con routing key "users.deleted"
   */
  @Bean
  public Binding userDeletedBinding() {
    logger.info("🔗 Creating UNIFIED Binding for USER_DELETED");
    logger.debug("📋 Binding details - Queue: '{}', Exchange: '{}', RoutingKey: 'users.deleted'",
        userDeletedQueue, mainExchange);

    Binding binding = BindingBuilder.bind(userDeletedQueue())
        .to(mainExchange())
        .with("users.deleted");

    logger.info(
        "✅ UNIFIED Binding USER_DELETED created: Queue '{}' -> Exchange '{}' with routing key "
            + "'users.deleted'",
        userDeletedQueue, mainExchange);
    return binding;
  }

  /**
   * Crea il binding tra la coda roleAssigned e l'exchange principale.
   *
   * @return il binding configurato con routing key "users.role.assigned"
   */
  @Bean
  public Binding roleAssignedBinding() {
    logger.info("🔗 Creating UNIFIED Binding for ROLE_ASSIGNED");
    logger.debug(
        "📋 Binding details - Queue: '{}', Exchange: '{}', RoutingKey: 'users.role.assigned'",
        roleAssignedQueue, mainExchange);

    Binding binding = BindingBuilder.bind(roleAssignedQueue())
        .to(mainExchange())
        .with("users.role.assigned");

    logger.info(
        "✅ UNIFIED Binding ROLE_ASSIGNED created: Queue '{}' -> Exchange '{}' with routing key "
            + "'users.role.assigned'",
        roleAssignedQueue, mainExchange);
    return binding;
  }

  // ===================================================================
  //  MESSAGE CONVERTER AND RABBIT TEMPLATE
  // ===================================================================

  /**
   * Crea il convertitore di messaggi Jackson per la serializzazione JSON.
   *
   * @return il convertitore configurato per la serializzazione JSON
   */
  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    logger.info("🔧 Creating Jackson2JsonMessageConverter");
    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
    converter.setCreateMessageIds(true);
    logger.info(
        "✅ Jackson2JsonMessageConverter configured for JSON serialization with Message IDs");
    return converter;
  }

  /**
   * Crea e configura il RabbitTemplate per l'invio di messaggi.
   * Include callback per la conferma di delivery e gestione dei messaggi restituiti.
   *
   * @param connectionFactory la factory per la connessione a RabbitMQ
   * @return il RabbitTemplate configurato
   */
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    logger.info("🔧 Creating UNIFIED RabbitTemplate");
    logger.debug("📋 ConnectionFactory received: {}", connectionFactory.getClass().getSimpleName());

    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter());
    template.setMandatory(true);

    // Configurazione callback per conferme di delivery
    template.setConfirmCallback((correlationData, ack, cause) -> {
      if (!ack) {
        logger.error("❌ Message not delivered to exchange. Correlation ID: {}, Cause: {}",
            correlationData != null ? correlationData.getId() : "unknown", cause);
      } else {
        logger.debug("✅ Message successfully delivered to exchange. Correlation ID: {}",
            correlationData != null ? correlationData.getId() : "unknown");
      }
    });

    // Configurazione callback per messaggi restituiti
    template.setReturnsCallback(returned -> {
      logger.error(
          "🔄 Message returned from exchange. Reply Code: {}, Reply Text: {}, Exchange: {}, "
              + "Routing Key: {}",
          returned.getReplyCode(),
          returned.getReplyText(),
          returned.getExchange(),
          returned.getRoutingKey());
    });

    logger.info("✅ UNIFIED RabbitTemplate configured successfully with confirmation callbacks");

    logUnifiedConfigurationSummary();

    return template;
  }

  // ===================================================================
  //  UTILITY METHODS
  // ===================================================================

  /**
   * Crea gli argomenti standard per le code con supporto Dead Letter Queue.
   *
   * @return una mappa contenente gli argomenti della coda
   */
  private Map<String, Object> createUnifiedQueueArguments() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-dead-letter-exchange", deadLetterExchange);
    args.put("x-dead-letter-routing-key", "dlq");
    args.put("x-message-ttl", messageTtl);
    args.put("x-max-retries", maxRetries);
    args.put("x-retry-delay", retryDelay);
    return args;
  }

  /**
   * Registra nel log la configurazione dettagliata di una coda.
   *
   * @param queueName il nome della coda
   * @param args gli argomenti di configurazione della coda
   */
  private void logQueueConfiguration(String queueName, Map<String, Object> args) {
    logger.debug("📋 UNIFIED Queue '{}' configuration:", queueName);
    logger.debug("   - Durable: true");
    logger.debug("   - Dead Letter Exchange: {} (UNIFIED)", args.get("x-dead-letter-exchange"));
    logger.debug("   - Dead Letter Routing Key: {}", args.get("x-dead-letter-routing-key"));
    logger.debug("   - Message TTL: {}ms ({}h)", args.get("x-message-ttl"), messageTtl / 3600000);
    logger.debug("   - Max Retries: {}", args.get("x-max-retries"));
    logger.debug("   - Retry Delay: {}ms", args.get("x-retry-delay"));
  }

  /**
   * Registra nel log un riepilogo completo della configurazione RabbitMQ.
   */
  private void logUnifiedConfigurationSummary() {
    logger.info("🎯 CONFIGURAZIONE RABBITMQ UNIFICATA COMPLETATA:");
    logger.info("   📧 UNIFIED Main Exchange: {}", mainExchange);
    logger.info("   ⚰️ UNIFIED Dead Letter Exchange: {}", deadLetterExchange);
    logger.info("   📥 Queues configurate (con prefisso servizio):");
    logger.info("      - UserCreated: {}", userCreatedQueue);
    logger.info("      - UserUpdated: {}", userUpdatedQueue);
    logger.info("      - UserDeleted: {}", userDeletedQueue);
    logger.info("      - RoleAssigned: {}", roleAssignedQueue);
    logger.info("      - Dead Letter Queue: {}", deadLetterQueue);
    logger.info("   🔗 UNIFIED Routing keys:");
    logger.info("      - users.created -> {}", userCreatedQueue);
    logger.info("      - users.updated -> {}", userUpdatedQueue);
    logger.info("      - users.deleted -> {}", userDeletedQueue);
    logger.info("      - users.role.assigned -> {}", roleAssignedQueue);
    logger.info("   ⚙️ Configurazione avanzata UNIFICATA:");
    logger.info("      - Message TTL: {}ms ({}h)", messageTtl, messageTtl / 3600000);
    logger.info("      - Max Retries: {}", maxRetries);
    logger.info("      - Retry Delay: {}ms", retryDelay);
    logger.info("      - Dead Letter Support: UNIFIED ({})", deadLetterExchange);
    logger.info("      - Publisher Confirms: Enabled");
    logger.info("      - Message Returns: Enabled");
    logger.info("   🌟 ARCHITETTURA: Tutti i microservizi usano exchange '{}' e DLX '{}'",
        mainExchange, deadLetterExchange);
  }
}