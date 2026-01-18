package it.unimol.microserviceassessmentfeedback.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

  @Mock
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Mock
  private AuthenticationConfiguration authenticationConfiguration;

  @Mock
  private AuthenticationManager authenticationManager;

  @InjectMocks
  private SecurityConfig securityConfig;

  @BeforeEach
  void setUp() {
    // Setup comune se necessario
  }

  @Test
  void testPasswordEncoder_ReturnsBCryptPasswordEncoder() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

    assertNotNull(passwordEncoder);
    assertEquals("org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder",
        passwordEncoder.getClass().getName());
  }

  @Test
  void testPasswordEncoder_CanEncodePassword() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String rawPassword = "mySecretPassword123";

    String encodedPassword = passwordEncoder.encode(rawPassword);

    assertNotNull(encodedPassword);
    assertNotEquals(rawPassword, encodedPassword);
    assertTrue(encodedPassword.startsWith("$2"));
  }

  @Test
  void testPasswordEncoder_CanValidatePassword() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String rawPassword = "mySecretPassword123";
    String encodedPassword = passwordEncoder.encode(rawPassword);

    boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

    assertTrue(matches);
  }

  @Test
  void testPasswordEncoder_RejectsWrongPassword() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String rawPassword = "mySecretPassword123";
    String wrongPassword = "wrongPassword";
    String encodedPassword = passwordEncoder.encode(rawPassword);

    boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

    assertFalse(matches);
  }

  @Test
  void testPasswordEncoder_DifferentEncodingsForSamePassword() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String rawPassword = "mySecretPassword123";

    String encoded1 = passwordEncoder.encode(rawPassword);
    String encoded2 = passwordEncoder.encode(rawPassword);

    assertNotEquals(encoded1, encoded2);
    assertTrue(passwordEncoder.matches(rawPassword, encoded1));
    assertTrue(passwordEncoder.matches(rawPassword, encoded2));
  }

  @Test
  void testAuthenticationManager_ReturnsCorrectManager() throws Exception {
    when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

    AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

    assertNotNull(result);
    assertEquals(authenticationManager, result);
    verify(authenticationConfiguration).getAuthenticationManager();
  }

  @Test
  void testAuthenticationManager_ThrowsException() throws Exception {
    when(authenticationConfiguration.getAuthenticationManager())
        .thenThrow(new RuntimeException("Configuration error"));

    assertThrows(RuntimeException.class,
        () -> securityConfig.authenticationManager(authenticationConfiguration));
  }

  @Test
  void testSecurityConfig_HasJwtAuthenticationFilter() {
    JwtAuthenticationFilter filter =
        (JwtAuthenticationFilter) ReflectionTestUtils.getField(securityConfig, "jwtAuthenticationFilter");

    assertNotNull(filter);
    assertEquals(jwtAuthenticationFilter, filter);
  }

  @Test
  void testPasswordEncoder_HandlesEmptyPassword() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String emptyPassword = "";

    String encoded = passwordEncoder.encode(emptyPassword);

    assertNotNull(encoded);
    assertTrue(passwordEncoder.matches(emptyPassword, encoded));
  }

  @Test
  void testPasswordEncoder_HandlesLongPassword() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    // BCrypt supporta max 72 bytes, quindi usiamo una password di 70 caratteri
    String longPassword = "a".repeat(70);

    String encoded = passwordEncoder.encode(longPassword);

    assertNotNull(encoded);
    assertTrue(passwordEncoder.matches(longPassword, encoded));
  }

  @Test
  void testPasswordEncoder_HandlesSpecialCharacters() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String passwordWithSpecialChars = "P@ssw0rd!#$%^&*()_+-=[]{}|;:',.<>?/~`";

    String encoded = passwordEncoder.encode(passwordWithSpecialChars);

    assertNotNull(encoded);
    assertTrue(passwordEncoder.matches(passwordWithSpecialChars, encoded));
  }

  @Test
  void testPasswordEncoder_HandlesUnicodeCharacters() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String unicodePassword = "パスワード123àèéìòù";

    String encoded = passwordEncoder.encode(unicodePassword);

    assertNotNull(encoded);
    assertTrue(passwordEncoder.matches(unicodePassword, encoded));
  }

  @Test
  void testPasswordEncoder_IsDeterministicInValidation() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String password = "testPassword123";
    String encoded = passwordEncoder.encode(password);

    // Valida più volte con la stessa password
    assertTrue(passwordEncoder.matches(password, encoded));
    assertTrue(passwordEncoder.matches(password, encoded));
    assertTrue(passwordEncoder.matches(password, encoded));
  }

  @Test
  void testPasswordEncoder_CaseSensitive() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String password = "TestPassword123";
    String encoded = passwordEncoder.encode(password);

    assertTrue(passwordEncoder.matches("TestPassword123", encoded));
    assertFalse(passwordEncoder.matches("testpassword123", encoded));
    assertFalse(passwordEncoder.matches("TESTPASSWORD123", encoded));
  }

  @Test
  void testPasswordEncoder_HandlesNullValidation() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String password = "testPassword";
    String encoded = passwordEncoder.encode(password);

    assertThrows(IllegalArgumentException.class,
        () -> passwordEncoder.matches(null, encoded));
  }

  @Test
  void testPasswordEncoder_WorksWithMultipleInstances() {
    PasswordEncoder encoder1 = securityConfig.passwordEncoder();
    PasswordEncoder encoder2 = securityConfig.passwordEncoder();

    String password = "sharedPassword123";
    String encoded1 = encoder1.encode(password);

    // Un encoder diverso può validare password codificata da un altro
    assertTrue(encoder2.matches(password, encoded1));
  }
}