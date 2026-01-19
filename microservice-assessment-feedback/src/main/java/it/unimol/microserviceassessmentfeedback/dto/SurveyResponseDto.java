package it.unimol.microserviceassessmentfeedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO (Data Transfer Object) per le risposte ai questionari.
 * Rappresenta una risposta fornita da uno studente a una domanda specifica di un questionario.
 */
@Schema(description = "DTO per le Risposte ai Questionari")
public class SurveyResponseDto {

  @Schema(description = "ID Risposta Questionario", example = "uuid-risposta-123", accessMode =
      Schema.AccessMode.READ_ONLY)
  private String id;

  @Schema(description = "ID del Questionario a cui appartiene la risposta", example = "uuid"
      + "-questionario-456", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "SurveyID richiesto")
  private String surveyId;

  @Schema(description = "ID dello studente che ha fornito la risposta", example = "uuid-studente"
      + "-789")
  private String studentId;

  @Schema(description = "ID della domanda a cui si riferisce la risposta", example = "uuid"
      + "-domanda-101", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "QuestionID richiesto")
  private String questionId;

  @Schema(description = "Valutazione numerica (tra 1 e 5)", example = "4")
  @Min(value = 1, message = "La valutazione deve essere tra 1 e 5")
  @Max(value = 5, message = "La valutazione deve essere tra 1 e 5")
  private Integer numericRating;

  @Schema(description = "Commento testuale opzionale alla risposta", example = "Il corso è stato "
      + "molto interessante e ben strutturato.", maxLength = 1000)
  @Size(max = 1000, message = "I commenti non possono superare i 1000 caratteri")
  private String textComment;

  @Schema(description = "Data di sottomissione della risposta", example = "2024-03-15T14:00:00",
      accessMode = Schema.AccessMode.READ_ONLY)
  private LocalDateTime submissionDate;


  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public SurveyResponseDto() {
  }

  /**
   * Costruttore con tutti i parametri.
   *
   * @param id ID della risposta
   * @param surveyId ID del questionario
   * @param studentId ID dello studente
   * @param questionId ID della domanda
   * @param numericRating Valutazione numerica (1-5)
   * @param textComment Commento testuale opzionale
   * @param submissionDate Data di sottomissione
   */
  public SurveyResponseDto(String id, String surveyId, String studentId, String questionId,
      Integer numericRating, String textComment, LocalDateTime submissionDate) {
    this.id = id;
    this.surveyId = surveyId;
    this.studentId = studentId;
    this.questionId = questionId;
    this.numericRating = numericRating;
    this.textComment = textComment;
    this.submissionDate = submissionDate;
  }

  // ============ Metodi Override ============
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SurveyResponseDto that)) {
      return false;
    }
    return Objects.equals(id, that.id)
        && Objects.equals(surveyId, that.surveyId)
        && Objects.equals(studentId, that.studentId)
        && Objects.equals(questionId, that.questionId)
        && Objects.equals(numericRating, that.numericRating)
        && Objects.equals(textComment, that.textComment)
        && Objects.equals(submissionDate, that.submissionDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, surveyId, studentId, questionId,
        numericRating, textComment, submissionDate);
  }

  @Override
  public String toString() {
    return "SurveyResponseDTO{"
        + "id='" + id + '\''
        + ", surveyId='" + surveyId + '\''
        + ", studentId='" + studentId + '\''
        + ", questionId='" + questionId + '\''
        + ", numericRating=" + numericRating
        + ", textComment='" + textComment + '\''
        + ", submissionDate=" + submissionDate
        + '}';
  }

  // ============ Getters & Setters & Bool ============
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSurveyId() {
    return surveyId;
  }

  public void setSurveyId(String surveyId) {
    this.surveyId = surveyId;
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

  // ============ Metodi di Classe ============

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder per la costruzione di oggetti SurveyResponseDto.
   */
  public static class Builder {

    private String id;
    private String surveyId;
    private String studentId;
    private String questionId;
    private Integer numericRating;
    private String textComment;
    private LocalDateTime submissionDate;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder surveyId(String surveyId) {
      this.surveyId = surveyId;
      return this;
    }

    public Builder studentId(String studentId) {
      this.studentId = studentId;
      return this;
    }

    public Builder questionId(String questionId) {
      this.questionId = questionId;
      return this;
    }

    public Builder numericRating(Integer numericRating) {
      this.numericRating = numericRating;
      return this;
    }

    public Builder textComment(String textComment) {
      this.textComment = textComment;
      return this;
    }

    public Builder submissionDate(LocalDateTime submissionDate) {
      this.submissionDate = submissionDate;
      return this;
    }

    public SurveyResponseDto build() {
      return new SurveyResponseDto(id, surveyId, studentId, questionId,
          numericRating, textComment, submissionDate);
    }
  }
}