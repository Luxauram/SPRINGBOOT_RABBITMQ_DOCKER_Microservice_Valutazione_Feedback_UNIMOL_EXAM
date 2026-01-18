package it.unimol.microserviceassessmentfeedback.model;

import it.unimol.microserviceassessmentfeedback.enums.FeedbackCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entità JPA che rappresenta un feedback dettagliato su una valutazione.
 * Fornisce informazioni qualitative aggiuntive come punti di forza,
 * aree di miglioramento e categorizzazione del feedback.
 */
@Entity
@Table(name = "detailed_feedback")
public class DetailedFeedback {

  @Id
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assessment_id", nullable = false)
  private Assessment assessment;

  @Column(name = "feedback_text", length = 2000, nullable = false)
  private String feedbackText;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FeedbackCategory category;

  @Column(length = 1000)
  private String strengths;

  @Column(name = "improvement_areas", length = 1000)
  private String improvementAreas;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public DetailedFeedback() {
  }

  /**
   * Costruttore con tutti i parametri.
   *
   * @param id l'ID univoco del feedback
   * @param assessment la valutazione associata
   * @param feedbackText il testo del feedback
   * @param category la categoria del feedback
   * @param strengths i punti di forza
   * @param improvementAreas le aree di miglioramento
   * @param createdAt la data di creazione
   * @param updatedAt la data di ultimo aggiornamento
   */
  public DetailedFeedback(String id, Assessment assessment, String feedbackText,
      FeedbackCategory category, String strengths, String improvementAreas,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.assessment = assessment;
    this.feedbackText = feedbackText;
    this.category = category;
    this.strengths = strengths;
    this.improvementAreas = improvementAreas;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // ============ Metodi Override ============

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DetailedFeedback that = (DetailedFeedback) o;
    return Objects.equals(id, that.id)
        && Objects.equals(assessment, that.assessment)
        && Objects.equals(feedbackText, that.feedbackText)
        && category == that.category
        && Objects.equals(strengths, that.strengths)
        && Objects.equals(improvementAreas, that.improvementAreas)
        && Objects.equals(createdAt, that.createdAt)
        && Objects.equals(updatedAt, that.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, assessment, feedbackText, category,
        strengths, improvementAreas, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    return "DetailedFeedback{"
        + "id='" + id + '\''
        + ", assessment=" + assessment
        + ", feedbackText='" + feedbackText + '\''
        + ", category=" + category
        + ", strengths='" + strengths + '\''
        + ", improvementAreas='" + improvementAreas + '\''
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

  public Assessment getAssessment() {
    return assessment;
  }

  public void setAssessment(Assessment assessment) {
    this.assessment = assessment;
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
   * Crea un nuovo builder per costruire un'istanza di DetailedFeedback.
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
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  /**
   * Callback JPA eseguito prima dell'update.
   * Aggiorna il timestamp di ultimo aggiornamento.
   */
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  /**
   * Builder per la costruzione fluente di istanze DetailedFeedback.
   */
  public static class Builder {

    private String id;
    private Assessment assessment;
    private String feedbackText;
    private FeedbackCategory category;
    private String strengths;
    private String improvementAreas;
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
     * Imposta la valutazione associata.
     *
     * @param assessment la valutazione
     * @return il builder
     */
    public Builder assessment(Assessment assessment) {
      this.assessment = assessment;
      return this;
    }

    /**
     * Imposta il testo del feedback.
     *
     * @param feedbackText il testo del feedback
     * @return il builder
     */
    public Builder feedbackText(String feedbackText) {
      this.feedbackText = feedbackText;
      return this;
    }

    /**
     * Imposta la categoria del feedback.
     *
     * @param category la categoria
     * @return il builder
     */
    public Builder category(FeedbackCategory category) {
      this.category = category;
      return this;
    }

    /**
     * Imposta i punti di forza.
     *
     * @param strengths i punti di forza
     * @return il builder
     */
    public Builder strengths(String strengths) {
      this.strengths = strengths;
      return this;
    }

    /**
     * Imposta le aree di miglioramento.
     *
     * @param improvementAreas le aree di miglioramento
     * @return il builder
     */
    public Builder improvementAreas(String improvementAreas) {
      this.improvementAreas = improvementAreas;
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
     * Costruisce l'istanza di DetailedFeedback.
     *
     * @return l'istanza di DetailedFeedback costruita
     */
    public DetailedFeedback build() {
      return new DetailedFeedback(id, assessment, feedbackText, category,
          strengths, improvementAreas, createdAt, updatedAt);
    }
  }
}