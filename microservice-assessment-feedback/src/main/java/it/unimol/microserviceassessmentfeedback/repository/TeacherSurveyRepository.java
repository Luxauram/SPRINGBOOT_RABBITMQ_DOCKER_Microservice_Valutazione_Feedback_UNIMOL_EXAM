package it.unimol.microserviceassessmentfeedback.repository;

import it.unimol.microserviceassessmentfeedback.enums.SurveyStatus;
import it.unimol.microserviceassessmentfeedback.model.TeacherSurvey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository per la gestione delle operazioni CRUD sui questionari di valutazione docente.
 * Fornisce metodi di ricerca per docente, corso, stato, anno accademico e semestre.
 */
@Repository
public interface TeacherSurveyRepository extends JpaRepository<TeacherSurvey, String> {

  List<TeacherSurvey> findByTeacherId(String teacherId);

  List<TeacherSurvey> findByCourseId(String courseId);

  List<TeacherSurvey> findByStatus(SurveyStatus status);

  List<TeacherSurvey> findByAcademicYear(String academicYear);

  List<TeacherSurvey> findBySemester(Integer semester);

  @Query("SELECT COUNT(ts) FROM TeacherSurvey ts WHERE ts.status = :status")
  Long countByStatus(@Param("status") SurveyStatus status);

  boolean existsByTeacherIdAndCourseIdAndAcademicYearAndSemester(
      String teacherId, String courseId, String academicYear, Integer semester);
}