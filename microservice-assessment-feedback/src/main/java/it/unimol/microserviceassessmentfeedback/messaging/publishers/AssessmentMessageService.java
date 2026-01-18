package it.unimol.microserviceassessmentfeedback.messaging.publishers;

import it.unimol.microserviceassessmentfeedback.dto.AssessmentDto;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Servizio responsabile della pubblicazione degli eventi relativi agli assessment.
 * Si occupa di costruire e inviare messaggi di creazione, aggiornamento
 * e cancellazione degli assessment verso il sistema di messaggistica.
 */
@Service
public class AssessmentMessageService extends BaseEventPublisher {

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  /**
   * Pubblica un evento di creazione di un assessment.
   *
   * @param assessment l'assessment da pubblicare
   */
  public void publishAssessmentCreated(AssessmentDto assessment) {
    Map<String, Object> message = createAssessmentMessage(assessment, "ASSESSMENT_CREATED");
    publishMessage("assessment.created", message, "assessment", assessment.getId());
  }

  /**
   * Pubblica un evento di aggiornamento di un assessment.
   *
   * @param assessment l'assessment aggiornato da pubblicare
   */
  public void publishAssessmentUpdated(AssessmentDto assessment) {
    Map<String, Object> message = createAssessmentMessage(assessment, "ASSESSMENT_UPDATED");
    publishMessage("assessment.updated", message, "assessment", assessment.getId());
  }

  /**
   * Pubblica un evento di cancellazione di un assessment.
   *
   * @param assessmentId l'ID dell'assessment da eliminare
   */
  public void publishAssessmentDeleted(String assessmentId) {
    Map<String, Object> message = new HashMap<>();
    addBaseMessageFields(message, "ASSESSMENT_DELETED");
    message.put("assessmentId", assessmentId);
    publishMessage("assessment.deleted", message, "assessment", assessmentId);
  }

  /**
   * Crea la mappa dei campi del messaggio per un assessment.
   * Questo metodo è utilizzato internamente dai metodi di pubblicazione
   * per standardizzare il contenuto dei messaggi.
   *
   * @param assessment l'assessment da trasformare in messaggio
   * @param eventType il tipo di evento (es. "ASSESSMENT_CREATED")
   * @return mappa contenente i dati dell'assessment pronti per la pubblicazione
   */
  private Map<String, Object> createAssessmentMessage(AssessmentDto assessment, String eventType) {
    Map<String, Object> message = new HashMap<>();
    addBaseMessageFields(message, eventType);
    message.put("assessmentId", assessment.getId());
    message.put("referenceId", assessment.getReferenceId());
    message.put("referenceType", assessment.getReferenceType().toString());
    message.put("studentId", assessment.getStudentId());
    message.put("teacherId", assessment.getTeacherId());
    message.put("courseId", assessment.getCourseId());
    message.put("score", assessment.getScore());
    message.put("assessmentDate", assessment.getAssessmentDate());
    message.put("notes", assessment.getNotes());
    return message;
  }
}
