package it.unimol.microserviceassessmentfeedback.config.rabbitmq;

import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.ASSIGNMENT_CREATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.ASSIGNMENT_SUBMITTED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.ASSIGNMENT_UPDATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.COURSE_CREATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.COURSE_DELETED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.EXAM_COMPLETED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.EXAM_GRADE_REGISTERED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.ROLE_ASSIGNED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.STUDENT_CREATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.TEACHER_CREATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.USER_CREATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.USER_DELETED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.ConsumerRoutingKeys.USER_UPDATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.ASSESSMENT_CREATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.ASSESSMENT_DELETED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.ASSESSMENT_UPDATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.FEEDBACK_CREATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.FEEDBACK_DELETED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.FEEDBACK_UPDATED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.SURVEY_COMMENTS_REQUESTED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.SURVEY_COMPLETED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.SURVEY_RESPONSES_BULK_SUBMITTED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.SURVEY_RESPONSE_SUBMITTED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.PublisherRoutingKeys.SURVEY_RESULTS_REQUESTED;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.QueueConfigurationConstants.DEAD_LETTER_ROUTING_KEY;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.QueueConfigurationConstants.X_DEAD_LETTER_EXCHANGE;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.QueueConfigurationConstants.X_DEAD_LETTER_ROUTING_KEY;
import static it.unimol.microserviceassessmentfeedback.config.rabbitmq.constants.QueueConfigurationConstants.X_MESSAGE_TTL;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione dell'infrastruttura RabbitMQ. Definisce exchanges, code (publisher e consumer),
 * bindings e dead letter queue per la comunicazione asincrona tra microservizi.
 */
@Configuration
public class RabbitMqInfrastructureConfig {

  private final RabbitMqProperties properties;

  // ============ Costruttore ============

  /**
   * Costruttore con iniezione delle properties RabbitMQ.
   *
   * @param properties le configurazioni RabbitMQ caricate da application.properties
   */
  public RabbitMqInfrastructureConfig(RabbitMqProperties properties) {
    this.properties = properties;
  }

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  // ===================================================================
  //  EXCHANGES
  // ===================================================================

  /**
   * Crea il topic exchange principale per gli assessments.
   *
   * @return il topic exchange configurato
   */
  @Bean
  public TopicExchange assessmentsExchange() {
    return ExchangeBuilder
        .topicExchange(properties.getExchange().getAssessments())
        .durable(true)
        .build();
  }

  /**
   * Crea il direct exchange per le dead letter.
   *
   * @return il direct exchange per DLQ
   */
  @Bean
  public DirectExchange deadLetterExchange() {
    return ExchangeBuilder
        .directExchange(properties.getExchange().getDlx())
        .durable(true)
        .build();
  }

  // ===================================================================
  //  DEAD LETTER QUEUE
  // ===================================================================

  /**
   * Crea la coda dead letter per i messaggi non elaborati.
   *
   * @return la dead letter queue
   */
  @Bean
  public Queue deadLetterQueue() {
    return QueueBuilder
        .durable(properties.getQueue().getDlq())
        .build();
  }

