package it.unimol.microserviceassessmentfeedback.model;

import it.unimol.microserviceassessmentfeedback.enums.ReferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entità JPA che rappresenta una valutazione.
 * Contiene informazioni su punteggio, studente, docente e riferimento
 * (esame o assignment) associato alla valutazione.
 */
@Entity
@Table(name = "assessments")
public class Assessment {

  @Id
  private String id;

  @Column(name = "reference_id", nullable = false)
  private String referenceId;

  @Enumerated(EnumType.STRING)
  @Column(name = "reference_type", nullable = false)
  private ReferenceType referenceType;

  @Column(name = "student_id", nullable = false)
  private String studentId;

  @Column(name = "teacher_id", nullable = false)
  private String teacherId;

  @Column(nullable = false)
  private Double score;

  @Column(name = "assessment_date", nullable = false)
  private LocalDateTime assessmentDate;

  @Column(length = 1000)
  private String notes;

  @Column(name = "course_id")
  private String courseId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public Assessment() {
  }

  /**
   * Costruttore con tutti i parametri.
   *
   * @param id l'ID univoco dell'assessment
   * @param referenceId l'ID del riferimento (esame o assignment)
   * @param referenceType il tipo di riferimento
   * @param studentId l'ID dello studente
   * @param teacherId l'ID del docente
   * @param score il punteggio della valutazione
   * @param assessmentDate la data della valutazione
   * @param notes note aggiuntive
   * @param courseId l'ID del corso
   * @param createdAt la data di creazione
   * @param updatedAt la data di ultimo aggiornamento
   */
  public Assessment(String id, String referenceId, ReferenceType referenceType, String studentId,
      String teacherId, Double score, LocalDateTime assessmentDate, String notes,
      String courseId, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.referenceId = referenceId;
    this.referenceType = referenceType;
    this.studentId = studentId;
    this.teacherId = teacherId;
    this.score = score;
    this.assessmentDate = assessmentDate;
    this.notes = notes;
    this.courseId = courseId;
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
    Assessment that = (Assessment) o;
    return Objects.equals(id, that.id)
        && Objects.equals(referenceId, that.referenceId)
        && referenceType == that.referenceType
        && Objects.equals(studentId, that.studentId)
        && Objects.equals(teacherId, that.teacherId)
        && Objects.equals(score, that.score)
        && Objects.equals(assessmentDate, that.assessmentDate)
        && Objects.equals(notes, that.notes)
        && Objects.equals(courseId, that.courseId)
        && Objects.equals(createdAt, that.createdAt)
        && Objects.equals(updatedAt, that.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, referenceId, referenceType, studentId, teacherId,
        score, assessmentDate, notes, courseId, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    return "Assessment{"
        + "id='" + id + '\''
        + ", referenceId='" + referenceId + '\''
        + ", referenceType=" + referenceType
        + ", studentId='" + studentId + '\''
        + ", teacherId='" + teacherId + '\''
        + ", score=" + score
        + ", assessmentDate=" + assessmentDate
        + ", notes='" + notes + '\''
        + ", courseId='" + courseId + '\''
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

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  public ReferenceType getReferenceType() {
    return referenceType;
  }

  public void setReferenceType(ReferenceType referenceType) {
    this.referenceType = referenceType;
  }

  public String getStudentId() {
    return studentId;
  }

  public void setStudentId(String studentId) {
    this.studentId = studentId;
  }

  public String getTeacherId() {
    return teacherId;
  }

  public void setTeacherId(String teacherId) {
    this.teacherId = teacherId;
  }

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public LocalDateTime getAssessmentDate() {
    return assessmentDate;
  }

  public void setAssessmentDate(LocalDateTime assessmentDate) {
    this.assessmentDate = assessmentDate;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getCourseId() {
    return courseId;
  }

  public void setCourseId(String courseId) {
    this.courseId = courseId;
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
   * Crea un nuovo builder per costruire un'istanza di Assessment.
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
   * Builder per la costruzione fluente di istanze Assessment.
   */
  public static class Builder {

    private String id;
    private String referenceId;
    private ReferenceType referenceType;
    private String studentId;
    private String teacherId;
    private Double score;
    private LocalDateTime assessmentDate;
    private String notes;
    private String courseId;
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
     * Imposta il reference ID.
     *
     * @param referenceId il reference ID
     * @return il builder
     */
    public Builder referenceId(String referenceId) {
      this.referenceId = referenceId;
      return this;
    }

    /**
     * Imposta il tipo di riferimento.
     *
     * @param referenceType il tipo di riferimento
     * @return il builder
     */
    public Builder referenceType(ReferenceType referenceType) {
      this.referenceType = referenceType;
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
     * Imposta l'ID del docente.
     *
     * @param teacherId l'ID del docente
     * @return il builder
     */
    public Builder teacherId(String teacherId) {
      this.teacherId = teacherId;
      return this;
    }

    /**
     * Imposta il punteggio.
     *
     * @param score il punteggio
     * @return il builder
     */
    public Builder score(Double score) {
      this.score = score;
      return this;
    }

    /**
     * Imposta la data di valutazione.
     *
     * @param assessmentDate la data di valutazione
     * @return il builder
     */
    public Builder assessmentDate(LocalDateTime assessmentDate) {
      this.assessmentDate = assessmentDate;
      return this;
    }

    /**
     * Imposta le note.
     *
     * @param notes le note
     * @return il builder
     */
    public Builder notes(String notes) {
      this.notes = notes;
      return this;
    }

    /**
     * Imposta l'ID del corso.
     *
     * @param courseId l'ID del corso
     * @return il builder
     */
    public Builder courseId(String courseId) {
      this.courseId = courseId;
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
     * Costruisce l'istanza di Assessment.
     *
     * @return l'istanza di Assessment costruita
     */
    public Assessment build() {
      return new Assessment(id, referenceId, referenceType, studentId, teacherId,
          score, assessmentDate, notes, courseId, createdAt, updatedAt);
    }
  }
}