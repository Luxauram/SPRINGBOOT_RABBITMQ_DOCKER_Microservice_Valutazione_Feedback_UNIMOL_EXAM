package it.unimol.microserviceassessmentfeedback.repository;

import it.unimol.microserviceassessmentfeedback.model.DetailedFeedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository per la gestione delle operazioni CRUD sui feedback dettagliati.
 * Fornisce metodi di ricerca per valutazione e studente.
 */
@Repository
public interface DetailedFeedbackRepository extends JpaRepository<DetailedFeedback, String> {

  List<DetailedFeedback> findByAssessmentId(String assessmentId);

  @Query("SELECT df FROM DetailedFeedback df JOIN df.assessment a WHERE a.studentId = :studentId")
  List<DetailedFeedback> findByStudentId(@Param("studentId") String studentId);
}