package it.unimol.microserviceassessmentfeedback.repository;

import it.unimol.microserviceassessmentfeedback.enums.ReferenceType;
import it.unimol.microserviceassessmentfeedback.model.Assessment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository per la gestione delle operazioni CRUD sulle valutazioni (Assessment).
 * Fornisce metodi di ricerca per studente, docente, corso e riferimento.
 */
@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, String> {

  List<Assessment> findByStudentId(String studentId);

  List<Assessment> findByTeacherId(String teacherId);

  List<Assessment> findByCourseId(String courseId);

  List<Assessment> findByReferenceIdAndReferenceType(String referenceId,
      ReferenceType referenceType);

}