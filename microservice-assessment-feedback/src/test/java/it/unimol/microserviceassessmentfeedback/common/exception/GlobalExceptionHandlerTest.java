package it.unimol.microserviceassessmentfeedback.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private GlobalExceptionHandler exceptionHandler;

  private static final String TEST_URI = "/api/test";

  @BeforeEach
  void setUp() {
    // Non impostiamo stub qui perché alcuni test non usano request.getRequestURI()
  }

  @Test
  void testHandleResourceNotFoundException() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    String errorMessage = "Survey non trovato";
    ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleResourceNotFoundException(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
    assertEquals("Not Found", errorResponse.getError());
    assertEquals(errorMessage, errorResponse.getMessage());
    assertEquals(TEST_URI, errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
    assertTrue(errorResponse.getTimestamp().isBefore(LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(1)));
  }

  @Test
  void testHandleResourceNotFoundException_WithDetailedMessage() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    String errorMessage = "Survey con ID 12345 non trovato nel database";
    ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleResourceNotFoundException(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(errorMessage, response.getBody().getMessage());
  }

  @Test
  void testHandleResourceNotFoundException_DifferentURIs() {
    String uri1 = "/api/surveys/123";
    String uri2 = "/api/responses/456";

    when(request.getRequestURI()).thenReturn(uri1);
    ResourceNotFoundException exception1 = new ResourceNotFoundException("Error 1");
    ResponseEntity<ErrorResponse> response1 =
        exceptionHandler.handleResourceNotFoundException(exception1, request);

    when(request.getRequestURI()).thenReturn(uri2);
    ResourceNotFoundException exception2 = new ResourceNotFoundException("Error 2");
    ResponseEntity<ErrorResponse> response2 =
        exceptionHandler.handleResourceNotFoundException(exception2, request);

    assertEquals(uri1, response1.getBody().getPath());
    assertEquals(uri2, response2.getBody().getPath());
  }

  @Test
  void testHandleSurveyClosedException() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    String errorMessage = "Il questionario è chiuso";
    SurveyClosedException exception = new SurveyClosedException(errorMessage);

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleSurveyClosedException(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
    assertEquals("Bad Request", errorResponse.getError());
    assertEquals(errorMessage, errorResponse.getMessage());
    assertEquals(TEST_URI, errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
  }

  @Test
  void testHandleSurveyClosedException_WithDetailedMessage() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    String errorMessage = "Il questionario è stato chiuso il 2024-01-15 e non accetta più risposte";
    SurveyClosedException exception = new SurveyClosedException(errorMessage);

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleSurveyClosedException(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(errorMessage, response.getBody().getMessage());
  }

  @Test
  void testHandleValidationExceptions() throws NoSuchMethodException {
    // Crea un mock di BindingResult con errori di validazione
    BindingResult bindingResult = mock(BindingResult.class);

    FieldError fieldError1 = new FieldError("teacherSurveyDto", "title",
        "Il titolo del questionario è richiesto");
    FieldError fieldError2 = new FieldError("teacherSurveyDto", "academicYear",
        "Il formato dell'anno accademico deve essere YYYY-YYYY");

    when(bindingResult.getAllErrors()).thenReturn(java.util.Arrays.asList(fieldError1, fieldError2));

    // Crea un mock di MethodParameter
    MethodParameter methodParameter = mock(MethodParameter.class);

    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(methodParameter, bindingResult);

    ResponseEntity<Map<String, String>> response =
        exceptionHandler.handleValidationExceptions(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    Map<String, String> errors = response.getBody();
    assertNotNull(errors);
    assertEquals(2, errors.size());
    assertTrue(errors.containsKey("title"));
    assertTrue(errors.containsKey("academicYear"));
    assertEquals("Il titolo del questionario è richiesto", errors.get("title"));
    assertEquals("Il formato dell'anno accademico deve essere YYYY-YYYY",
        errors.get("academicYear"));
  }

  @Test
  void testHandleValidationExceptions_SingleError() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("dto", "field", "Error message");
    when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

    MethodParameter methodParameter = mock(MethodParameter.class);
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(methodParameter, bindingResult);

    ResponseEntity<Map<String, String>> response =
        exceptionHandler.handleValidationExceptions(exception);

    assertNotNull(response);
    Map<String, String> errors = response.getBody();
    assertEquals(1, errors.size());
    assertTrue(errors.containsKey("field"));
  }

  @Test
  void testHandleValidationExceptions_MultipleErrors() {
    BindingResult bindingResult = mock(BindingResult.class);

    FieldError error1 = new FieldError("dto", "field1", "Error 1");
    FieldError error2 = new FieldError("dto", "field2", "Error 2");
    FieldError error3 = new FieldError("dto", "field3", "Error 3");

    when(bindingResult.getAllErrors()).thenReturn(
        java.util.Arrays.asList(error1, error2, error3));

    MethodParameter methodParameter = mock(MethodParameter.class);
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(methodParameter, bindingResult);

    ResponseEntity<Map<String, String>> response =
        exceptionHandler.handleValidationExceptions(exception);

    Map<String, String> errors = response.getBody();
    assertEquals(3, errors.size());
  }

  @Test
  void testHandleGlobalException() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    String errorMessage = "Errore generico del server";
    Exception exception = new Exception(errorMessage);

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleGlobalException(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
    assertEquals("Internal Server Error", errorResponse.getError());
    assertEquals(errorMessage, errorResponse.getMessage());
    assertEquals(TEST_URI, errorResponse.getPath());
    assertNotNull(errorResponse.getTimestamp());
  }

  @Test
  void testHandleGlobalException_RuntimeException() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    String errorMessage = "Runtime exception occurred";
    RuntimeException exception = new RuntimeException(errorMessage);

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleGlobalException(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(errorMessage, response.getBody().getMessage());
  }

  @Test
  void testHandleGlobalException_NullPointerException() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    NullPointerException exception = new NullPointerException("Null pointer error");

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleGlobalException(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Null pointer error", response.getBody().getMessage());
  }

  @Test
  void testHandleGlobalException_IllegalArgumentException() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleGlobalException(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Invalid argument", response.getBody().getMessage());
  }

  @Test
  void testMultipleExceptionHandlers_DifferentPaths() {
    when(request.getRequestURI()).thenReturn("/api/surveys");
    ResourceNotFoundException notFoundEx = new ResourceNotFoundException("Not found");
    ResponseEntity<ErrorResponse> response1 =
        exceptionHandler.handleResourceNotFoundException(notFoundEx, request);

    when(request.getRequestURI()).thenReturn("/api/responses");
    SurveyClosedException closedEx = new SurveyClosedException("Closed");
    ResponseEntity<ErrorResponse> response2 =
        exceptionHandler.handleSurveyClosedException(closedEx, request);

    when(request.getRequestURI()).thenReturn("/api/error");
    Exception generalEx = new Exception("Error");
    ResponseEntity<ErrorResponse> response3 =
        exceptionHandler.handleGlobalException(generalEx, request);

    assertEquals("/api/surveys", response1.getBody().getPath());
    assertEquals("/api/responses", response2.getBody().getPath());
    assertEquals("/api/error", response3.getBody().getPath());
  }

  @Test
  void testTimestampIsRecent() throws InterruptedException {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    LocalDateTime before = LocalDateTime.now(ZoneId.systemDefault());
    Thread.sleep(10); // Breve pausa per garantire differenza temporale

    ResourceNotFoundException exception = new ResourceNotFoundException("Test");
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleResourceNotFoundException(exception, request);

    Thread.sleep(10);
    LocalDateTime after = LocalDateTime.now(ZoneId.systemDefault());

    LocalDateTime timestamp = response.getBody().getTimestamp();
    assertTrue(timestamp.isAfter(before) || timestamp.isEqual(before));
    assertTrue(timestamp.isBefore(after) || timestamp.isEqual(after));
  }

  @Test
  void testErrorResponseStructure_ResourceNotFound() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    ResourceNotFoundException exception = new ResourceNotFoundException("Test message");
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleResourceNotFoundException(exception, request);

    ErrorResponse body = response.getBody();
    assertNotNull(body.getTimestamp());
    assertEquals(404, body.getStatus());
    assertNotNull(body.getError());
    assertNotNull(body.getMessage());
    assertNotNull(body.getPath());
  }

  @Test
  void testErrorResponseStructure_SurveyClosed() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    SurveyClosedException exception = new SurveyClosedException("Test message");
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleSurveyClosedException(exception, request);

    ErrorResponse body = response.getBody();
    assertNotNull(body.getTimestamp());
    assertEquals(400, body.getStatus());
    assertNotNull(body.getError());
    assertNotNull(body.getMessage());
    assertNotNull(body.getPath());
  }

  @Test
  void testErrorResponseStructure_GlobalException() {
    when(request.getRequestURI()).thenReturn(TEST_URI);
    Exception exception = new Exception("Test message");
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleGlobalException(exception, request);

    ErrorResponse body = response.getBody();
    assertNotNull(body.getTimestamp());
    assertEquals(500, body.getStatus());
    assertNotNull(body.getError());
    assertNotNull(body.getMessage());
    assertNotNull(body.getPath());
  }

  @Test
  void testValidationExceptions_EmptyErrors() {
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.emptyList());

    MethodParameter methodParameter = mock(MethodParameter.class);
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(methodParameter, bindingResult);

    ResponseEntity<Map<String, String>> response =
        exceptionHandler.handleValidationExceptions(exception);

    assertNotNull(response);
    assertTrue(response.getBody().isEmpty());
  }
}