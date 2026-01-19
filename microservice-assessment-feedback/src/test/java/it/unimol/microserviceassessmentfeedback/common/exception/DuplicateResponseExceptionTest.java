package it.unimol.microserviceassessmentfeedback.common.exception;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

/**
 * Test per DuplicateResponseException
 */
class DuplicateResponseExceptionTest {

  @Test
  void testConstructorWithMessage() {
    String message = "Risposta duplicata trovata";

    DuplicateResponseException exception = new DuplicateResponseException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testExceptionCanBeThrown() {
    String message = "Test duplicate response";

    assertThrows(DuplicateResponseException.class, () -> {
      throw new DuplicateResponseException(message);
    });
  }


  @Test
  void testExceptionWithNullMessage() {
    DuplicateResponseException exception = new DuplicateResponseException(null);

    assertNotNull(exception);
    assertNull(exception.getMessage());
  }

  @Test
  void testExceptionWithEmptyMessage() {
    DuplicateResponseException exception = new DuplicateResponseException("");

    assertNotNull(exception);
    assertEquals("", exception.getMessage());
  }
}

/**
 * Test per ResourceNotFoundException
 */
class ResourceNotFoundExceptionTest {

  @Test
  void testConstructorWithMessage() {
    String message = "Risorsa non trovata";

    ResourceNotFoundException exception = new ResourceNotFoundException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testExceptionCanBeThrown() {
    String message = "Test resource not found";

    assertThrows(ResourceNotFoundException.class, () -> {
      throw new ResourceNotFoundException(message);
    });
  }


  @Test
  void testExceptionWithNullMessage() {
    ResourceNotFoundException exception = new ResourceNotFoundException(null);

    assertNotNull(exception);
    assertNull(exception.getMessage());
  }

  @Test
  void testExceptionWithDetailedMessage() {
    String message = "Survey con ID 12345 non trovato nel database";

    ResourceNotFoundException exception = new ResourceNotFoundException(message);

    assertEquals(message, exception.getMessage());
  }
}

/**
 * Test per SurveyClosedException
 */
class SurveyClosedExceptionTest {

  @Test
  void testConstructorWithMessage() {
    String message = "Survey già chiusa";

    SurveyClosedException exception = new SurveyClosedException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testExceptionCanBeThrown() {
    String message = "Test survey closed";

    assertThrows(SurveyClosedException.class, () -> {
      throw new SurveyClosedException(message);
    });
  }


  @Test
  void testExceptionWithNullMessage() {
    SurveyClosedException exception = new SurveyClosedException(null);

    assertNotNull(exception);
    assertNull(exception.getMessage());
  }

  @Test
  void testExceptionWithDetailedMessage() {
    String message = "Il questionario è stato chiuso il 2024-01-15 e non accetta più risposte";

    SurveyClosedException exception = new SurveyClosedException(message);

    assertEquals(message, exception.getMessage());
  }
}

/**
 * Test per UnauthorizedAccessException
 */
class UnauthorizedAccessExceptionTest {

  @Test
  void testConstructorWithMessage() {
    String message = "Accesso non autorizzato";

    UnauthorizedAccessException exception = new UnauthorizedAccessException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testExceptionCanBeThrown() {
    String message = "Test unauthorized access";

    assertThrows(UnauthorizedAccessException.class, () -> {
      throw new UnauthorizedAccessException(message);
    });
  }


  @Test
  void testExceptionWithNullMessage() {
    UnauthorizedAccessException exception = new UnauthorizedAccessException(null);

    assertNotNull(exception);
    assertNull(exception.getMessage());
  }

  @Test
  void testExceptionWithDetailedMessage() {
    String message = "L'utente student123 non ha i permessi per accedere a questa risorsa";

    UnauthorizedAccessException exception = new UnauthorizedAccessException(message);

    assertEquals(message, exception.getMessage());
  }
}

/**
 * Test per ErrorResponse
 */
class ErrorResponseTest {

