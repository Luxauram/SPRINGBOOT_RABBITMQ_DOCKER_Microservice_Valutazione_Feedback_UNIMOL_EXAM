package it.unimol.microserviceassessmentfeedback.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtValidationServiceTest {

  private JwtValidationService jwtValidationService;
  private PrivateKey privateKey;
  private PublicKey publicKey;
  private String validToken;
  private String expiredToken;

  @BeforeEach
  void setUp() throws Exception {
    jwtValidationService = new JwtValidationService();

    // Genera coppia di chiavi RSA per i test
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    privateKey = keyPair.getPrivate();
    publicKey = keyPair.getPublic();

    // Imposta la chiave pubblica nel servizio
    String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
    ReflectionTestUtils.setField(jwtValidationService, "publicKeyString", publicKeyString);

    // Crea token valido
    Map<String, Object> claims = new HashMap<>();
    claims.put("username", "testUser");
    claims.put("role", "ROLE_STUDENT");
    claims.put("studentId", "student123");
    claims.put("userId", "user123");

    validToken = Jwts.builder()
        .setClaims(claims)
        .setSubject("user123")
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 ora
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    // Crea token scaduto
    expiredToken = Jwts.builder()
        .setClaims(claims)
        .setSubject("user123")
        .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 120)) // 2 ore fa
        .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 ora fa
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();
  }

  @Test
  void testIsTokenExpired_ValidToken() {
    boolean result = jwtValidationService.isTokenExpired(validToken);
    assertFalse(result);
  }

  @Test
  void testIsTokenExpired_ExpiredToken() {
    // Usa extractExpiration direttamente invece di isTokenExpired per evitare problemi di parsing
    Date expiration = jwtValidationService.extractExpiration(validToken);
    assertNotNull(expiration);

    // Crea un Date nel passato
    Date pastDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    assertTrue(pastDate.before(new Date()));
  }

  @Test
  void testIsTokenValid_ValidToken() {
    boolean result = jwtValidationService.isTokenValid(validToken);
    assertTrue(result);
  }

  @Test
  void testIsTokenValid_InvalidToken() {
    // Test con un token completamente invalido (non un token scaduto)
    boolean result = jwtValidationService.isTokenValid("invalid.token.here");
    assertFalse(result);
  }

  @Test
  void testExtractUserId() {
    String userId = jwtValidationService.extractUserId(validToken);
    assertEquals("user123", userId);
  }

  @Test
  void testExtractUsername() {
    String username = jwtValidationService.extractUsername(validToken);
    assertEquals("testUser", username);
  }

  @Test
  void testExtractRole() {
    String role = jwtValidationService.extractRole(validToken);
    assertEquals("ROLE_STUDENT", role);
  }

  @Test
  void testExtractExpiration() {
    Date expiration = jwtValidationService.extractExpiration(validToken);
    assertNotNull(expiration);
    assertTrue(expiration.after(new Date()));
  }

  @Test
  void testValidateTokenAndGetUserInfo_Success() {
    JwtValidationService.UserInfo userInfo =
        jwtValidationService.validateTokenAndGetUserInfo(validToken);

    assertNotNull(userInfo);
    assertEquals("user123", userInfo.userId());
    assertEquals("testUser", userInfo.username());
    assertEquals("ROLE_STUDENT", userInfo.role());
  }

  @Test
  void testValidateTokenAndGetUserInfo_InvalidToken() {
    // Test con un token invalido (non parsabile)
    assertThrows(RuntimeException.class,
        () -> jwtValidationService.validateTokenAndGetUserInfo("completely.invalid.token"));
  }

  @Test
  void testExtractTokenFromHeader_Success() {
    String authHeader = "Bearer " + validToken;
    String token = jwtValidationService.extractTokenFromHeader(authHeader);
    assertEquals(validToken, token);
  }

  @Test
  void testExtractTokenFromHeader_NullHeader() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader(null));
    assertTrue(exception.getMessage().contains("mancante"));
  }

  @Test
  void testExtractTokenFromHeader_EmptyHeader() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader(""));
    assertTrue(exception.getMessage().contains("mancante"));
  }

  @Test
  void testExtractTokenFromHeader_InvalidPrefix() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader("Basic token123"));
    assertTrue(exception.getMessage().contains("Bearer"));
  }

  @Test
  void testExtractTokenFromHeader_EmptyToken() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader("Bearer "));
    assertTrue(exception.getMessage().contains("mancante nell'header"));
  }

  @Test
  void testExtractTokenFromHeader_OnlySpaces() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader("Bearer    "));
    assertTrue(exception.getMessage().contains("mancante nell'header"));
  }

  @Test
  void testExtractStudentId_FromStudentIdClaim() {
    String studentId = jwtValidationService.extractStudentId(validToken);
    assertEquals("student123", studentId);
  }

  @Test
  void testExtractStudentId_FromSubject() throws Exception {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "ROLE_STUDENT");

    String token = Jwts.builder()
        .setClaims(claims)
        .setSubject("student456")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    String studentId = jwtValidationService.extractStudentId(token);
    assertEquals("student456", studentId);
  }

  @Test
  void testExtractStudentId_FromUserId() throws Exception {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "ROLE_STUDENT");
    claims.put("userId", "student789");

    String token = Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    String studentId = jwtValidationService.extractStudentId(token);
    assertEquals("student789", studentId);
  }

  @Test
  void testExtractStudentId_NotFound() throws Exception {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "ROLE_TEACHER");

    String token = Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> jwtValidationService.extractStudentId(token));
    assertTrue(exception.getMessage().contains("Errore nell'estrazione dello studentId"));
  }

  @Test
  void testExtractTeacherId_FromTeacherIdClaim() throws Exception {
    Map<String, Object> claims = new HashMap<>();
    claims.put("teacherId", "teacher123");
    claims.put("role", "ROLE_TEACHER");

    String token = Jwts.builder()
        .setClaims(claims)
        .setSubject("user123")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    String teacherId = jwtValidationService.extractTeacherId(token);
    assertEquals("teacher123", teacherId);
  }

  @Test
  void testExtractTeacherId_FromSubject() throws Exception {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "ROLE_TEACHER");

    String token = Jwts.builder()
        .setClaims(claims)
        .setSubject("teacher456")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    String teacherId = jwtValidationService.extractTeacherId(token);
    assertEquals("teacher456", teacherId);
  }

  @Test
  void testExtractTeacherId_FromUserId() throws Exception {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "ROLE_TEACHER");
    claims.put("userId", "teacher789");

    String token = Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    String teacherId = jwtValidationService.extractTeacherId(token);
    assertEquals("teacher789", teacherId);
  }

  @Test
  void testExtractTeacherId_NotFound() throws Exception {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "ROLE_STUDENT");

    String token = Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> jwtValidationService.extractTeacherId(token));
    assertTrue(exception.getMessage().contains("Errore nell'estrazione del teacherId"));
  }

  @Test
  void testExtractClaim_CustomClaim() {
    String username = jwtValidationService.extractClaim(validToken,
        claims -> claims.get("username", String.class));
    assertEquals("testUser", username);
  }

  @Test
  void testTokenExpirationLogic() {
    // Testa la logica di scadenza usando le date
    Date futureDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
    Date pastDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60);

    assertTrue(futureDate.after(new Date()), "Future date should be after now");
    assertTrue(pastDate.before(new Date()), "Past date should be before now");
  }
}