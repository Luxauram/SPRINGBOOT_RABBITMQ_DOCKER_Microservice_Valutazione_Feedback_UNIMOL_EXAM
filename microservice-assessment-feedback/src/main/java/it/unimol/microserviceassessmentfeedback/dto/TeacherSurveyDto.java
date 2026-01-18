package it.unimol.microserviceassessmentfeedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.unimol.microserviceassessmentfeedback.enums.QuestionType;
import it.unimol.microserviceassessmentfeedback.enums.SurveyStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) per i questionari degli insegnanti.
 * Rappresenta un questionario di valutazione per un corso tenuto da un docente.
 */
@Schema(description = "DTO per i Questionari degli Insegnanti")
public class TeacherSurveyDto {

  @Schema(description = "ID Questionario", example = "uuid-questionario-123", accessMode =
      Schema.AccessMode.READ_ONLY)
  private String id;

  @Schema(description = "ID del Corso a cui si riferisce il questionario", example = "uuid-corso"
      + "-456", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "CourseID è richiesto")
  private String courseId;

  @Schema(description = "ID del Docente a cui si riferisce il questionario", example = "uuid"
      + "-docente-789", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "TeacherID è richiesto")
  private String teacherId;

  @Schema(description = "Anno accademico del questionario (formato YYYY-YYYY)", example = "2023"
      + "-2024", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "AcademicYear è richiesto")
  @Pattern(regexp = "\\d{4}-\\d{4}", message = "Il formato dell'anno accademico deve essere "
      + "YYYY-YYYY")
  private String academicYear;

  @Schema(description = "Semestre del questionario (es. 1 o 2)", example = "1", requiredMode =
      Schema.RequiredMode.REQUIRED)
  @NotNull(message = "Semester è richiesto")
  @Positive(message = "Semester deve essere un numero positivo")
  private Integer semester;

  @Schema(description = "Stato del questionario (es. DRAFT, ACTIVE, CLOSED). Valori consentiti: "
      + "DRAFT, ACTIVE, CLOSED",
      example = "ACTIVE",
      requiredMode = Schema.RequiredMode.REQUIRED,
      allowableValues = {"DRAFT", "ACTIVE", "CLOSED"})
  @NotNull(message = "Status è richiesto")
  private SurveyStatus status;

  @Schema(description = "Data di creazione del questionario", example = "2024-03-01T09:00:00",
      accessMode = Schema.AccessMode.READ_ONLY)
  private LocalDateTime creationDate;

  @Schema(description = "Data di chiusura del questionario", example = "2024-03-31T23:59:59")
  private LocalDateTime closingDate;

