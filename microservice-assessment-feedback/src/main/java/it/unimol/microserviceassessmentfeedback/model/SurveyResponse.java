package it.unimol.microserviceassessmentfeedback.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/**
 * Entità JPA che rappresenta una risposta a un questionario di valutazione docente.
 * Contiene sia valutazioni numeriche che commenti testuali per ogni domanda.
 */
@Entity
@Table(name = "survey_responses")
public class SurveyResponse {

  @Id
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_id", nullable = false)
  private TeacherSurvey survey;

  @Column(name = "student_id")
  private String studentId;

  @Column(name = "question_id", nullable = false)
  private String questionId;

  @Column(name = "numeric_rating")
  private Integer numericRating;

  @Column(name = "text_comment", length = 1000)
  private String textComment;

  @Column(name = "submission_date", nullable = false)
  private LocalDateTime submissionDate;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public SurveyResponse() {
  }

  /**
   * Costruttore con tutti i parametri.
   *
   * @param id l'ID univoco della risposta
   * @param survey il questionario associato
   * @param studentId l'ID dello studente
   * @param questionId l'ID della domanda
   * @param numericRating la valutazione numerica
   * @param textComment il commento testuale
   * @param submissionDate la data di invio
   * @param createdAt la data di creazione
   * @param updatedAt la data di ultimo aggiornamento
   */
  public SurveyResponse(String id, TeacherSurvey survey, String studentId, String questionId,
      Integer numericRating, String textComment, LocalDateTime submissionDate,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.survey = survey;
    this.studentId = studentId;
    this.questionId = questionId;
    this.numericRating = numericRating;
    this.textComment = textComment;
    this.submissionDate = submissionDate;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // ============ Metodi Override ============

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SurveyResponse that)) {
      return false;
    }
    return Objects.equals(id, that.id)
        && Objects.equals(survey, that.survey)
        && Objects.equals(studentId, that.studentId)
        && Objects.equals(questionId, that.questionId)
        && Objects.equals(numericRating, that.numericRating)
        && Objects.equals(textComment, that.textComment)
        && Objects.equals(submissionDate, that.submissionDate)
        && Objects.equals(createdAt, that.createdAt)
        && Objects.equals(updatedAt, that.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, survey, studentId, questionId, numericRating,
        textComment, submissionDate, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    return "SurveyResponse{"
        + "id='" + id + '\''
        + ", survey=" + survey
        + ", studentId='" + studentId + '\''
        + ", questionId='" + questionId + '\''
        + ", numericRating=" + numericRating
        + ", textComment='" + textComment + '\''
        + ", submissionDate=" + submissionDate
        + ", createdAt=" + createdAt
        + ", updatedAt=" + updatedAt
        + '}';
  }

  // ============ Getters & Setters & Bool ============

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public TeacherSurvey getSurvey() {
    return survey;
  }

  public void setSurvey(TeacherSurvey survey) {
    this.survey = survey;
  }

  public String getStudentId() {
    return studentId;
  }

  public void setStudentId(String studentId) {
    this.studentId = studentId;
  }

  public String getQuestionId() {
    return questionId;
  }

  public void setQuestionId(String questionId) {
    this.questionId = questionId;
  }

  public Integer getNumericRating() {
    return numericRating;
  }

  public void setNumericRating(Integer numericRating) {
    this.numericRating = numericRating;
  }

  public String getTextComment() {
    return textComment;
  }

  public void setTextComment(String textComment) {
    this.textComment = textComment;
  }

  public LocalDateTime getSubmissionDate() {
    return submissionDate;
  }

  public void setSubmissionDate(LocalDateTime submissionDate) {
    this.submissionDate = submissionDate;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  // ============ Metodi di Classe ============

  /**
   * Crea un nuovo builder per costruire un'istanza di SurveyResponse.
   *
   * @return un nuovo builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Callback JPA eseguito prima del persist.
   * Genera un ID UUID se non presente e imposta le timestamp.
   */
  @PrePersist
  protected void onCreate() {
    if (id == null || id.isEmpty()) {
      id = UUID.randomUUID().toString();
    }
    createdAt = LocalDateTime.now(ZoneId.systemDefault());
    updatedAt = LocalDateTime.now(ZoneId.systemDefault());
  }

  /**
   * Callback JPA eseguito prima dell'update.
   * Aggiorna il timestamp di ultimo aggiornamento.
   */
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now(ZoneId.systemDefault());
  }

  /**
   * Builder per la costruzione fluente di istanze SurveyResponse.
   */
  public static class Builder {

    private String id;
    private TeacherSurvey survey;
    private String studentId;
    private String questionId;
    private Integer numericRating;
    private String textComment;
    private LocalDateTime submissionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Imposta l'ID.
     *
     * @param id l'ID
     * @return il builder
     */
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    /**
     * Imposta il questionario associato.
     *
     * @param survey il questionario
     * @return il builder
     */
    public Builder survey(TeacherSurvey survey) {
      this.survey = survey;
      return this;
    }

    /**
     * Imposta l'ID dello studente.
     *
     * @param studentId l'ID dello studente
     * @return il builder
     */
    public Builder studentId(String studentId) {
      this.studentId = studentId;
      return this;
    }

    /**
     * Imposta l'ID della domanda.
     *
     * @param questionId l'ID della domanda
     * @return il builder
     */
    public Builder questionId(String questionId) {
      this.questionId = questionId;
      return this;
    }

    /**
     * Imposta la valutazione numerica.
     *
     * @param numericRating la valutazione numerica
     * @return il builder
     */
    public Builder numericRating(Integer numericRating) {
      this.numericRating = numericRating;
      return this;
    }

    /**
     * Imposta il commento testuale.
     *
     * @param textComment il commento testuale
     * @return il builder
     */
    public Builder textComment(String textComment) {
      this.textComment = textComment;
      return this;
    }

    /**
     * Imposta la data di invio.
     *
     * @param submissionDate la data di invio
     * @return il builder
     */
    public Builder submissionDate(LocalDateTime submissionDate) {
      this.submissionDate = submissionDate;
      return this;
    }

    /**
     * Imposta la data di creazione.
     *
     * @param createdAt la data di creazione
     * @return il builder
     */
    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    /**
     * Imposta la data di aggiornamento.
     *
     * @param updatedAt la data di aggiornamento
     * @return il builder
     */
    public Builder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    /**
     * Costruisce l'istanza di SurveyResponse.
     *
     * @return l'istanza di SurveyResponse costruita
     */
    public SurveyResponse build() {
      return new SurveyResponse(id, survey, studentId, questionId, numericRating,
          textComment, submissionDate, createdAt, updatedAt);
    }
  }
}