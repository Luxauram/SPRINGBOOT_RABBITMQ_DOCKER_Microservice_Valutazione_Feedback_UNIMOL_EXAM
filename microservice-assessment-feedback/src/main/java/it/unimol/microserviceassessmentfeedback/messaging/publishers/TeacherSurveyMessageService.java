package it.unimol.microserviceassessmentfeedback.messaging.publishers;

import it.unimol.microserviceassessmentfeedback.dto.TeacherSurveyDto;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Servizio responsabile della pubblicazione degli eventi relativi ai survey dei docenti.
 * Gestisce la costruzione e l’invio di messaggi per survey completati,
 * richieste di risultati e richieste di commenti verso il sistema di messaggistica.
 */
@Service
public class TeacherSurveyMessageService extends BaseEventPublisher {

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============
  /**
   * Pubblica un evento di completamento di un survey da parte del docente.
   *
   * @param survey il DTO del survey completato
   */
  public void publishSurveyCompleted(TeacherSurveyDto survey) {
    Map<String, Object> message = createSurveyMessage(survey, "SURVEY_COMPLETED");
    publishMessage("survey.completed", message, "survey", survey.getId());
  }

  /**
   * Pubblica un evento di richiesta dei risultati di un survey.
   *
   * @param surveyId l'ID del survey
   * @param requestedBy l'identificativo di chi ha richiesto i risultati
   */
  public void publishSurveyResultsRequested(String surveyId, String requestedBy) {
    Map<String, Object> message = new HashMap<>();
    addBaseMessageFields(message, "SURVEY_RESULTS_REQUESTED");
    message.put("surveyId", surveyId);
    message.put("requestedBy", requestedBy);
    publishMessage("survey.results.requested", message, "survey", surveyId);
  }

  /**
   * Pubblica un evento di richiesta dei commenti di un survey.
   *
   * @param surveyId l'ID del survey
   * @param requestedBy l'identificativo di chi ha richiesto i commenti
   */
  public void publishSurveyCommentsRequested(String surveyId, String requestedBy) {
    Map<String, Object> message = new HashMap<>();
    addBaseMessageFields(message, "SURVEY_COMMENTS_REQUESTED");
    message.put("surveyId", surveyId);
    message.put("requestedBy", requestedBy);
    publishMessage("survey.comments.requested", message, "survey", surveyId);
  }

  /**
   * Crea il messaggio associato a un evento relativo a un survey del docente.
   *
   * @param survey il DTO del survey
   * @param eventType il tipo di evento da associare al messaggio
   * @return la mappa contenente i dati del messaggio
   */
  private Map<String, Object> createSurveyMessage(TeacherSurveyDto survey, String eventType) {
    Map<String, Object> message = new HashMap<>();
    addBaseMessageFields(message, eventType);
    message.put("surveyId", survey.getId());
    message.put("courseId", survey.getCourseId());
    message.put("teacherId", survey.getTeacherId());
    message.put("academicYear", survey.getAcademicYear());
    message.put("semester", survey.getSemester());
    message.put("status", survey.getStatus().toString());
    message.put("creationDate", survey.getCreationDate());
    message.put("closingDate", survey.getClosingDate());
    return message;
  }
}