  /**
   * Binding della dead letter queue al suo exchange.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding deadLetterBinding() {
    return BindingBuilder
        .bind(deadLetterQueue())
        .to(deadLetterExchange())
        .with(DEAD_LETTER_ROUTING_KEY);
  }

  // ===================================================================
  //  PUBLISHER QUEUES - ASSESSMENT
  // ===================================================================

  /**
   * Coda per gli eventi di creazione assessment.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue assessmentCreatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getAssessment().getCreated());
  }

  /**
   * Coda per gli eventi di aggiornamento assessment.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue assessmentUpdatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getAssessment().getUpdated());
  }

  /**
   * Coda per gli eventi di cancellazione assessment.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue assessmentDeletedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getAssessment().getDeleted());
  }

  /**
   * Binding per assessment.created.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding assessmentCreatedBinding() {
    return BindingBuilder
        .bind(assessmentCreatedQueue())
        .to(assessmentsExchange())
        .with(ASSESSMENT_CREATED);
  }

  /**
   * Binding per assessment.updated.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding assessmentUpdatedBinding() {
    return BindingBuilder
        .bind(assessmentUpdatedQueue())
        .to(assessmentsExchange())
        .with(ASSESSMENT_UPDATED);
  }

  /**
   * Binding per assessment.deleted.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding assessmentDeletedBinding() {
    return BindingBuilder
        .bind(assessmentDeletedQueue())
        .to(assessmentsExchange())
        .with(ASSESSMENT_DELETED);
  }

  // ===================================================================
  //  PUBLISHER QUEUES - FEEDBACK
  // ===================================================================

  /**
   * Coda per gli eventi di creazione feedback.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue feedbackCreatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getFeedback().getCreated());
  }

  /**
   * Coda per gli eventi di aggiornamento feedback.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue feedbackUpdatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getFeedback().getUpdated());
  }

  /**
   * Coda per gli eventi di cancellazione feedback.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue feedbackDeletedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getFeedback().getDeleted());
  }

  /**
   * Binding per feedback.created.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding feedbackCreatedBinding() {
    return BindingBuilder
        .bind(feedbackCreatedQueue())
        .to(assessmentsExchange())
        .with(FEEDBACK_CREATED);
  }

  /**
   * Binding per feedback.updated.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding feedbackUpdatedBinding() {
    return BindingBuilder
        .bind(feedbackUpdatedQueue())
        .to(assessmentsExchange())
        .with(FEEDBACK_UPDATED);
  }

  /**
   * Binding per feedback.deleted.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding feedbackDeletedBinding() {
    return BindingBuilder
        .bind(feedbackDeletedQueue())
        .to(assessmentsExchange())
        .with(FEEDBACK_DELETED);
  }

  // ===================================================================
  //  PUBLISHER QUEUES - SURVEY
  // ===================================================================

  /**
   * Coda per gli eventi di completamento survey.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue surveyCompletedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getSurvey().getCompleted());
  }

  /**
   * Coda per gli eventi di invio singola risposta survey.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue surveyResponseSubmittedQueue() {
    return createDurableQueueWithDlx(
        properties.getQueue().getSurvey().getResponse().getSubmitted());
  }

  /**
   * Coda per gli eventi di invio multiplo risposte survey.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue surveyResponsesBulkSubmittedQueue() {
    return createDurableQueueWithDlx(
        properties.getQueue().getSurvey().getResponse().getBulkSubmitted());
  }

  /**
   * Coda per le richieste di risultati survey.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue surveyResultsRequestedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getSurvey().getResultsRequested());
  }

  /**
   * Coda per le richieste di commenti survey.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue surveyCommentsRequestedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getSurvey().getCommentsRequested());
  }

  /**
   * Binding per survey.completed.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding surveyCompletedBinding() {
    return BindingBuilder
        .bind(surveyCompletedQueue())
        .to(assessmentsExchange())
        .with(SURVEY_COMPLETED);
  }

  /**
   * Binding per survey.response.submitted.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding surveyResponseSubmittedBinding() {
    return BindingBuilder
        .bind(surveyResponseSubmittedQueue())
        .to(assessmentsExchange())
        .with(SURVEY_RESPONSE_SUBMITTED);
  }

  /**
   * Binding per survey.responses.bulk.submitted.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding surveyResponsesBulkSubmittedBinding() {
    return BindingBuilder
        .bind(surveyResponsesBulkSubmittedQueue())
        .to(assessmentsExchange())
        .with(SURVEY_RESPONSES_BULK_SUBMITTED);
  }

  /**
   * Binding per survey.results.requested.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding surveyResultsRequestedBinding() {
    return BindingBuilder
        .bind(surveyResultsRequestedQueue())
        .to(assessmentsExchange())
        .with(SURVEY_RESULTS_REQUESTED);
  }

  /**
   * Binding per survey.comments.requested.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding surveyCommentsRequestedBinding() {
    return BindingBuilder
        .bind(surveyCommentsRequestedQueue())
        .to(assessmentsExchange())
        .with(SURVEY_COMMENTS_REQUESTED);
  }

  /**
   * Binding per user.created.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding userCreatedBinding() {
    return BindingBuilder
        .bind(userCreatedQueue())
        .to(assessmentsExchange())
        .with(USER_CREATED);
  }

  /**
   * Binding per user.updated.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding userUpdatedBinding() {
    return BindingBuilder
        .bind(userUpdatedQueue())
        .to(assessmentsExchange())
        .with(USER_UPDATED);
  }

  /**
   * Binding per user.deleted.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding userDeletedBinding() {
    return BindingBuilder
        .bind(userDeletedQueue())
        .to(assessmentsExchange())
        .with(USER_DELETED);
  }

  /**
   * Binding per role.assigned.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding roleAssignedBinding() {
    return BindingBuilder
        .bind(roleAssignedQueue())
        .to(assessmentsExchange())
        .with(ROLE_ASSIGNED);
  }

  // ===================================================================
  //  CONSUMERS QUEUES
  // ===================================================================

  /**
   * Coda consumer per assignment.submitted.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue assignmentSubmittedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getAssignmentSubmitted());
  }

  /**
   * Coda consumer per assignment.created.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue assignmentCreatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getAssignmentCreated());
  }

  /**
   * Coda consumer per assignment.updated.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue assignmentUpdatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getAssignmentUpdated());
  }

  /**
   * Coda consumer per exam.completed.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue examCompletedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getExamCompleted());
  }

  /**
   * Coda consumer per exam.grade.registered.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue examGradeRegisteredQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getExamGradeRegistered());
  }

  /**
   * Coda consumer per course.created.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue courseCreatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getCourseCreated());
  }

  /**
   * Coda consumer per course.deleted.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue courseDeletedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getCourseDeleted());
  }

  /**
   * Coda consumer per teacher.created.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue teacherCreatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getTeacherCreated());
  }

  /**
   * Coda consumer per student.created.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue studentCreatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getStudentCreated());
  }

  /**
   * Binding consumer per assignment.submitted.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding assignmentSubmittedBinding() {
    return BindingBuilder
        .bind(assignmentSubmittedQueue())
        .to(assessmentsExchange())
        .with(ASSIGNMENT_SUBMITTED);
  }

  /**
   * Binding consumer per assignment.created.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding assignmentCreatedBinding() {
    return BindingBuilder
        .bind(assignmentCreatedQueue())
        .to(assessmentsExchange())
        .with(ASSIGNMENT_CREATED);
  }

  /**
   * Binding consumer per assignment.updated.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding assignmentUpdatedBinding() {
    return BindingBuilder
        .bind(assignmentUpdatedQueue())
        .to(assessmentsExchange())
        .with(ASSIGNMENT_UPDATED);
  }

  /**
   * Binding consumer per exam.completed.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding examCompletedBinding() {
    return BindingBuilder
        .bind(examCompletedQueue())
        .to(assessmentsExchange())
        .with(EXAM_COMPLETED);
  }

  /**
   * Binding consumer per exam.grade.registered.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding examGradeRegisteredBinding() {
    return BindingBuilder
        .bind(examGradeRegisteredQueue())
        .to(assessmentsExchange())
        .with(EXAM_GRADE_REGISTERED);
  }

  /**
   * Binding consumer per course.created.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding courseCreatedBinding() {
    return BindingBuilder
        .bind(courseCreatedQueue())
        .to(assessmentsExchange())
        .with(COURSE_CREATED);
  }

  /**
   * Binding consumer per course.deleted.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding courseDeletedBinding() {
    return BindingBuilder
        .bind(courseDeletedQueue())
        .to(assessmentsExchange())
        .with(COURSE_DELETED);
  }

  /**
   * Binding consumer per teacher.created.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding teacherCreatedBinding() {
    return BindingBuilder
        .bind(teacherCreatedQueue())
        .to(assessmentsExchange())
        .with(TEACHER_CREATED);
  }

  /**
   * Binding consumer per student.created.
   *
   * @return il binding configurato
   */
  @Bean
  public Binding studentCreatedBinding() {
    return BindingBuilder
        .bind(studentCreatedQueue())
        .to(assessmentsExchange())
        .with(STUDENT_CREATED);
  }

  /**
   * Coda consumer per user.created.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue userCreatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getUserCreated());
  }

  /**
   * Coda consumer per user.updated.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue userUpdatedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getUserUpdated());
  }

  /**
   * Coda consumer per user.deleted.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue userDeletedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getUserDeleted());
  }

  /**
   * Coda consumer per role.assigned.
   *
   * @return la coda configurata
   */
  @Bean
  public Queue roleAssignedQueue() {
    return createDurableQueueWithDlx(properties.getQueue().getRoleAssigned());
  }

  /**
   * Crea una coda durabile con configurazione dead letter exchange.
   *
   * @param queueName il nome della coda da creare
   * @return la coda configurata con DLX, TTL e routing key
   */
  private Queue createDurableQueueWithDlx(String queueName) {
    return QueueBuilder
        .durable(queueName)
        .withArgument(X_DEAD_LETTER_EXCHANGE, properties.getExchange().getDlx())
        .withArgument(X_DEAD_LETTER_ROUTING_KEY, DEAD_LETTER_ROUTING_KEY)
        .withArgument(X_MESSAGE_TTL, properties.getMessage().getTtl())
        .build();
  }
}