  @Schema(description = "Titolo del questionario", example = "Questionario Valutazione "
      + "Insegnamento Matematica I", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
  @NotBlank(message = "Il titolo del questionario è richiesto")
  @Size(max = 255, message = "Il titolo non può superare i 255 caratteri")
  private String title;

  @Schema(description = "Descrizione dettagliata del questionario", example = "Valutazione "
      + "dell'insegnamento di Matematica I tenuto dal Prof. Rossi, focalizzata sulla didattica e "
      + "il materiale.", maxLength = 1000)
  @Size(max = 1000, message = "La descrizione non può superare i 1000 caratteri")
  private String description;

  @Schema(description = "Lista delle domande del questionario", requiredMode =
      Schema.RequiredMode.REQUIRED)
  @NotNull(message = "La lista delle domande è richiesta")
  @Size(min = 1, message = "Il questionario deve contenere almeno una domanda")
  private List<SurveyQuestionDto> questions;


  // ============ Costruttore ============

  /**
   * Costruttore di default.
   */
  public TeacherSurveyDto() {
  }

  /**
   * Costruttore con tutti i parametri.
   *
   * @param id ID del questionario
   * @param courseId ID del corso
   * @param teacherId ID del docente
   * @param academicYear Anno accademico
   * @param semester Semestre
   * @param status Stato del questionario
   * @param creationDate Data di creazione
   * @param closingDate Data di chiusura
   * @param title Titolo del questionario
   * @param description Descrizione del questionario
   * @param questions Lista delle domande
   */
  public TeacherSurveyDto(String id, String courseId, String teacherId, String academicYear,
      Integer semester, SurveyStatus status, LocalDateTime creationDate,
      LocalDateTime closingDate, String title, String description,
      List<SurveyQuestionDto> questions) {
    this.id = id;
    this.courseId = courseId;
    this.teacherId = teacherId;
    this.academicYear = academicYear;
    this.semester = semester;
    this.status = status;
    this.creationDate = creationDate;
    this.closingDate = closingDate;
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
    TeacherSurveyDto that = (TeacherSurveyDto) o;
    return Objects.equals(id, that.id)
        && Objects.equals(courseId, that.courseId)
        && Objects.equals(teacherId, that.teacherId)
        && Objects.equals(academicYear, that.academicYear)
        && Objects.equals(semester, that.semester)
        && status == that.status
        && Objects.equals(creationDate, that.creationDate)
        && Objects.equals(closingDate, that.closingDate)
        && Objects.equals(title, that.title)
        && Objects.equals(description, that.description)
        && Objects.equals(questions, that.questions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, courseId, teacherId, academicYear,
        semester, status, creationDate, closingDate, title, description, questions);
  }

  @Override
  public String toString() {
    return "TeacherSurveyDTO{"
        + "id='" + id + '\''
        + ", courseId='" + courseId + '\''
        + ", teacherId='" + teacherId + '\''
        + ", academicYear='" + academicYear + '\''
        + ", semester=" + semester
        + ", status=" + status
        + ", creationDate=" + creationDate
        + ", closingDate=" + closingDate
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
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder per la costruzione di oggetti TeacherSurveyDto.
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
    private String title;
    private String description;
    private List<SurveyQuestionDto> questions;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder courseId(String courseId) {
      this.courseId = courseId;
      return this;
    }

    public Builder teacherId(String teacherId) {
      this.teacherId = teacherId;
      return this;
    }

    public Builder academicYear(String academicYear) {
      this.academicYear = academicYear;
      return this;
    }

    public Builder semester(Integer semester) {
      this.semester = semester;
      return this;
    }

    public Builder status(SurveyStatus status) {
      this.status = status;
      return this;
    }

    public Builder creationDate(LocalDateTime creationDate) {
      this.creationDate = creationDate;
      return this;
    }

    public Builder closingDate(LocalDateTime closingDate) {
      this.closingDate = closingDate;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder questions(List<SurveyQuestionDto> questions) {
      this.questions = questions;
      return this;
    }

    public TeacherSurveyDto build() {
      return new TeacherSurveyDto(id, courseId, teacherId, academicYear,
          semester, status, creationDate, closingDate, title, description, questions);
    }
  }

  /**
   * DTO per una singola domanda del questionario.
   */
  @Schema(description = "DTO per una singola domanda del questionario")
  public static class SurveyQuestionDto {

    @Schema(description = "ID univoco della domanda (generato automaticamente)", example = "uuid"
        + "-domanda-abc", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(description = "Testo della domanda", example = "Quanto è stata chiara la spiegazione "
        + "del docente?", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500)
    @NotBlank(message = "Il testo della domanda è richiesto")
    @Size(max = 500, message = "Il testo della domanda non può superare i 500 caratteri")
    private String questionText;

    @Schema(description = "Tipo di domanda (RATING o TEXT). Valori consentiti: RATING, TEXT",
        example = "RATING",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"RATING", "TEXT"})
    @NotNull(message = "Il tipo di domanda è richiesto")
    private QuestionType questionType;

    @Schema(description = "Valore minimo per domande di tipo RATING (valido solo se questionType "
        + "è RATING)", example = "1", minimum = "1", defaultValue = "1")
    @Min(value = 1, message = "Il rating minimo deve essere almeno 1")
    private Integer minRating;

    @Schema(description = "Valore massimo per domande di tipo RATING (valido solo se questionType"
        + " è RATING)", example = "5", maximum = "5", defaultValue = "5")
    @Max(value = 5, message = "Il rating massimo non può superare 5")
    private Integer maxRating;

    @Schema(description = "Lunghezza massima per domande di tipo TEXT (valido solo se "
        + "questionType è TEXT)", example = "255", minimum = "1", defaultValue = "255")
    @Min(value = 1, message = "La lunghezza massima del testo deve essere almeno 1")
    private Integer maxLengthText;

    // ============ Costruttore ============

    /**
     * Costruttore di default.
     * Genera automaticamente un ID univoco.
     */
    public SurveyQuestionDto() {
      this.id = UUID.randomUUID().toString();
    }

    /**
     * Costruttore con tutti i parametri.
     *
     * @param id ID della domanda
     * @param questionText Testo della domanda
     * @param questionType Tipo di domanda (RATING o TEXT)
     * @param minRating Valore minimo per domande RATING
     * @param maxRating Valore massimo per domande RATING
     * @param maxLengthText Lunghezza massima per domande TEXT
     */
    public SurveyQuestionDto(String id, String questionText, QuestionType questionType,
        Integer minRating, Integer maxRating, Integer maxLengthText) {
      this.id = id;
      this.questionText = questionText;
      this.questionType = questionType;
      this.minRating = minRating;
      this.maxRating = maxRating;
      this.maxLengthText = maxLengthText;
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
      SurveyQuestionDto that = (SurveyQuestionDto) o;
      return Objects.equals(id, that.id)
          && Objects.equals(questionText, that.questionText)
          && questionType == that.questionType
          && Objects.equals(minRating, that.minRating)
          && Objects.equals(maxRating, that.maxRating)
          && Objects.equals(maxLengthText, that.maxLengthText);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, questionText, questionType, minRating, maxRating, maxLengthText);
    }

    @Override
    public String toString() {
      return "SurveyQuestionDTO{"
          + "id='" + id + '\''
          + ", questionText='" + questionText + '\''
          + ", questionType=" + questionType
          + ", minRating=" + minRating
          + ", maxRating=" + maxRating
          + ", maxLengthText=" + maxLengthText
          + '}';
    }

    // ============ Getters & Setters & Bool ============
    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getQuestionText() {
      return questionText;
    }

    public void setQuestionText(String questionText) {
      this.questionText = questionText;
    }

    public QuestionType getQuestionType() {
      return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
      this.questionType = questionType;
    }

    public Integer getMinRating() {
      return minRating;
    }

    public void setMinRating(Integer minRating) {
      this.minRating = minRating;
    }

    public Integer getMaxRating() {
      return maxRating;
    }

    public void setMaxRating(Integer maxRating) {
      this.maxRating = maxRating;
    }

    public Integer getMaxLengthText() {
      return maxLengthText;
    }

    public void setMaxLengthText(Integer maxLengthText) {
      this.maxLengthText = maxLengthText;
    }

    // ============ Metodi di Classe ============
    public static SurveyQuestionDto.Builder builder() {
      return new SurveyQuestionDto.Builder();
    }

    /**
     * Builder per la costruzione di oggetti SurveyQuestionDto.
     */
    public static class Builder {

      private String id;
      private String questionText;
      private QuestionType questionType;
      private Integer minRating;
      private Integer maxRating;
      private Integer maxLengthText;

      public Builder id(String id) {
        this.id = id;
        return this;
      }

      public Builder questionText(String questionText) {
        this.questionText = questionText;
        return this;
      }

      public Builder questionType(QuestionType questionType) {
        this.questionType = questionType;
        return this;
      }

      public Builder minRating(Integer minRating) {
        this.minRating = minRating;
        return this;
      }

      public Builder maxRating(Integer maxRating) {
        this.maxRating = maxRating;
        return this;
      }

      public Builder maxLengthText(Integer maxLengthText) {
        this.maxLengthText = maxLengthText;
        return this;
      }

      /**
       * Costruisce un oggetto SurveyQuestionDto con i valori impostati.
       * Imposta i valori di default per minRating, maxRating e maxLengthText se non specificati.
       *
       * @return Un nuovo oggetto SurveyQuestionDto
       */
      public SurveyQuestionDto build() {
        if (questionType == QuestionType.RATING) {
          if (minRating == null) {
            minRating = 1;
          }
          if (maxRating == null) {
            maxRating = 5;
          }
        }
        if (questionType == QuestionType.TEXT && maxLengthText == null) {
          maxLengthText = 255;
        }
        return new SurveyQuestionDto(id, questionText, questionType, minRating, maxRating,
            maxLengthText);
      }
    }
  }
}