  @Test
  void testDefaultConstructor() {
    ErrorResponse errorResponse = new ErrorResponse();

    assertNotNull(errorResponse);
    assertNull(errorResponse.getTimestamp());
    assertEquals(0, errorResponse.getStatus());
    assertNull(errorResponse.getError());
    assertNull(errorResponse.getMessage());
    assertNull(errorResponse.getPath());
  }

  @Test
  void testConstructorWithAllParameters() {
    LocalDateTime timestamp = LocalDateTime.now(ZoneId.systemDefault());
    int status = 404;
    String error = "Not Found";
    String message = "Risorsa non trovata";
    String path = "/api/surveys/123";

    ErrorResponse errorResponse = new ErrorResponse(timestamp, status, error, message, path);

    assertEquals(timestamp, errorResponse.getTimestamp());
    assertEquals(status, errorResponse.getStatus());
    assertEquals(error, errorResponse.getError());
    assertEquals(message, errorResponse.getMessage());
    assertEquals(path, errorResponse.getPath());
  }

  @Test
  void testSettersAndGetters() {
    ErrorResponse errorResponse = new ErrorResponse();
    LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30);

    errorResponse.setTimestamp(timestamp);
    errorResponse.setStatus(500);
    errorResponse.setError("Internal Server Error");
    errorResponse.setMessage("Errore del server");
    errorResponse.setPath("/api/test");

    assertEquals(timestamp, errorResponse.getTimestamp());
    assertEquals(500, errorResponse.getStatus());
    assertEquals("Internal Server Error", errorResponse.getError());
    assertEquals("Errore del server", errorResponse.getMessage());
    assertEquals("/api/test", errorResponse.getPath());
  }

  @Test
  void testEquals_SameObject() {
    ErrorResponse errorResponse = new ErrorResponse();

    assertTrue(errorResponse.equals(errorResponse));
  }

  @Test
  void testEquals_EqualObjects() {
    LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30);
    ErrorResponse response1 = new ErrorResponse(timestamp, 404, "Not Found", "Test", "/api/test");
    ErrorResponse response2 = new ErrorResponse(timestamp, 404, "Not Found", "Test", "/api/test");

    assertTrue(response1.equals(response2));
    assertTrue(response2.equals(response1));
  }

  @Test
  void testEquals_DifferentStatus() {
    LocalDateTime timestamp = LocalDateTime.now(ZoneId.systemDefault());
    ErrorResponse response1 = new ErrorResponse(timestamp, 404, "Not Found", "Test", "/api/test");
    ErrorResponse response2 = new ErrorResponse(timestamp, 500, "Not Found", "Test", "/api/test");

    assertFalse(response1.equals(response2));
  }

  @Test
  void testEquals_DifferentError() {
    LocalDateTime timestamp = LocalDateTime.now(ZoneId.systemDefault());
    ErrorResponse response1 = new ErrorResponse(timestamp, 404, "Not Found", "Test", "/api/test");
    ErrorResponse response2 = new ErrorResponse(timestamp, 404, "Bad Request", "Test", "/api/test");

    assertFalse(response1.equals(response2));
  }

  @Test
  void testEquals_DifferentMessage() {
    LocalDateTime timestamp = LocalDateTime.now(ZoneId.systemDefault());
    ErrorResponse response1 = new ErrorResponse(timestamp, 404, "Not Found", "Message1", "/api/test");
    ErrorResponse response2 = new ErrorResponse(timestamp, 404, "Not Found", "Message2", "/api/test");

    assertFalse(response1.equals(response2));
  }

  @Test
  void testEquals_DifferentPath() {
    LocalDateTime timestamp = LocalDateTime.now(ZoneId.systemDefault());
    ErrorResponse response1 = new ErrorResponse(timestamp, 404, "Not Found", "Test", "/api/test1");
    ErrorResponse response2 = new ErrorResponse(timestamp, 404, "Not Found", "Test", "/api/test2");

    assertFalse(response1.equals(response2));
  }

