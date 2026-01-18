package it.unimol.microserviceassessmentfeedback.messaging.publishers;

import it.unimol.microserviceassessmentfeedback.dto.SurveyResponseDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Servizio responsabile della pubblicazione degli eventi relativi
 * alle risposte ai sondaggi.
 * Gestisce la costruzione e l’invio di messaggi per singole risposte o invii bulk
 * verso il sistema di messaggistica.
 */
@Service
public class SurveyResponseMessageService extends BaseEventPublisher {

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  /**
   * Pubblica un evento di invio di una singola risposta a un survey.
   *
   * @param response il DTO della risposta al survey
   */
  public void publishSurveyResponseSubmitted(SurveyResponseDto response) {
    Map<String, Object> message = createSurveyResponseMessage(response,
        "SURVEY_RESPONSE_SUBMITTED");
    publishMessage("survey.response.submitted", message, "survey response", response.getId());
  }

  /**
   * Pubblica un evento di invio multiplo di risposte a un survey.
   *
   * @param responses la lista delle risposte inviate
   * @param surveyId l'ID del survey di riferimento
   */
  public void publishSurveyResponsesSubmitted(List<SurveyResponseDto> responses, String surveyId) {
    Map<String, Object> message = createBulkSurveyResponseMessage(responses, surveyId,
        "SURVEY_RESPONSES_BULK_SUBMITTED");
    publishMessage("survey.responses.bulk.submitted", message, "survey responses", surveyId);
    logger.info(
        "Bulk survey responses submitted event published successfully for survey ID: {} with {} "
            + "responses",
        surveyId, responses.size());
  }

  /**
   * Crea il messaggio per una singola risposta al survey.
   *
   * @param response il DTO della risposta
   * @param eventType il tipo di evento da associare al messaggio
   * @return la mappa contenente i dati del messaggio
   */
  private Map<String, Object> createSurveyResponseMessage(SurveyResponseDto response,
      String eventType) {
    Map<String, Object> message = new HashMap<>();
    addBaseMessageFields(message, eventType);
    message.put("responseId", response.getId());
    message.put("surveyId", response.getSurveyId());
    message.put("studentId", response.getStudentId());
    message.put("questionId", response.getQuestionId());
    message.put("numericRating", response.getNumericRating());
    message.put("textComment", response.getTextComment());
    message.put("submissionDate", response.getSubmissionDate());
    return message;
  }

  /**
   * Crea il messaggio per l'invio multiplo di risposte a un survey.
   *
   * @param responses la lista delle risposte
   * @param surveyId l'ID del survey
   * @param eventType il tipo di evento da associare al messaggio
   * @return la mappa contenente i dati del messaggio
   */
  private Map<String, Object> createBulkSurveyResponseMessage(List<SurveyResponseDto> responses,
      String surveyId, String eventType) {
    Map<String, Object> message = new HashMap<>();
    addBaseMessageFields(message, eventType);
    message.put("surveyId", surveyId);
    message.put("responseCount", responses.size());
    message.put("responses", responses.stream().map(response -> {
      Map<String, Object> responseData = new HashMap<>();
      responseData.put("responseId", response.getId());
      responseData.put("studentId", response.getStudentId());
      responseData.put("questionId", response.getQuestionId());
      responseData.put("numericRating", response.getNumericRating());
      responseData.put("textComment", response.getTextComment());
      responseData.put("submissionDate", response.getSubmissionDate());
      return responseData;
    }).toList());
    return message;
  }
}
