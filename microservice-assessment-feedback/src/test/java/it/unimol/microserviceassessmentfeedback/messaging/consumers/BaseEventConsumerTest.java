package it.unimol.microserviceassessmentfeedback.messaging.consumers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BaseEventConsumerTest {

  private TestEventConsumer testConsumer;

  @BeforeEach
  void setUp() {
    testConsumer = new TestEventConsumer();
    ReflectionTestUtils.setField(testConsumer, "serviceName", "test-service");
  }

  // ===================================================================
  // TEST METODI GET VALUE
  // ===================================================================

  @Test
  void testGetStringValue_Success() {
    Map<String, Object> message = new HashMap<>();
    message.put("key", "value");

    String result = testConsumer.getStringValue(message, "key");

    assertEquals("value", result);
  }

  @Test
  void testGetStringValue_NullValue() {
    Map<String, Object> message = new HashMap<>();

    String result = testConsumer.getStringValue(message, "nonExistent");

    assertNull(result);
  }

  @Test
  void testGetStringValue_NumberConversion() {
    Map<String, Object> message = new HashMap<>();
    message.put("number", 123);

    String result = testConsumer.getStringValue(message, "number");

    assertEquals("123", result);
  }

  @Test
  void testGetIntegerValue_Success() {
    Map<String, Object> message = new HashMap<>();
    message.put("count", 42);

    Integer result = testConsumer.getIntegerValue(message, "count");

    assertEquals(42, result);
  }

  @Test
  void testGetIntegerValue_FromString() {
    Map<String, Object> message = new HashMap<>();
    message.put("count", "42");

    Integer result = testConsumer.getIntegerValue(message, "count");

    assertEquals(42, result);
  }

  @Test
  void testGetIntegerValue_InvalidString() {
    Map<String, Object> message = new HashMap<>();
    message.put("count", "invalid");

    Integer result = testConsumer.getIntegerValue(message, "count");

    assertNull(result);
  }

  @Test
  void testGetIntegerValue_NullValue() {
    Map<String, Object> message = new HashMap<>();

    Integer result = testConsumer.getIntegerValue(message, "nonExistent");

    assertNull(result);
  }

  @Test
  void testGetLongValue_Success() {
    Map<String, Object> message = new HashMap<>();
    message.put("timestamp", 1234567890L);

    Long result = testConsumer.getLongValue(message, "timestamp");

    assertEquals(1234567890L, result);
  }

  @Test
  void testGetLongValue_FromInteger() {
    Map<String, Object> message = new HashMap<>();
    message.put("timestamp", 123);

    Long result = testConsumer.getLongValue(message, "timestamp");

    assertEquals(123L, result);
  }

  @Test
  void testGetLongValue_FromString() {
    Map<String, Object> message = new HashMap<>();
    message.put("timestamp", "1234567890");

    Long result = testConsumer.getLongValue(message, "timestamp");

    assertEquals(1234567890L, result);
  }

  @Test
  void testGetLongValue_InvalidString() {
    Map<String, Object> message = new HashMap<>();
    message.put("timestamp", "invalid");

    Long result = testConsumer.getLongValue(message, "timestamp");

    assertNull(result);
  }

  @Test
  void testGetLongValue_NullValue() {
    Map<String, Object> message = new HashMap<>();

    Long result = testConsumer.getLongValue(message, "nonExistent");

    assertNull(result);
  }

  @Test
  void testGetBooleanValue_Success() {
    Map<String, Object> message = new HashMap<>();
    message.put("flag", true);

    Boolean result = testConsumer.getBooleanValue(message, "flag");

    assertTrue(result);
  }

  @Test
  void testGetBooleanValue_FromString() {
    Map<String, Object> message = new HashMap<>();
    message.put("flag", "true");

    Boolean result = testConsumer.getBooleanValue(message, "flag");

    assertTrue(result);
  }

  @Test
  void testGetBooleanValue_FalseString() {
    Map<String, Object> message = new HashMap<>();
    message.put("flag", "false");

    Boolean result = testConsumer.getBooleanValue(message, "flag");

    assertFalse(result);
  }

  @Test
  void testGetBooleanValue_NullValue() {
    Map<String, Object> message = new HashMap<>();

    Boolean result = testConsumer.getBooleanValue(message, "nonExistent");

    assertNull(result);
  }

  // ===================================================================
  // TEST PROCESS MESSAGE
  // ===================================================================

  @Test
  void testProcessMessage_Success() {
    Map<String, Object> message = new HashMap<>();
    message.put("eventType", "TEST_EVENT");
    message.put("serviceName", "test-service");
    message.put("timestamp", System.currentTimeMillis());

    assertDoesNotThrow(() -> testConsumer.processMessage(message, "TEST"));
    assertTrue(testConsumer.wasHandleMessageCalled());
  }

  @Test
  void testProcessMessage_NullMessage() {
    assertDoesNotThrow(() -> testConsumer.processMessage(null, "TEST"));
    assertFalse(testConsumer.wasHandleMessageCalled());
  }

  @Test
  void testProcessMessage_EmptyMessage() {
    Map<String, Object> message = new HashMap<>();

    assertDoesNotThrow(() -> testConsumer.processMessage(message, "TEST"));
    assertFalse(testConsumer.wasHandleMessageCalled());
  }

  @Test
  void testProcessMessage_WithException() {
    testConsumer.setShouldThrowException(true);
    Map<String, Object> message = new HashMap<>();
    message.put("eventType", "TEST_EVENT");
    message.put("serviceName", "test-service");
    message.put("timestamp", System.currentTimeMillis());

    assertThrows(RuntimeException.class,
        () -> testConsumer.processMessage(message, "TEST"));
  }

  @Test
  void testProcessMessage_MissingEventType() {
    Map<String, Object> message = new HashMap<>();
    message.put("serviceName", "test-service");
    message.put("timestamp", System.currentTimeMillis());

    assertDoesNotThrow(() -> testConsumer.processMessage(message, "TEST"));
  }

  @Test
  void testProcessMessage_MissingTimestamp() {
    Map<String, Object> message = new HashMap<>();
    message.put("eventType", "TEST_EVENT");
    message.put("serviceName", "test-service");

    assertDoesNotThrow(() -> testConsumer.processMessage(message, "TEST"));
  }

  // ===================================================================
  // TEST EDGE CASES
  // ===================================================================

  @Test
  void testGetStringValue_EmptyString() {
    Map<String, Object> message = new HashMap<>();
    message.put("key", "");

    String result = testConsumer.getStringValue(message, "key");

    assertEquals("", result);
  }

  @Test
  void testGetIntegerValue_Zero() {
    Map<String, Object> message = new HashMap<>();
    message.put("count", 0);

    Integer result = testConsumer.getIntegerValue(message, "count");

    assertEquals(0, result);
  }

  @Test
  void testGetLongValue_Zero() {
    Map<String, Object> message = new HashMap<>();
    message.put("timestamp", 0L);

    Long result = testConsumer.getLongValue(message, "timestamp");

    assertEquals(0L, result);
  }

  @Test
  void testGetBooleanValue_NonBooleanString() {
    Map<String, Object> message = new HashMap<>();
    message.put("flag", "notABoolean");

    Boolean result = testConsumer.getBooleanValue(message, "flag");

    assertFalse(result); // Boolean.valueOf returns false for invalid strings
  }

  @Test
  void testProcessMessage_CompleteMessage() {
    Map<String, Object> message = new HashMap<>();
    message.put("eventType", "COMPLETE_EVENT");
    message.put("serviceName", "complete-service");
    message.put("timestamp", 1704067200000L);
    message.put("data", "some data");

    assertDoesNotThrow(() -> testConsumer.processMessage(message, "COMPLETE"));
    assertTrue(testConsumer.wasHandleMessageCalled());
  }

  // ===================================================================
  // CLASSE HELPER PER TEST
  // ===================================================================

  private static class TestEventConsumer extends BaseEventConsumer {
    private boolean handleMessageCalled = false;
    private boolean shouldThrowException = false;

    @Override
    protected void handleMessage(Map<String, Object> message, String messageType) {
      handleMessageCalled = true;
      if (shouldThrowException) {
        throw new RuntimeException("Test exception");
      }
    }

    public boolean wasHandleMessageCalled() {
      return handleMessageCalled;
    }

    public void setShouldThrowException(boolean shouldThrow) {
      this.shouldThrowException = shouldThrow;
    }

    // Rendi pubblici i metodi protected per il testing
    @Override
    public String getStringValue(Map<String, Object> message, String key) {
      return super.getStringValue(message, key);
    }

    @Override
    public Integer getIntegerValue(Map<String, Object> message, String key) {
      return super.getIntegerValue(message, key);
    }

    @Override
    public Long getLongValue(Map<String, Object> message, String key) {
      return super.getLongValue(message, key);
    }

    @Override
    public Boolean getBooleanValue(Map<String, Object> message, String key) {
      return super.getBooleanValue(message, key);
    }
  }
}