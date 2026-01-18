package it.unimol.microserviceassessmentfeedback.repository;

import it.unimol.microserviceassessmentfeedback.model.SurveyResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository per la gestione delle operazioni CRUD sulle risposte ai questionari.
 * Fornisce metodi di ricerca per questionario, studente, domanda e commenti.
 */
@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, String> {

  List<SurveyResponse> findBySurveyId(String surveyId);

  List<SurveyResponse> findByStudentId(String studentId);

  List<SurveyResponse> findByQuestionId(String questionId);

  boolean existsBySurveyIdAndStudentId(String surveyId, String studentId);

  @Query("SELECT sr FROM SurveyResponse sr WHERE sr.survey.id = :surveyId AND sr.textComment IS "
      + "NOT NULL AND sr.textComment != ''")
  List<SurveyResponse> findAllWithCommentsForSurvey(@Param("surveyId") String surveyId);
}