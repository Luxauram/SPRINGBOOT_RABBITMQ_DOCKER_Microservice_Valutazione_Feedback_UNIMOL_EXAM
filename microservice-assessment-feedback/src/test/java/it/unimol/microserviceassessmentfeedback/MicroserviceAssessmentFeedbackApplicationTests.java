package it.unimol.microserviceassessmentfeedback;

import it.unimol.microserviceassessmentfeedback.config.TestRabbitConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRabbitConfig.class)
class MicroserviceAssessmentFeedbackApplicationTests {

  @Test
  void contextLoads() {
  }
}
