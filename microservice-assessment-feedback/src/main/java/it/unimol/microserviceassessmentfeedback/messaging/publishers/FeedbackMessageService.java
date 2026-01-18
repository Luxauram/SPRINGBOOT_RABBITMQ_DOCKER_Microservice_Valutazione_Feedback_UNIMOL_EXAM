package it.unimol.microserviceassessmentfeedback.messaging.publishers;

import it.unimol.microserviceassessmentfeedback.dto.DetailedFeedbackDto;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Servizio responsabile della pubblicazione degli eventi relativi ai feedback.
 * Si occupa di costruire e inviare messaggi di creazione, aggiornamento
 * e cancellazione dei feedback verso il sistema di messaggistica.
 */
@Service
public class FeedbackMessageService extends BaseEventPublisher {

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============
  /**
   * Pubblica un evento di creazione di un feedback.
   *
   * @param feedback il DTO del feedback appena creato
   */
  public void publishFeedbackCreated(DetailedFeedbackDto feedback) {
    Map<String, Object> message = createFeedbackMessage(feedback, "FEEDBACK_CREATED");
    publishMessage("feedback.created", message, "feedback", feedback.getId());
  }

  /**
   * Pubblica un evento di aggiornamento di un feedback.
   *
   * @param feedback il DTO del feedback aggiornato
   */
  public void publishFeedbackUpdated(DetailedFeedbackDto feedback) {
    Map<String, Object> message = createFeedbackMessage(feedback, "FEEDBACK_UPDATED");
    publishMessage("feedback.updated", message, "feedback", feedback.getId());
  }

  /**
   * Pubblica un evento di cancellazione di un feedback.
   *
   * @param feedbackId l'ID del feedback da eliminare
   */
  public void publishFeedbackDeleted(String feedbackId) {
    Map<String, Object> message = new HashMap<>();
    addBaseMessageFields(message, "FEEDBACK_DELETED");
    message.put("feedbackId", feedbackId);
    publishMessage("feedback.deleted", message, "feedback", feedbackId);
  }

  /**
   * Crea il messaggio da pubblicare contenente i dati del feedback.
   *
   * @param feedback il DTO del feedback
   * @param eventType il tipo di evento da associare al messaggio
   * @return la mappa contenente i dati del messaggio
   */
  private Map<String, Object> createFeedbackMessage(DetailedFeedbackDto feedback,
      String eventType) {
    Map<String, Object> message = new HashMap<>();
    addBaseMessageFields(message, eventType);
    message.put("feedbackId", feedback.getId());
    message.put("assessmentId", feedback.getAssessmentId());
    message.put("feedbackText", feedback.getFeedbackText());
    message.put("category", feedback.getCategory().toString());
    message.put("strengths", feedback.getStrengths());
    message.put("improvementAreas", feedback.getImprovementAreas());
    return message;
  }
}
