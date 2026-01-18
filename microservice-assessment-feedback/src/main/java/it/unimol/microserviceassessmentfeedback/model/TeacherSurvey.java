package it.unimol.microserviceassessmentfeedback.model;

import it.unimol.microserviceassessmentfeedback.common.util.ListToJsonConverter;
import it.unimol.microserviceassessmentfeedback.dto.TeacherSurveyDto.SurveyQuestionDto;
import it.unimol.microserviceassessmentfeedback.enums.SurveyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entità JPA che rappresenta un questionario di valutazione docente.
 * Contiene informazioni sul corso, docente, periodo accademico, domande e stato del questionario.
 */
@Entity
@Table(name = "teacher_surveys")
public class TeacherSurvey {

  @Id
  private String id;

  @Column(name = "course_id", nullable = false)
  private String courseId;

  @Column(name = "teacher_id", nullable = false)
  private String teacherId;

  @Column(name = "academic_year", nullable = false)
  private String academicYear;

  @Column(nullable = false)
  private Integer semester;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SurveyStatus status;

  @Column(name = "creation_date", nullable = false)
  private LocalDateTime creationDate;

  @Column(name = "closing_date")
  private LocalDateTime closingDate;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "questions", columnDefinition = "TEXT")
  @Convert(converter = ListToJsonConverter.class)
  private List<SurveyQuestionDto> questions;

  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public TeacherSurvey() {
  }

  /**
   * Costruttore con tutti i parametri.
   *
   * @param id l'ID univoco del questionario
   * @param courseId l'ID del corso
   * @param teacherId l'ID del docente
   * @param academicYear l'anno accademico
   * @param semester il semestre
   * @param status lo stato del questionario
   * @param creationDate la data di creazione
   * @param closingDate la data di chiusura
   * @param createdAt la data di creazione record
   * @param updatedAt la data di ultimo aggiornamento
   * @param title il titolo del questionario
   * @param description la descrizione del questionario
   * @param questions la lista delle domande
   */
  public TeacherSurvey(String id, String courseId, String teacherId, String academicYear,
      Integer semester, SurveyStatus status, LocalDateTime creationDate,
      LocalDateTime closingDate, LocalDateTime createdAt, LocalDateTime updatedAt,
      String title, String description, List<SurveyQuestionDto> questions) {
    this.id = id;
    this.courseId = courseId;
    this.teacherId = teacherId;
    this.academicYear = academicYear;
    this.semester = semester;
    this.status = status;
    this.creationDate = creationDate;
    this.closingDate = closingDate;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.title = title;
    this.description = description;
    this.questions = questions;
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
    TeacherSurvey that = (TeacherSurvey) o;
    return Objects.equals(id, that.id)
        && Objects.equals(courseId, that.courseId)
        && Objects.equals(teacherId, that.teacherId)
        && Objects.equals(academicYear, that.academicYear)
        && Objects.equals(semester, that.semester)
        && status == that.status
        && Objects.equals(creationDate, that.creationDate)
        && Objects.equals(closingDate, that.closingDate)
        && Objects.equals(createdAt, that.createdAt)
        && Objects.equals(updatedAt, that.updatedAt)
        && Objects.equals(title, that.title)
        && Objects.equals(description, that.description)
        && Objects.equals(questions, that.questions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, courseId, teacherId, academicYear, semester,
        status, creationDate, closingDate, createdAt, updatedAt,
        title, description, questions);
  }

  @Override
  public String toString() {
    return "TeacherSurvey{"
        + "id='" + id + '\''
        + ", courseId='" + courseId + '\''
        + ", teacherId='" + teacherId + '\''
        + ", academicYear='" + academicYear + '\''
        + ", semester=" + semester
        + ", status=" + status
        + ", creationDate=" + creationDate
        + ", closingDate=" + closingDate
        + ", createdAt=" + createdAt
        + ", updatedAt=" + updatedAt
        + ", title='" + title + '\''
        + ", description='" + description + '\''
        + ", questions=" + questions
        + '}';
  }

  // ============ Getters & Setters & Bool ============

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCourseId() {
    return courseId;
  }

  public void setCourseId(String courseId) {
    this.courseId = courseId;
  }

  public String getTeacherId() {
    return teacherId;
  }

  public void setTeacherId(String teacherId) {
    this.teacherId = teacherId;
  }

  public String getAcademicYear() {
    return academicYear;
  }

  public void setAcademicYear(String academicYear) {
    this.academicYear = academicYear;
  }

  public Integer getSemester() {
    return semester;
  }

  public void setSemester(Integer semester) {
    this.semester = semester;
  }

  public SurveyStatus getStatus() {
    return status;
  }

  public void setStatus(SurveyStatus status) {
    this.status = status;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public LocalDateTime getClosingDate() {
    return closingDate;
  }

  public void setClosingDate(LocalDateTime closingDate) {
    this.closingDate = closingDate;
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

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<SurveyQuestionDto> getQuestions() {
    return questions;
  }

  public void setQuestions(List<SurveyQuestionDto> questions) {
    this.questions = questions;
  }

  // ============ Metodi di Classe ============

  /**
   * Crea un nuovo builder per costruire un'istanza di TeacherSurvey.
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
   * Builder per la costruzione fluente di istanze TeacherSurvey.
   */
  public static class Builder {

    private String id;
    private String courseId;
    private String teacherId;
    private String academicYear;
    private Integer semester;
    private SurveyStatus status;
    private LocalDateTime creationDate;
    private LocalDateTime closingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String title;
    private String description;
    private List<SurveyQuestionDto> questions;

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
     * Imposta l'anno accademico.
     *
     * @param academicYear l'anno accademico
     * @return il builder
     */
    public Builder academicYear(String academicYear) {
      this.academicYear = academicYear;
      return this;
    }

    /**
     * Imposta il semestre.
     *
     * @param semester il semestre
     * @return il builder
     */
    public Builder semester(Integer semester) {
      this.semester = semester;
      return this;
    }

    /**
     * Imposta lo stato del questionario.
     *
     * @param status lo stato
     * @return il builder
     */
    public Builder status(SurveyStatus status) {
      this.status = status;
      return this;
    }

    /**
     * Imposta la data di creazione.
     *
     * @param creationDate la data di creazione
     * @return il builder
     */
    public Builder creationDate(LocalDateTime creationDate) {
      this.creationDate = creationDate;
      return this;
    }

    /**
     * Imposta la data di chiusura.
     *
     * @param closingDate la data di chiusura
     * @return il builder
     */
    public Builder closingDate(LocalDateTime closingDate) {
      this.closingDate = closingDate;
      return this;
    }

    /**
     * Imposta la data di creazione record.
     *
     * @param createdAt la data di creazione record
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
     * Imposta il titolo.
     *
     * @param title il titolo
     * @return il builder
     */
    public Builder title(String title) {
      this.title = title;
      return this;
    }

    /**
     * Imposta la descrizione.
     *
     * @param description la descrizione
     * @return il builder
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Imposta la lista delle domande.
     *
     * @param questions la lista delle domande
     * @return il builder
     */
    public Builder questions(List<SurveyQuestionDto> questions) {
      this.questions = questions;
      return this;
    }

    /**
     * Costruisce l'istanza di TeacherSurvey.
     *
     * @return l'istanza di TeacherSurvey costruita
     */
    public TeacherSurvey build() {
      return new TeacherSurvey(id, courseId, teacherId, academicYear, semester,
          status, creationDate, closingDate, createdAt, updatedAt,
          title, description, questions);
    }
  }
}