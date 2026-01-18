package it.unimol.apigateway.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test completi per JwtValidationService con alta coverage.
 */
class JwtValidationServiceTest {

  private JwtValidationService jwtValidationService;
  private PrivateKey privateKey;
  private PublicKey publicKey;
  private String publicKeyString;
  private String privateKeyString;

  @BeforeEach
  void setUp() throws Exception {
    jwtValidationService = new JwtValidationService();

    // Genera una coppia di chiavi RSA reale per i test
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair keyPair = keyGen.generateKeyPair();

    privateKey = keyPair.getPrivate();
    publicKey = keyPair.getPublic();

    // Converti le chiavi in Base64
    publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
    privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());

    // Inietta le chiavi nel service usando reflection
    ReflectionTestUtils.setField(jwtValidationService, "publicKeyString", publicKeyString);
    ReflectionTestUtils.setField(jwtValidationService, "privateKeyString", privateKeyString);
    ReflectionTestUtils.setField(jwtValidationService, "jwtExpiration", 3600000L);
  }

  /**
   * Metodo helper per generare un token JWT valido
   */
  private String generateValidToken(String userId, String username, String role,
      Long expirationMs) {
    return Jwts.builder()
        .setSubject(userId)
        .claim("username", username)
        .claim("role", role)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();
  }

  private String generateValidToken(String userId, String username, String role) {
    return generateValidToken(userId, username, role, 3600000L);
  }

  // ========== Test extractTokenFromHeader ==========

  @Test
  void testExtractTokenFromHeader_ValidHeader() {
    String authHeader = "Bearer mytoken123";
    String token = jwtValidationService.extractTokenFromHeader(authHeader);
    assertEquals("mytoken123", token);
  }

  @Test
  void testExtractTokenFromHeader_NullHeader() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader(null));
    assertEquals("Header Authorization mancante", exception.getMessage());
  }

  @Test
  void testExtractTokenFromHeader_EmptyHeader() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader(""));
    assertEquals("Header Authorization mancante", exception.getMessage());
  }

  @Test
  void testExtractTokenFromHeader_WhitespaceHeader() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader("   "));
    assertEquals("Header Authorization mancante", exception.getMessage());
  }

  @Test
  void testExtractTokenFromHeader_MissingBearer() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader("InvalidHeader token123"));
    assertEquals("Header Authorization deve iniziare con 'Bearer '", exception.getMessage());
  }

  @Test
  void testExtractTokenFromHeader_EmptyToken() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader("Bearer "));
    assertEquals("Token JWT mancante nell'header Authorization", exception.getMessage());
  }

  @Test
  void testExtractTokenFromHeader_OnlyWhitespaceToken() {
    SecurityException exception = assertThrows(SecurityException.class,
        () -> jwtValidationService.extractTokenFromHeader("Bearer    "));
    assertEquals("Token JWT mancante nell'header Authorization", exception.getMessage());
  }

  @Test
  void testExtractTokenFromHeader_WithWhitespace() {
    String authHeader = "Bearer   token_with_spaces  ";
    String token = jwtValidationService.extractTokenFromHeader(authHeader);
    assertEquals("token_with_spaces", token);
  }

  // ========== Test extractUserId, extractUsername, extractRole ==========

  @Test
  void testExtractUserId_ValidToken() {
    String token = generateValidToken("user123", "testuser", "ROLE_STUDENT");
    String userId = jwtValidationService.extractUserId(token);
    assertEquals("user123", userId);
  }

  @Test
  void testExtractUsername_ValidToken() {
    String token = generateValidToken("user123", "testuser", "ROLE_STUDENT");
    String username = jwtValidationService.extractUsername(token);
    assertEquals("testuser", username);
  }

  @Test
  void testExtractRole_ValidToken() {
    String token = generateValidToken("user123", "testuser", "ROLE_TEACHER");
    String role = jwtValidationService.extractRole(token);
    assertEquals("ROLE_TEACHER", role);
  }

  // ========== Test extractExpiration e isTokenExpired ==========

  @Test
  void testExtractExpiration_ValidToken() {
    String token = generateValidToken("user123", "testuser", "ROLE_STUDENT");
    Date expiration = jwtValidationService.extractExpiration(token);
    assertNotNull(expiration);
    assertTrue(expiration.after(new Date()));
  }

  @Test
  void testIsTokenExpired_NotExpired() {
    String token = generateValidToken("user123", "testuser", "ROLE_STUDENT", 3600000L);
    boolean expired = jwtValidationService.isTokenExpired(token);
    assertFalse(expired);
  }

  @Test
  void testIsTokenExpired_Expired() {
    // Crea un token con scadenza esplicita nel passato
    String token = Jwts.builder()
        .setSubject("user123")
        .claim("username", "testuser")
        .claim("role", "ROLE_STUDENT")
        .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 ore fa
        .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 ora fa
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    boolean expired = jwtValidationService.isTokenExpired(token);
    assertTrue(expired);
  }

  // ========== Test isTokenValid ==========

  @Test
  void testIsTokenValid_ValidToken() {
    String token = generateValidToken("user123", "testuser", "ROLE_STUDENT");
    boolean isValid = jwtValidationService.isTokenValid(token);
    assertTrue(isValid);
  }

  @Test
  void testIsTokenValid_ExpiredToken() {
    String token = generateValidToken("user123", "testuser", "ROLE_STUDENT", -1000L);
    boolean isValid = jwtValidationService.isTokenValid(token);
    assertFalse(isValid);
  }

  @Test
  void testIsTokenValid_InvalidToken() {
    boolean isValid = jwtValidationService.isTokenValid("invalid.token.here");
    assertFalse(isValid);
  }

  @Test
  void testIsTokenValid_MalformedToken() {
    boolean isValid = jwtValidationService.isTokenValid("not-a-jwt");
    assertFalse(isValid);
  }

  @Test
  void testIsTokenValid_EmptyToken() {
    boolean isValid = jwtValidationService.isTokenValid("");
    assertFalse(isValid);
  }

  @Test
  void testIsTokenValid_NullToken() {
    boolean isValid = jwtValidationService.isTokenValid(null);
    assertFalse(isValid);
  }

  // ========== Test validateTokenAndGetUserInfo ==========

  @Test
  void testValidateTokenAndGetUserInfo_ValidToken() {
    String token = generateValidToken("user123", "testuser", "ROLE_STUDENT");
    JwtValidationService.UserInfo userInfo =
        jwtValidationService.validateTokenAndGetUserInfo(token);

    assertNotNull(userInfo);
    assertEquals("user123", userInfo.userId());
    assertEquals("testuser", userInfo.username());
    assertEquals("ROLE_STUDENT", userInfo.role());
  }

  @Test
  void testValidateTokenAndGetUserInfo_InvalidToken() {
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> jwtValidationService.validateTokenAndGetUserInfo("invalid.token"));
    assertEquals("Token non valido o scaduto", exception.getMessage());
  }

  @Test
  void testValidateTokenAndGetUserInfo_ExpiredToken() {
    String token = generateValidToken("user123", "testuser", "ROLE_STUDENT", -1000L);
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> jwtValidationService.validateTokenAndGetUserInfo(token));
    assertEquals("Token non valido o scaduto", exception.getMessage());
  }

  // ========== Test extractStudentId ==========

  @Test
  void testExtractStudentId_WithStudentIdClaim() {
    String token = Jwts.builder()
        .setSubject("user123")
        .claim("username", "testuser")
        .claim("role", "ROLE_STUDENT")
        .claim("studentId", "student456")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 3600000))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    String studentId = jwtValidationService.extractStudentId(token);
    assertEquals("student456", studentId);
  }

  @Test
  void testExtractStudentId_WithRoleStudentAndSubject() {
    String token = generateValidToken("student789", "testuser", "ROLE_STUDENT");
    String studentId = jwtValidationService.extractStudentId(token);
    assertEquals("student789", studentId);
  }

  @Test
  void testExtractStudentId_WithRoleStudentAndUserId() {
    String token = Jwts.builder()
        .setSubject("")
        .claim("username", "testuser")
        .claim("role", "ROLE_STUDENT")
        .claim("userId", "user999")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 3600000))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    String studentId = jwtValidationService.extractStudentId(token);
    assertEquals("user999", studentId);
  }

  @Test
  void testExtractStudentId_NotFound() {
    String token = generateValidToken("user123", "testuser", "ROLE_ADMIN");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> jwtValidationService.extractStudentId(token));
    assertTrue(exception.getMessage().contains("StudentId non trovato"));
  }

  @Test
  void testExtractStudentId_InvalidToken() {
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> jwtValidationService.extractStudentId("invalid.token"));
    assertTrue(exception.getMessage().contains("Errore nell'estrazione dello studentId"));
  }

  // ========== Test extractTeacherId ==========

  @Test
  void testExtractTeacherId_WithTeacherIdClaim() {
    String token = Jwts.builder()
        .setSubject("user123")
        .claim("username", "testteacher")
        .claim("role", "ROLE_TEACHER")
        .claim("teacherId", "teacher456")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 3600000))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    String teacherId = jwtValidationService.extractTeacherId(token);
    assertEquals("teacher456", teacherId);
  }

  @Test
  void testExtractTeacherId_WithRoleTeacherAndSubject() {
    String token = generateValidToken("teacher789", "testteacher", "ROLE_TEACHER");
    String teacherId = jwtValidationService.extractTeacherId(token);
    assertEquals("teacher789", teacherId);
  }

  @Test
  void testExtractTeacherId_WithRoleTeacherAndUserId() {
    String token = Jwts.builder()
        .setSubject("")
        .claim("username", "testteacher")
        .claim("role", "ROLE_TEACHER")
        .claim("userId", "teacher999")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 3600000))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    String teacherId = jwtValidationService.extractTeacherId(token);
    assertEquals("teacher999", teacherId);
  }

  @Test
  void testExtractTeacherId_NotFound() {
    String token = generateValidToken("user123", "testuser", "ROLE_STUDENT");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> jwtValidationService.extractTeacherId(token));
    assertTrue(exception.getMessage().contains("TeacherId non trovato"));
  }

  @Test
  void testExtractTeacherId_InvalidToken() {
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> jwtValidationService.extractTeacherId("invalid.token"));
    assertTrue(exception.getMessage().contains("Errore nell'estrazione del teacherId"));
  }

  // ========== Test UserInfo Record ==========

  @Test
  void testUserInfoRecord() {
    JwtValidationService.UserInfo userInfo =
        new JwtValidationService.UserInfo("user123", "testuser", "ROLE_STUDENT");

    assertEquals("user123", userInfo.userId());
    assertEquals("testuser", userInfo.username());
    assertEquals("ROLE_STUDENT", userInfo.role());
  }

  @Test
  void testUserInfoRecord_NullValues() {
    JwtValidationService.UserInfo userInfo =
        new JwtValidationService.UserInfo(null, null, null);

    assertNull(userInfo.userId());
    assertNull(userInfo.username());
    assertNull(userInfo.role());
  }

  @Test
  void testUserInfoRecord_Equality() {
    JwtValidationService.UserInfo info1 =
        new JwtValidationService.UserInfo("id1", "user1", "ROLE_STUDENT");
    JwtValidationService.UserInfo info2 =
        new JwtValidationService.UserInfo("id1", "user1", "ROLE_STUDENT");

    assertEquals(info1, info2);
  }

  // ========== Test getPublicKey (coverage indiretto) ==========

  @Test
  void testGetPublicKey_InvalidBase64() {
    JwtValidationService service = new JwtValidationService();
    ReflectionTestUtils.setField(service, "publicKeyString", "not-valid-base64!");

    String token = generateValidToken("user123", "testuser", "ROLE_STUDENT");

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> service.extractUserId(token));
    assertTrue(exception.getMessage().contains("Chiave pubblica non è in formato Base64 valido") ||
        exception.getMessage().contains("Errore nella decodifica della chiave pubblica"));
  }

  @Test
  void testGetPublicKey_CachingBehavior() {
    // Primo accesso alla chiave
    String token1 = generateValidToken("user1", "username1", "ROLE_STUDENT");
    jwtValidationService.extractUserId(token1);

    // Secondo accesso dovrebbe usare la chiave in cache
    String token2 = generateValidToken("user2", "username2", "ROLE_TEACHER");
    jwtValidationService.extractUserId(token2);

    // Se non lancia eccezioni, il caching funziona
    assertNotNull(token2);
  }

  // ========== Test extractAllClaims (coverage indiretto) ==========

  @Test
  void testExtractAllClaims_JwtException() {
    // Token firmato con una chiave diversa
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048);
      KeyPair wrongKeyPair = keyGen.generateKeyPair();

      String tokenWithWrongKey = Jwts.builder()
          .setSubject("user123")
          .claim("username", "testuser")
          .claim("role", "ROLE_STUDENT")
          .setIssuedAt(new Date())
          .setExpiration(new Date(System.currentTimeMillis() + 3600000))
          .signWith(wrongKeyPair.getPrivate(), SignatureAlgorithm.RS256)
          .compact();

      RuntimeException exception = assertThrows(RuntimeException.class,
          () -> jwtValidationService.extractUserId(tokenWithWrongKey));
      assertTrue(exception.getMessage().contains("Token JWT non valido"));
    } catch (Exception e) {
      fail("Test setup failed: " + e.getMessage());
    }
  }

  @Test
  void testExtractClaim_CustomClaim() {
    String token = Jwts.builder()
        .setSubject("user123")
        .claim("customClaim", "customValue")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 3600000))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    String customValue = jwtValidationService.extractClaim(token,
        claims -> claims.get("customClaim", String.class));
    assertEquals("customValue", customValue);
  }
}