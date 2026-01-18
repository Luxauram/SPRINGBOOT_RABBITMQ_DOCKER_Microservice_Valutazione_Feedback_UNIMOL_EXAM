package it.unimol.microserviceassessmentfeedback.config.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe di configurazione per il template RabbitMQ. Configura il convertitore di messaggi e il
 * RabbitTemplate con callback di conferma e ritorno.
 */
@Configuration
public class RabbitMqTemplateConfig {

  private static final Logger logger = LoggerFactory.getLogger(RabbitMqTemplateConfig.class);

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  /**
   * Crea e configura un bean Jackson2JsonMessageConverter. Il convertitore è configurato per creare
   * automaticamente gli ID dei messaggi.
   *
   * @return il Jackson2JsonMessageConverter configurato
   */
  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
    converter.setCreateMessageIds(true);
    return converter;
  }

  /**
   * Crea e configura un bean RabbitTemplate con convertitore di messaggi e callback. Configura il
   * callback di conferma per loggare lo stato di consegna e il callback di ritorno per gestire i
   * messaggi non consegnabili.
   *
   * @param connectionFactory la factory di connessione RabbitMQ
   * @param messageConverter  il convertitore di messaggi JSON
   * @return il RabbitTemplate configurato
   */
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
      Jackson2JsonMessageConverter messageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);

    template.setMessageConverter(messageConverter);

    template.setConfirmCallback((correlationData, ack, cause) -> {
      if (!ack) {
        logger.error("Message not delivered to exchange. Correlation ID: {}, Cause: {}",
            correlationData != null ? correlationData.getId() : "unknown", cause);
      } else {
        logger.debug("Message successfully delivered to exchange. Correlation ID: {}",
            correlationData != null ? correlationData.getId() : "unknown");
      }
    });

    template.setReturnsCallback(returned -> {
      logger.error(
          "Message returned from exchange. Reply Code: {}, Reply Text: {}, Exchange: {}, Routing "
              + "Key: {}, Message: {}",
          returned.getReplyCode(),
          returned.getReplyText(),
          returned.getExchange(),
          returned.getRoutingKey(),
          returned.getMessage().toString());
    });

    template.setMandatory(true);

    return template;
  }
}