package it.unimol.microserviceuserrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import it.unimol.microserviceuserrole.dto.role.RoleDto;
import it.unimol.microserviceuserrole.dto.user.UserDto;
import it.unimol.microserviceuserrole.dto.user.UserProfileDto;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private MessageService messageService;

  @Captor
  private ArgumentCaptor<String> exchangeCaptor;

  @Captor
  private ArgumentCaptor<String> routingKeyCaptor;

  @Captor
  private ArgumentCaptor<Map<String, Object>> messageCaptor;

  private UserDto testUserDto;
  private UserProfileDto testUserProfileDto;
  private RoleDto testRoleDto;

  @BeforeEach
  void setUp() {
    // Inject configuration values
    ReflectionTestUtils.setField(messageService, "mainExchange", "unimol.exchange");
    ReflectionTestUtils.setField(messageService, "serviceName", "user-role-service");

    // Setup test data
    testRoleDto = new RoleDto("STUDENT", "Student", "Student role");

    testUserDto = new UserDto(
        "user123",
        "testuser",
        "test@example.com",
        "John",
        "Doe",
        LocalDateTime.now(),
        LocalDateTime.now(),
        testRoleDto
    );

    testUserProfileDto = new UserProfileDto(
        "user123",
        "testuser",
        "test@example.com",
        "John",
        "Doe",
        "STUDENT",
        LocalDateTime.now(),
        LocalDateTime.now()
    );
  }

  @Test
  void testPublishUserCreated_Success() {
    // Arrange
    doNothing().when(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        any(Object.class),
        any(MessagePostProcessor.class)
    );

    // Act
    messageService.publishUserCreated(testUserDto);

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq("unimol.exchange"),
        eq("user.created"),
        messageCaptor.capture(),
        any(MessagePostProcessor.class)
    );

    Map<String, Object> capturedMessage = messageCaptor.getValue();
    assertEquals("USER_CREATED", capturedMessage.get("eventType"));
    assertEquals("user123", capturedMessage.get("userId"));
    assertEquals("testuser", capturedMessage.get("username"));
    assertEquals("test@example.com", capturedMessage.get("email"));
    assertEquals("John", capturedMessage.get("name"));
    assertEquals("Doe", capturedMessage.get("surname"));
    assertEquals("STUDENT", capturedMessage.get("roleId"));
    assertEquals("Student", capturedMessage.get("roleName"));
    assertNotNull(capturedMessage.get("correlationId"));
    assertNotNull(capturedMessage.get("timestamp"));
    assertEquals("user-role-service", capturedMessage.get("sourceService"));
    assertEquals("1.0", capturedMessage.get("version"));
  }

  @Test
  void testPublishUserCreated_AmqpException() {
    // Arrange
    doThrow(new AmqpException("Connection error"))
        .when(rabbitTemplate).convertAndSend(
            anyString(),
            anyString(),
            any(Object.class),
            any(MessagePostProcessor.class)
        );

    // Act & Assert
    assertThrows(RuntimeException.class,
        () -> messageService.publishUserCreated(testUserDto));
  }

  @Test
  void testPublishUserCreated_MessageConversionException() {
    // Arrange
    doThrow(new MessageConversionException("Conversion error"))
        .when(rabbitTemplate).convertAndSend(
            anyString(),
            anyString(),
            any(Object.class),
            any(MessagePostProcessor.class)
        );

    // Act & Assert
    assertThrows(RuntimeException.class,
        () -> messageService.publishUserCreated(testUserDto));
  }

  @Test
  void testPublishUserUpdated_Success() {
    // Arrange
    doNothing().when(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        any(Object.class),
        any(MessagePostProcessor.class)
    );

    // Act
    messageService.publishUserUpdated(testUserDto);

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq("unimol.exchange"),
        eq("user.updated"),
        messageCaptor.capture(),
        any(MessagePostProcessor.class)
    );

    Map<String, Object> capturedMessage = messageCaptor.getValue();
    assertEquals("USER_UPDATED", capturedMessage.get("eventType"));
    assertEquals("user123", capturedMessage.get("userId"));
    assertEquals("testuser", capturedMessage.get("username"));
    assertNotNull(capturedMessage.get("correlationId"));
  }

  @Test
  void testPublishUserUpdated_Exception() {
    // Arrange
    doThrow(new AmqpException("Connection error"))
        .when(rabbitTemplate).convertAndSend(
            anyString(),
            anyString(),
            any(Object.class),
            any(MessagePostProcessor.class)
        );

    // Act & Assert
    assertThrows(RuntimeException.class,
        () -> messageService.publishUserUpdated(testUserDto));
  }

  @Test
  void testPublishUserDeleted_Success() {
    // Arrange
    String userId = "user123";
    doNothing().when(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        any(Object.class),
        any(MessagePostProcessor.class)
    );

    // Act
    messageService.publishUserDeleted(userId);

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq("unimol.exchange"),
        eq("user.deleted"),
        messageCaptor.capture(),
        any(MessagePostProcessor.class)
    );

    Map<String, Object> capturedMessage = messageCaptor.getValue();
    assertEquals("USER_DELETED", capturedMessage.get("eventType"));
    assertEquals("user123", capturedMessage.get("userId"));
    assertNotNull(capturedMessage.get("correlationId"));
    assertNotNull(capturedMessage.get("timestamp"));
    assertEquals("user-role-service", capturedMessage.get("sourceService"));
  }

  @Test
  void testPublishUserDeleted_Exception() {
    // Arrange
    doThrow(new AmqpException("Connection error"))
        .when(rabbitTemplate).convertAndSend(
            anyString(),
            anyString(),
            any(Object.class),
            any(MessagePostProcessor.class)
        );

    // Act & Assert
    assertThrows(RuntimeException.class,
        () -> messageService.publishUserDeleted("user123"));
  }

  @Test
  void testPublishRoleAssigned_Success() {
    // Arrange
    String userId = "user123";
    String roleId = "TEACHER";
    doNothing().when(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        any(Object.class),
        any(MessagePostProcessor.class)
    );

    // Act
    messageService.publishRoleAssigned(userId, roleId);

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq("unimol.exchange"),
        eq("role.assigned"),
        messageCaptor.capture(),
        any(MessagePostProcessor.class)
    );

    Map<String, Object> capturedMessage = messageCaptor.getValue();
    assertEquals("ROLE_ASSIGNED", capturedMessage.get("eventType"));
    assertEquals("user123", capturedMessage.get("userId"));
    assertEquals("TEACHER", capturedMessage.get("roleId"));
    assertNotNull(capturedMessage.get("correlationId"));
  }

  @Test
  void testPublishRoleAssigned_Exception() {
    // Arrange
    doThrow(new AmqpException("Connection error"))
        .when(rabbitTemplate).convertAndSend(
            anyString(),
            anyString(),
            any(Object.class),
            any(MessagePostProcessor.class)
        );

    // Act & Assert
    assertThrows(RuntimeException.class,
        () -> messageService.publishRoleAssigned("user123", "TEACHER"));
  }

  @Test
  void testPublishProfileUpdated_Success() {
    // Arrange
    doNothing().when(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        any(Object.class),
        any(MessagePostProcessor.class)
    );

    // Act
    messageService.publishProfileUpdated(testUserProfileDto);

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq("unimol.exchange"),
        eq("user.updated"),
        messageCaptor.capture(),
        any(MessagePostProcessor.class)
    );

    Map<String, Object> capturedMessage = messageCaptor.getValue();
    assertEquals("PROFILE_UPDATED", capturedMessage.get("eventType"));
    assertEquals("user123", capturedMessage.get("userId"));
    assertEquals("testuser", capturedMessage.get("username"));
    assertEquals("test@example.com", capturedMessage.get("email"));
    assertEquals("John", capturedMessage.get("name"));
    assertEquals("Doe", capturedMessage.get("surname"));
    assertNotNull(capturedMessage.get("correlationId"));
  }

  @Test
  void testPublishProfileUpdated_Exception() {
    // Arrange
    doThrow(new AmqpException("Connection error"))
        .when(rabbitTemplate).convertAndSend(
            anyString(),
            anyString(),
            any(Object.class),
            any(MessagePostProcessor.class)
        );

    // Act & Assert
    assertThrows(RuntimeException.class,
        () -> messageService.publishProfileUpdated(testUserProfileDto));
  }

  @Test
  void testPublishUserCreated_GenericException() {
    // Arrange
    doThrow(new RuntimeException("Generic error"))
        .when(rabbitTemplate).convertAndSend(
            anyString(),
            anyString(),
            any(Object.class),
            any(MessagePostProcessor.class)
        );

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> messageService.publishUserCreated(testUserDto));

    assertTrue(exception.getMessage().contains("USER_CREATED"));
  }

  @Test
  void testMessageStructure_ContainsAllRequiredFields() {
    // Arrange
    doNothing().when(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        any(Object.class),
        any(MessagePostProcessor.class)
    );

    // Act
    messageService.publishUserCreated(testUserDto);

    // Assert
    verify(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        messageCaptor.capture(),
        any(MessagePostProcessor.class)
    );

    Map<String, Object> message = messageCaptor.getValue();

    // Verify all required fields are present
    assertTrue(message.containsKey("eventType"));
    assertTrue(message.containsKey("correlationId"));
    assertTrue(message.containsKey("timestamp"));
    assertTrue(message.containsKey("sourceService"));
    assertTrue(message.containsKey("version"));
    assertTrue(message.containsKey("userId"));
    assertTrue(message.containsKey("username"));
    assertTrue(message.containsKey("email"));
  }

  @Test
  void testCorrelationId_IsUnique() {
    // Arrange
    doNothing().when(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        any(Object.class),
        any(MessagePostProcessor.class)
    );

    // Act
    messageService.publishUserCreated(testUserDto);
    messageService.publishUserCreated(testUserDto);

    // Assert
    verify(rabbitTemplate, times(2)).convertAndSend(
        anyString(),
        anyString(),
        messageCaptor.capture(),
        any(MessagePostProcessor.class)
    );

    String correlationId1 = (String) messageCaptor.getAllValues().get(0).get("correlationId");
    String correlationId2 = (String) messageCaptor.getAllValues().get(1).get("correlationId");

    assertNotEquals(correlationId1, correlationId2);
  }

  @Test
  void testTimestamp_IsRecentlyGenerated() {
    // Arrange
    long beforeTimestamp = System.currentTimeMillis();
    doNothing().when(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        any(Object.class),
        any(MessagePostProcessor.class)
    );

    // Act
    messageService.publishUserCreated(testUserDto);
    long afterTimestamp = System.currentTimeMillis();

    // Assert
    verify(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        messageCaptor.capture(),
        any(MessagePostProcessor.class)
    );

    long messageTimestamp = (Long) messageCaptor.getValue().get("timestamp");
    assertTrue(messageTimestamp >= beforeTimestamp);
    assertTrue(messageTimestamp <= afterTimestamp);
  }

  @Test
  void testPublishRoleAssigned_MultipleContextFields() {
    // Arrange
    doThrow(new AmqpException("Connection error"))
        .when(rabbitTemplate).convertAndSend(
            anyString(),
            anyString(),
            any(Object.class),
            any(MessagePostProcessor.class)
        );

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> messageService.publishRoleAssigned("user123", "TEACHER"));

    assertTrue(exception.getMessage().contains("ROLE_ASSIGNED"));
  }

  @Test
  void testExchangeAndRoutingKey_AreCorrect() {
    // Arrange
    doNothing().when(rabbitTemplate).convertAndSend(
        anyString(),
        anyString(),
        any(Object.class),
        any(MessagePostProcessor.class)
    );

    // Act
    messageService.publishUserCreated(testUserDto);
    messageService.publishUserUpdated(testUserDto);
    messageService.publishUserDeleted("user123");
    messageService.publishRoleAssigned("user123", "TEACHER");
    messageService.publishProfileUpdated(testUserProfileDto);

    // Assert
    verify(rabbitTemplate, times(5)).convertAndSend(
        exchangeCaptor.capture(),
        routingKeyCaptor.capture(),
        any(Object.class),
        any(MessagePostProcessor.class)
    );

    // Verify exchange is always the same
    assertEquals("unimol.exchange", exchangeCaptor.getAllValues().get(0));
    assertEquals("unimol.exchange", exchangeCaptor.getAllValues().get(1));
    assertEquals("unimol.exchange", exchangeCaptor.getAllValues().get(2));
    assertEquals("unimol.exchange", exchangeCaptor.getAllValues().get(3));
    assertEquals("unimol.exchange", exchangeCaptor.getAllValues().get(4));

    // Verify routing keys are correct
    assertEquals("user.created", routingKeyCaptor.getAllValues().get(0));
    assertEquals("user.updated", routingKeyCaptor.getAllValues().get(1));
    assertEquals("user.deleted", routingKeyCaptor.getAllValues().get(2));
    assertEquals("role.assigned", routingKeyCaptor.getAllValues().get(3));
    assertEquals("user.updated", routingKeyCaptor.getAllValues().get(4));
  }
}