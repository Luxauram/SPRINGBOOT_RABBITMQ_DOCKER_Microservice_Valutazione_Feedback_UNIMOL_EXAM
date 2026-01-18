package it.unimol.microserviceassessmentfeedback.config.rabbitmq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties per la configurazione RabbitMQ. Mappa le configurazioni dal file
 * application.properties con prefisso "rabbitmq". Contiene configurazioni per exchanges, code e
 * messaggi.
 */
@Component
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMqProperties {

  private Exchange exchange = new Exchange();
  private Queue queue = new Queue();
  private Message message = new Message();

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  /**
   * Ottiene la configurazione degli exchange.
   *
   * @return la configurazione degli exchange
   */
  public Exchange getExchange() {
    return exchange;
  }

  /**
   * Imposta la configurazione degli exchange.
   *
   * @param exchange la configurazione degli exchange
   */
  public void setExchange(Exchange exchange) {
    this.exchange = exchange;
  }

  /**
   * Ottiene la configurazione delle code.
   *
   * @return la configurazione delle code
   */
  public Queue getQueue() {
    return queue;
  }

  /**
   * Imposta la configurazione delle code.
   *
   * @param queue la configurazione delle code
   */
  public void setQueue(Queue queue) {
    this.queue = queue;
  }

  /**
   * Ottiene la configurazione dei messaggi.
   *
   * @return la configurazione dei messaggi
   */
  public Message getMessage() {
    return message;
  }

  /**
   * Imposta la configurazione dei messaggi.
   *
   * @param message la configurazione dei messaggi
   */
  public void setMessage(Message message) {
    this.message = message;
  }

  // ============ Metodi di Classe ============

  /**
   * Configurazione degli exchange RabbitMQ.
   */
  public static class Exchange {

    private String assessments;
    private String dlx = "unimol.dlx";

    /**
     * Ottiene il nome dell'exchange per gli assessments.
     *
     * @return il nome dell'exchange
     */
    public String getAssessments() {
      return assessments;
    }

    /**
     * Imposta il nome dell'exchange per gli assessments.
     *
     * @param assessments il nome dell'exchange
     */
    public void setAssessments(String assessments) {
      this.assessments = assessments;
    }

    /**
     * Ottiene il nome del dead letter exchange.
     *
     * @return il nome del DLX
     */
    public String getDlx() {
      return dlx;
    }

    /**
     * Imposta il nome del dead letter exchange.
     *
     * @param dlx il nome del DLX
     */
    public void setDlx(String dlx) {
      this.dlx = dlx;
    }
  }

  /**
   * Configurazione delle code RabbitMQ.
   */
  public static class Queue {

    private String dlq = "assessments.dlq";
    private Assessment assessment = new Assessment();
    private Feedback feedback = new Feedback();
    private Survey survey = new Survey();

    // CONSUMERS QUEUES - Default Values
    private String assignmentSubmitted = "assignment.submitted";
    private String assignmentCreated = "assignment.created.queue";
    private String assignmentUpdated = "assignment.updated.queue";
    private String examCompleted = "exam.completed";
    private String examGradeRegistered = "exam.grade.registered";
    private String courseCreated = "course.created";
    private String courseDeleted = "course.deleted";
    private String teacherCreated = "teacher.created";
    private String studentCreated = "student.created";
    private String userCreated = "user.created.queue";
    private String userUpdated = "user.updated.queue";
    private String userDeleted = "user.deleted.queue";
    private String roleAssigned = "role.assigned.queue";

    // Getters e Setters - Queue Class Main

    /**
     * Ottiene il nome della dead letter queue.
     *
     * @return il nome della DLQ
     */
    public String getDlq() {
      return dlq;
    }

    /**
     * Imposta il nome della dead letter queue.
     *
     * @param dlq il nome della DLQ
     */
    public void setDlq(String dlq) {
      this.dlq = dlq;
    }

    /**
     * Ottiene la configurazione delle code assessment.
     *
     * @return la configurazione assessment
     */
    public Assessment getAssessment() {
      return assessment;
    }

    /**
     * Imposta la configurazione delle code assessment.
     *
     * @param assessment la configurazione assessment
     */
    public void setAssessment(Assessment assessment) {
      this.assessment = assessment;
    }

    /**
     * Ottiene la configurazione delle code feedback.
     *
     * @return la configurazione feedback
     */
    public Feedback getFeedback() {
      return feedback;
    }

    /**
     * Imposta la configurazione delle code feedback.
     *
     * @param feedback la configurazione feedback
     */
    public void setFeedback(Feedback feedback) {
      this.feedback = feedback;
    }

    /**
     * Ottiene la configurazione delle code survey.
     *
     * @return la configurazione survey
     */
    public Survey getSurvey() {
      return survey;
    }

    /**
     * Imposta la configurazione delle code survey.
     *
     * @param survey la configurazione survey
     */
    public void setSurvey(Survey survey) {
      this.survey = survey;
    }

    /**
     * Ottiene il nome della coda assignment submitted.
     *
     * @return il nome della coda
     */
    public String getAssignmentSubmitted() {
      return assignmentSubmitted;
    }

    /**
     * Imposta il nome della coda assignment submitted.
     *
     * @param assignmentSubmitted il nome della coda
     */
    public void setAssignmentSubmitted(String assignmentSubmitted) {
      this.assignmentSubmitted = assignmentSubmitted;
    }

    /**
     * Ottiene il nome della coda assignment created.
     *
     * @return il nome della coda
     */
    public String getAssignmentCreated() {
      return assignmentCreated;
    }

    /**
     * Imposta il nome della coda assignment created.
     *
     * @param assignmentCreated il nome della coda
     */
    public void setAssignmentCreated(String assignmentCreated) {
      this.assignmentCreated = assignmentCreated;
    }

    /**
     * Ottiene il nome della coda assignment updated.
     *
     * @return il nome della coda
     */
    public String getAssignmentUpdated() {
      return assignmentUpdated;
    }

    /**
     * Imposta il nome della coda assignment updated.
     *
     * @param assignmentUpdated il nome della coda
     */
    public void setAssignmentUpdated(String assignmentUpdated) {
      this.assignmentUpdated = assignmentUpdated;
    }

    /**
     * Ottiene il nome della coda exam completed.
     *
     * @return il nome della coda
     */
    public String getExamCompleted() {
      return examCompleted;
    }

    /**
     * Imposta il nome della coda exam completed.
     *
     * @param examCompleted il nome della coda
     */
    public void setExamCompleted(String examCompleted) {
      this.examCompleted = examCompleted;
    }

    /**
     * Ottiene il nome della coda exam grade registered.
     *
     * @return il nome della coda
     */
    public String getExamGradeRegistered() {
      return examGradeRegistered;
    }

    /**
     * Imposta il nome della coda exam grade registered.
     *
     * @param examGradeRegistered il nome della coda
     */
    public void setExamGradeRegistered(String examGradeRegistered) {
      this.examGradeRegistered = examGradeRegistered;
    }

    /**
     * Ottiene il nome della coda course created.
     *
     * @return il nome della coda
     */
    public String getCourseCreated() {
      return courseCreated;
    }

    /**
     * Imposta il nome della coda course created.
     *
     * @param courseCreated il nome della coda
     */
    public void setCourseCreated(String courseCreated) {
      this.courseCreated = courseCreated;
    }

    /**
     * Ottiene il nome della coda course deleted.
     *
     * @return il nome della coda
     */
    public String getCourseDeleted() {
      return courseDeleted;
    }

    /**
     * Imposta il nome della coda course deleted.
     *
     * @param courseDeleted il nome della coda
     */
    public void setCourseDeleted(String courseDeleted) {
      this.courseDeleted = courseDeleted;
    }

    /**
     * Ottiene il nome della coda teacher created.
     *
     * @return il nome della coda
     */
    public String getTeacherCreated() {
      return teacherCreated;
    }

    /**
     * Imposta il nome della coda teacher created.
     *
     * @param teacherCreated il nome della coda
     */
    public void setTeacherCreated(String teacherCreated) {
      this.teacherCreated = teacherCreated;
    }

    /**
     * Ottiene il nome della coda student created.
     *
     * @return il nome della coda
     */
    public String getStudentCreated() {
      return studentCreated;
    }

    /**
     * Imposta il nome della coda student created.
     *
     * @param studentCreated il nome della coda
     */
    public void setStudentCreated(String studentCreated) {
      this.studentCreated = studentCreated;
    }

    /**
     * Ottiene il nome della coda user created.
     *
     * @return il nome della coda
     */
    public String getUserCreated() {
      return userCreated;
    }

    /**
     * Imposta il nome della coda user created.
     *
     * @param userCreated il nome della coda
     */
    public void setUserCreated(String userCreated) {
      this.userCreated = userCreated;
    }

    /**
     * Ottiene il nome della coda user updated.
     *
     * @return il nome della coda
     */
    public String getUserUpdated() {
      return userUpdated;
    }

    /**
     * Imposta il nome della coda user updated.
     *
     * @param userUpdated il nome della coda
     */
    public void setUserUpdated(String userUpdated) {
      this.userUpdated = userUpdated;
    }

    /**
     * Ottiene il nome della coda user deleted.
     *
     * @return il nome della coda
     */
    public String getUserDeleted() {
      return userDeleted;
    }

    /**
     * Imposta il nome della coda user deleted.
     *
     * @param userDeleted il nome della coda
     */
    public void setUserDeleted(String userDeleted) {
      this.userDeleted = userDeleted;
    }

    /**
     * Ottiene il nome della coda role assigned.
     *
     * @return il nome della coda
     */
    public String getRoleAssigned() {
      return roleAssigned;
    }

    /**
     * Imposta il nome della coda role assigned.
     *
     * @param roleAssigned il nome della coda
     */
    public void setRoleAssigned(String roleAssigned) {
      this.roleAssigned = roleAssigned;
    }

    /**
     * Configurazione delle code assessment.
     */
    public static class Assessment {

      private String created;
      private String updated;
      private String deleted;

      /**
       * Ottiene il nome della coda assessment created.
       *
       * @return il nome della coda
       */
      public String getCreated() {
        return created;
      }

      /**
       * Imposta il nome della coda assessment created.
       *
       * @param created il nome della coda
       */
      public void setCreated(String created) {
        this.created = created;
      }

      /**
       * Ottiene il nome della coda assessment updated.
       *
       * @return il nome della coda
       */
      public String getUpdated() {
        return updated;
      }

      /**
       * Imposta il nome della coda assessment updated.
       *
       * @param updated il nome della coda
       */
      public void setUpdated(String updated) {
        this.updated = updated;
      }

      /**
       * Ottiene il nome della coda assessment deleted.
       *
       * @return il nome della coda
       */
      public String getDeleted() {
        return deleted;
      }

      /**
       * Imposta il nome della coda assessment deleted.
       *
       * @param deleted il nome della coda
       */
      public void setDeleted(String deleted) {
        this.deleted = deleted;
      }
    }

    /**
     * Configurazione delle code feedback.
     */
    public static class Feedback {

      private String created;
      private String updated;
      private String deleted;

      /**
       * Ottiene il nome della coda feedback created.
       *
       * @return il nome della coda
       */
      public String getCreated() {
        return created;
      }

      /**
       * Imposta il nome della coda feedback created.
       *
       * @param created il nome della coda
       */
      public void setCreated(String created) {
        this.created = created;
      }

      /**
       * Ottiene il nome della coda feedback updated.
       *
       * @return il nome della coda
       */
      public String getUpdated() {
        return updated;
      }

      /**
       * Imposta il nome della coda feedback updated.
       *
       * @param updated il nome della coda
       */
      public void setUpdated(String updated) {
        this.updated = updated;
      }

      /**
       * Ottiene il nome della coda feedback deleted.
       *
       * @return il nome della coda
       */
      public String getDeleted() {
        return deleted;
      }

      /**
       * Imposta il nome della coda feedback deleted.
       *
       * @param deleted il nome della coda
       */
      public void setDeleted(String deleted) {
        this.deleted = deleted;
      }
    }

    /**
     * Configurazione delle code survey.
     */
    public static class Survey {

      private String completed;
      private Response response = new Response();
      private String resultsRequested = "survey.results.requested";
      private String commentsRequested = "survey.comments.requested";

      /**
       * Ottiene il nome della coda survey completed.
       *
       * @return il nome della coda
       */
      public String getCompleted() {
        return completed;
      }

      /**
       * Imposta il nome della coda survey completed.
       *
       * @param completed il nome della coda
       */
      public void setCompleted(String completed) {
        this.completed = completed;
      }

      /**
       * Ottiene la configurazione delle code response.
       *
       * @return la configurazione response
       */
      public Response getResponse() {
        return response;
      }

      /**
       * Imposta la configurazione delle code response.
       *
       * @param response la configurazione response
       */
      public void setResponse(Response response) {
        this.response = response;
      }

      /**
       * Ottiene il nome della coda survey results requested.
       *
       * @return il nome della coda
       */
      public String getResultsRequested() {
        return resultsRequested;
      }

      /**
       * Imposta il nome della coda survey results requested.
       *
       * @param resultsRequested il nome della coda
       */
      public void setResultsRequested(String resultsRequested) {
        this.resultsRequested = resultsRequested;
      }

      /**
       * Ottiene il nome della coda survey comments requested.
       *
       * @return il nome della coda
       */
      public String getCommentsRequested() {
        return commentsRequested;
      }

      /**
       * Imposta il nome della coda survey comments requested.
       *
       * @param commentsRequested il nome della coda
       */
      public void setCommentsRequested(String commentsRequested) {
        this.commentsRequested = commentsRequested;
      }

      /**
       * Configurazione delle code survey response.
       */
      public static class Response {

        private String submitted;
        private String bulkSubmitted = "survey.responses.bulk.submitted";

        /**
         * Ottiene il nome della coda survey response submitted.
         *
         * @return il nome della coda
         */
        public String getSubmitted() {
          return submitted;
        }

        /**
         * Imposta il nome della coda survey response submitted.
         *
         * @param submitted il nome della coda
         */
        public void setSubmitted(String submitted) {
          this.submitted = submitted;
        }

        /**
         * Ottiene il nome della coda survey responses bulk submitted.
         *
         * @return il nome della coda
         */
        public String getBulkSubmitted() {
          return bulkSubmitted;
        }

        /**
         * Imposta il nome della coda survey responses bulk submitted.
         *
         * @param bulkSubmitted il nome della coda
         */
        public void setBulkSubmitted(String bulkSubmitted) {
          this.bulkSubmitted = bulkSubmitted;
        }
      }
    }
  }

  /**
   * Configurazione dei messaggi RabbitMQ.
   */
  public static class Message {

    private int ttl = 86400000;

    /**
     * Ottiene il TTL (time to live) dei messaggi in millisecondi.
     *
     * @return il TTL in millisecondi
     */
    public int getTtl() {
      return ttl;
    }

    /**
     * Imposta il TTL (time to live) dei messaggi in millisecondi.
     *
     * @param ttl il TTL in millisecondi
     */
    public void setTtl(int ttl) {
      this.ttl = ttl;
    }
  }
}