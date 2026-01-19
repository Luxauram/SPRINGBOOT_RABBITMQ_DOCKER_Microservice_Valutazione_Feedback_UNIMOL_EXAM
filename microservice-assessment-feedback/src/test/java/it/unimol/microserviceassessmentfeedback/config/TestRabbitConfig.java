package it.unimol.microserviceassessmentfeedback.config;

import org.mockito.Mockito;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Configurazione di test per RabbitMQ Questa classe fornisce mock per i bean RabbitMQ durante i
 * test
 */
@TestConfiguration
@Profile("test")
public class TestRabbitConfig {

  /**
   * Mock del ConnectionFactory per i test.
   *
   * L'annotazione {@code @Primary} assicura che questo bean abbia priorità su altri.
   */
  @Bean
  @Primary
  public ConnectionFactory connectionFactory() {
    return Mockito.mock(ConnectionFactory.class);
  }

  /**
   * Mock del RabbitTemplate per i test.
   *
   * L'annotazione {@code @Primary} assicura che questo bean abbia priorità su altri.
   */
  @Bean
  @Primary
  public RabbitTemplate rabbitTemplate() {
    return Mockito.mock(RabbitTemplate.class);
  }
}
