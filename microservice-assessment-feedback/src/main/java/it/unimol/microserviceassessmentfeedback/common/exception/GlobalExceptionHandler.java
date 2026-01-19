package it.unimol.microserviceassessmentfeedback.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestore globale delle eccezioni per l'applicazione. Intercetta e gestisce tutte le eccezioni
 * lanciate dai controller REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  // ============ Metodi di Classe ============

  /**
   * Gestisce le eccezioni di tipo ResourceNotFoundException.
   *
   * @param ex      l'eccezione lanciata
   * @param request la richiesta HTTP che ha causato l'eccezione
   * @return una ResponseEntity contenente i dettagli dell'errore e status 404
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {

    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(ZoneId.systemDefault()),
        HttpStatus.NOT_FOUND.value(),
        "Not Found",
        ex.getMessage(),
        request.getRequestURI()
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Gestisce le eccezioni di tipo SurveyClosedException.
   *
   * @param ex      l'eccezione lanciata
   * @param request la richiesta HTTP che ha causato l'eccezione
   * @return una ResponseEntity contenente i dettagli dell'errore e status 400
   */
  @ExceptionHandler(SurveyClosedException.class)
  public ResponseEntity<ErrorResponse> handleSurveyClosedException(
      SurveyClosedException ex, HttpServletRequest request) {

    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(ZoneId.systemDefault()),
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        ex.getMessage(),
        request.getRequestURI()
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * Gestisce gli errori di validazione dei parametri di input.
   *
   * @param ex l'eccezione di validazione contenente gli errori
   * @return una ResponseEntity con una mappa degli errori di validazione e status 400
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Gestisce tutte le eccezioni non gestite specificatamente.
   *
   * @param ex      l'eccezione generica lanciata
   * @param request la richiesta HTTP che ha causato l'eccezione
   * @return una ResponseEntity contenente i dettagli dell'errore e status 500
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
      Exception ex, HttpServletRequest request) {

    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(ZoneId.systemDefault()),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal Server Error",
        ex.getMessage(),
        request.getRequestURI()
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}