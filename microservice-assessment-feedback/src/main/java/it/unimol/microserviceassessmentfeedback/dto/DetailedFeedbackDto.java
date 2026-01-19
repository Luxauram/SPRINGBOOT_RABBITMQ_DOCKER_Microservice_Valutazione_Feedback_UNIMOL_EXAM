package it.unimol.microserviceassessmentfeedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.unimol.microserviceassessmentfeedback.enums.FeedbackCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * DTO (Data Transfer Object) per il feedback dettagliato.
 * Rappresenta un feedback associato a una valutazione con categoria, punti di forza
 * e aree di miglioramento.
 */
@Schema(description = "DTO sul Feedback")
public class DetailedFeedbackDto {

  @Schema(description = "ID Feedback", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
  private String id;

  @Schema(description = "ID della valutazione a cui appartiene il Feedback", example = "123",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "AssessmentId richiesto")
  private String assessmentId;

  @Schema(description = "Contenuto principale del testo di feedback", example = "Lo studente ha "
      + "dimostrato un'eccellente comprensione dei concetti fondamentali...", requiredMode =
      Schema.RequiredMode.REQUIRED, maxLength = 2000)
  @NotBlank(message = "FeedbackText richiesto")
  @Size(max = 2000, message = "Il testo del Feedback (feedbackText) non può superare i 2000 "
      + "caratteri")
  private String feedbackText;

  @Schema(description = "La categoria del Feedback. Valori consentiti: CONTENT, PRESENTATION, "
      + "CORRECTNESS, OTHER.",
      example = "POSITIVE",
      requiredMode = Schema.RequiredMode.REQUIRED,
      allowableValues = {"CONTENT", "PRESENTATION", "CORRECTNESS", "OTHER"})
  @NotNull(message = "Category richiesto")
  private FeedbackCategory category;

  @Schema(description = "Aree in cui lo studente ha mostrato punti di forza", example = "Forti "
      + "capacità analitiche, presentazione chiara, buon uso di esempi", maxLength = 1000)
  @Size(max = 1000, message = "I punti di forza (strengths) non possono superare i 1000 caratteri")
  private String strengths;

  @Schema(description = "Aree da migliorare", example = "Potrebbe migliorare la gestione del "
      + "tempo e fornire spiegazioni più dettagliate", maxLength = 1000)
  @Size(max = 1000, message = "Le aree di miglioramento (improvementAreas) non può superare i "
      + "1000 caratteri")
  private String improvementAreas;

  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public DetailedFeedbackDto() {
  }

  /**
   * Costruttore con tutti i parametri.
   *
   * @param id ID del feedback
   * @param assessmentId ID della valutazione associata
   * @param feedbackText Testo del feedback
   * @param category Categoria del feedback
   * @param strengths Punti di forza dello studente
   * @param improvementAreas Aree di miglioramento
   */
  public DetailedFeedbackDto(String id, String assessmentId, String feedbackText,
      FeedbackCategory category,
      String strengths, String improvementAreas) {
    this.id = id;
    this.assessmentId = assessmentId;
    this.feedbackText = feedbackText;
    this.category = category;
    this.strengths = strengths;
    this.improvementAreas = improvementAreas;
  }

  // ============ Metodi Override ============
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DetailedFeedbackDto that)) {
      return false;
    }
    return Objects.equals(id, that.id)
        && Objects.equals(assessmentId, that.assessmentId)
        && Objects.equals(feedbackText, that.feedbackText)
        && category == that.category
        && Objects.equals(strengths, that.strengths)
        && Objects.equals(improvementAreas, that.improvementAreas);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, assessmentId, feedbackText, category, strengths, improvementAreas);
  }

  @Override
  public String toString() {
    return "DetailedFeedbackDTO{"
        + "id='" + id + '\''
        + ", assessmentId='" + assessmentId + '\''
        + ", feedbackText='" + feedbackText + '\''
        + ", category=" + category
        + ", strengths='" + strengths + '\''
        + ", improvementAreas='" + improvementAreas + '\''
        + '}';
  }

  // ============ Getters & Setters & Bool ============
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAssessmentId() {
    return assessmentId;
  }

  public void setAssessmentId(String assessmentId) {
    this.assessmentId = assessmentId;
  }

  public String getFeedbackText() {
    return feedbackText;
  }

  public void setFeedbackText(String feedbackText) {
    this.feedbackText = feedbackText;
  }

  public FeedbackCategory getCategory() {
    return category;
  }

  public void setCategory(FeedbackCategory category) {
    this.category = category;
  }

  public String getStrengths() {
    return strengths;
  }

  public void setStrengths(String strengths) {
    this.strengths = strengths;
  }

  public String getImprovementAreas() {
    return improvementAreas;
  }

  public void setImprovementAreas(String improvementAreas) {
    this.improvementAreas = improvementAreas;
  }

  // ============ Metodi di Classe ============

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder per la costruzione di oggetti DetailedFeedbackDto.
   */
  public static class Builder {

    private String id;
    private String assessmentId;
    private String feedbackText;
    private FeedbackCategory category;
    private String strengths;
    private String improvementAreas;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder assessmentId(String assessmentId) {
      this.assessmentId = assessmentId;
      return this;
    }

    public Builder feedbackText(String feedbackText) {
      this.feedbackText = feedbackText;
      return this;
    }

    public Builder category(FeedbackCategory category) {
      this.category = category;
      return this;
    }

    public Builder strengths(String strengths) {
      this.strengths = strengths;
      return this;
    }

    public Builder improvementAreas(String improvementAreas) {
      this.improvementAreas = improvementAreas;
      return this;
    }

    public DetailedFeedbackDto build() {
      return new DetailedFeedbackDto(id, assessmentId, feedbackText, category, strengths,
          improvementAreas);
    }
  }
}