  @Test
  void testEquals_DifferentTimestamp() {
    LocalDateTime timestamp1 = LocalDateTime.of(2024, 1, 15, 10, 30);
    LocalDateTime timestamp2 = LocalDateTime.of(2024, 1, 15, 11, 30);
    ErrorResponse response1 = new ErrorResponse(timestamp1, 404, "Not Found", "Test", "/api/test");
    ErrorResponse response2 = new ErrorResponse(timestamp2, 404, "Not Found", "Test", "/api/test");

    assertFalse(response1.equals(response2));
  }

  @Test
  void testEquals_Null() {
    ErrorResponse errorResponse = new ErrorResponse();

    assertFalse(errorResponse.equals(null));
  }

  @Test
  void testEquals_DifferentClass() {
    ErrorResponse errorResponse = new ErrorResponse();
    String other = "not an ErrorResponse";

    assertFalse(errorResponse.equals(other));
  }

  @Test
  void testEquals_WithNullFields() {
    ErrorResponse response1 = new ErrorResponse(null, 404, null, null, null);
    ErrorResponse response2 = new ErrorResponse(null, 404, null, null, null);

    assertTrue(response1.equals(response2));
  }

  @Test
  void testEquals_OneNullTimestamp() {
    LocalDateTime timestamp = LocalDateTime.now(ZoneId.systemDefault());
    ErrorResponse response1 = new ErrorResponse(timestamp, 404, "Error", "Message", "/path");
    ErrorResponse response2 = new ErrorResponse(null, 404, "Error", "Message", "/path");

    assertFalse(response1.equals(response2));
    assertFalse(response2.equals(response1));
  }

  @Test
  void testHashCode_EqualObjects() {
    LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30);
    ErrorResponse response1 = new ErrorResponse(timestamp, 404, "Not Found", "Test", "/api/test");
    ErrorResponse response2 = new ErrorResponse(timestamp, 404, "Not Found", "Test", "/api/test");

    assertEquals(response1.hashCode(), response2.hashCode());
  }

  @Test
  void testHashCode_DifferentObjects() {
    ErrorResponse response1 = new ErrorResponse(LocalDateTime.now(ZoneId.systemDefault()), 404, "Not Found", "Test1", "/api/test");
    ErrorResponse response2 = new ErrorResponse(LocalDateTime.now(ZoneId.systemDefault()), 500, "Error", "Test2", "/api/other");

    // Non è garantito che siano diversi, ma molto probabilmente lo saranno
    assertNotEquals(response1.hashCode(), response2.hashCode());
  }

  @Test
  void testHashCode_WithNullFields() {
    ErrorResponse errorResponse = new ErrorResponse(null, 0, null, null, null);

    // Deve poter calcolare hashCode anche con campi null
    assertDoesNotThrow(() -> errorResponse.hashCode());
  }

  @Test
  void testToString() {
    LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30);
    ErrorResponse errorResponse = new ErrorResponse(timestamp, 404, "Not Found",
        "Risorsa non trovata", "/api/surveys/123");

    String result = errorResponse.toString();

    assertNotNull(result);
    assertTrue(result.contains("ErrorResponse"));
    assertTrue(result.contains("404"));
    assertTrue(result.contains("Not Found"));
    assertTrue(result.contains("Risorsa non trovata"));
    assertTrue(result.contains("/api/surveys/123"));
  }

  @Test
  void testToString_WithNullFields() {
    ErrorResponse errorResponse = new ErrorResponse();

    String result = errorResponse.toString();

    assertNotNull(result);
    assertTrue(result.contains("ErrorResponse"));
    assertTrue(result.contains("null"));
  }

  @Test
  void testToString_ContainsAllFields() {
    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(ZoneId.systemDefault()),
        500,
        "Internal Server Error",
        "Test message",
        "/test/path"
    );

    String result = errorResponse.toString();

    assertTrue(result.contains("timestamp="));
    assertTrue(result.contains("status="));
    assertTrue(result.contains("error="));
    assertTrue(result.contains("message="));
    assertTrue(result.contains("path="));
  }
}