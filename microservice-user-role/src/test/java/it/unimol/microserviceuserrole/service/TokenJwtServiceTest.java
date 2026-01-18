package it.unimol.microserviceuserrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.unimol.microserviceuserrole.dto.auth.TokenDto;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TokenJwtServiceTest {

  private TokenJwtService tokenJwtService;
  private PrivateKey privateKey;
  private PublicKey publicKey;
  private String privateKeyString;
  private String publicKeyString;

  @BeforeEach
  void setUp() throws Exception {
    // Generate RSA key pair for testing
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();

    privateKey = keyPair.getPrivate();
    publicKey = keyPair.getPublic();

    // Encode keys to Base64 strings
    privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
    publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

    // Create service instance and inject values
    tokenJwtService = new TokenJwtService();
    ReflectionTestUtils.setField(tokenJwtService, "privateKeyString", privateKeyString);
    ReflectionTestUtils.setField(tokenJwtService, "publicKeyString", publicKeyString);
    ReflectionTestUtils.setField(tokenJwtService, "jwtExpiration", 3600L);
  }

  @Test
  void testGenerateToken() {
    TokenDto tokenDto = tokenJwtService.generateToken("user123", "testuser", "STUDENT");
    assertNotNull(tokenDto);
    assertNotNull(tokenDto.token());
    assertEquals("Bearer", tokenDto.type());
    assertEquals(3600L, tokenDto.expiresIn());
  }

  @Test
  void testExtractUserId() {
    // Arrange
    TokenDto tokenDto = tokenJwtService.generateToken("user123", "testuser", "STUDENT");

    // Act
    String userId = tokenJwtService.extractUserId(tokenDto.token());

    // Assert
    assertEquals("user123", userId);
  }

  @Test
  void testExtractUsername() {
    // Arrange
    TokenDto tokenDto = tokenJwtService.generateToken("user123", "testuser", "STUDENT");

    // Act
    String username = tokenJwtService.extractUsername(tokenDto.token());

    // Assert
    assertEquals("testuser", username);
  }

  @Test
  void testExtractRole() {
    // Arrange
    TokenDto tokenDto = tokenJwtService.generateToken("user123", "testuser", "STUDENT");

    // Act
    String role = tokenJwtService.extractRole(tokenDto.token());

    // Assert
    assertEquals("STUDENT", role);
  }

  @Test
  void testIsTokenValid_ValidToken() {
    // Arrange
    TokenDto tokenDto = tokenJwtService.generateToken("user123", "testuser", "STUDENT");

    // Act
    boolean isValid = tokenJwtService.isTokenValid(tokenDto.token());

    // Assert
    assertTrue(isValid);
  }

  @Test
  void testIsTokenValid_ExpiredToken() throws Exception {
    // Arrange - create an expired token
    long now = System.currentTimeMillis();
    long expiredTime = now - 10000;

    String expiredToken = Jwts.builder()
        .setSubject("user123")
        .setIssuedAt(new Date(now - 20000))
        .setExpiration(new Date(expiredTime))
        .claim("username", "testuser")
        .claim("role", "STUDENT")
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    // Act & Assert
    assertThrows(io.jsonwebtoken.ExpiredJwtException.class,
        () -> tokenJwtService.isTokenValid(expiredToken));
  }

  @Test
  void testIsTokenValid_InvalidatedToken() {
    // Arrange
    TokenDto tokenDto = tokenJwtService.generateToken("user123", "testuser", "STUDENT");
    tokenJwtService.invalidateToken(tokenDto.token());

    // Act
    boolean isValid = tokenJwtService.isTokenValid(tokenDto.token());

    // Assert
    assertFalse(isValid);
  }

  @Test
  void testInvalidateToken() {
    // Arrange
    TokenDto tokenDto = tokenJwtService.generateToken("user123", "testuser", "STUDENT");
    assertTrue(tokenJwtService.isTokenValid(tokenDto.token()));

    // Act
    tokenJwtService.invalidateToken(tokenDto.token());

    // Assert
    assertFalse(tokenJwtService.isTokenValid(tokenDto.token()));
  }

  @Test
  void testRefreshToken_Success() throws InterruptedException {
    // Arrange
    TokenDto originalToken = tokenJwtService.generateToken("user123", "testuser", "STUDENT");

    Thread.sleep(1000);

    // Act
    TokenDto refreshedToken = tokenJwtService.refreshToken(originalToken.token());

    // Assert
    assertNotNull(refreshedToken);
    assertEquals("user123", tokenJwtService.extractUserId(refreshedToken.token()));
    assertEquals("testuser", tokenJwtService.extractUsername(refreshedToken.token()));
    assertEquals("STUDENT", tokenJwtService.extractRole(refreshedToken.token()));
    assertTrue(tokenJwtService.isTokenValid(refreshedToken.token()));

    // Original token should be invalidated
    assertFalse(tokenJwtService.isTokenValid(originalToken.token()));
  }

  @Test
  void testRefreshToken_AlreadyInvalidated() {
    // Arrange
    TokenDto tokenDto = tokenJwtService.generateToken("user123", "testuser", "STUDENT");
    tokenJwtService.invalidateToken(tokenDto.token());

    // Act & Assert
    assertThrows(RuntimeException.class,
        () -> tokenJwtService.refreshToken(tokenDto.token()));
  }

  @Test
  void testParseToken() {
    TokenDto originalToken = tokenJwtService.generateToken("user123", "testuser", "STUDENT");
    TokenDto parsedToken = tokenJwtService.parseToken(originalToken.token());
    assertNotNull(parsedToken);
    assertEquals(originalToken.token(), parsedToken.token());
    assertEquals("Bearer", parsedToken.type());
    assertTrue(parsedToken.expiresIn() > 0);
    assertTrue(parsedToken.expiresIn() <= 3600L);
  }

  @Test
  void testParseToken_ExpiredToken() throws Exception {
    // Arrange - create an expired token
    long now = System.currentTimeMillis();
    String expiredToken = Jwts.builder()
        .setSubject("user123")
        .setIssuedAt(new Date(now - 20000))
        .setExpiration(new Date(now - 10000))
        .claim("username", "testuser")
        .claim("role", "STUDENT")
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();

    // Act
    TokenDto parsedToken = tokenJwtService.parseToken(expiredToken);

    // Assert
    assertNotNull(parsedToken);
    assertEquals(0L, parsedToken.expiresIn());
  }

  @Test
  void testExtractTokenFromHeader_Success() {
    // Arrange
    String authHeader = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.test.token";

    // Act
    String token = tokenJwtService.extractTokenFromHeader(authHeader);

    // Assert
    assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.test.token", token);
  }

  @Test
  void testExtractTokenFromHeader_NullHeader() {
    // Act & Assert
    assertThrows(SecurityException.class,
        () -> tokenJwtService.extractTokenFromHeader(null));
  }

  @Test
  void testExtractTokenFromHeader_EmptyHeader() {
    // Act & Assert
    assertThrows(SecurityException.class,
        () -> tokenJwtService.extractTokenFromHeader(""));
  }

  @Test
  void testExtractTokenFromHeader_WhitespaceHeader() {
    // Act & Assert
    assertThrows(SecurityException.class,
        () -> tokenJwtService.extractTokenFromHeader("   "));
  }

  @Test
  void testExtractTokenFromHeader_MissingBearerPrefix() {
    // Act & Assert
    assertThrows(SecurityException.class,
        () -> tokenJwtService.extractTokenFromHeader("Token eyJ0eXAi..."));
  }

  @Test
  void testExtractTokenFromHeader_OnlyBearer() {
    // Act & Assert
    assertThrows(SecurityException.class,
        () -> tokenJwtService.extractTokenFromHeader("Bearer "));
  }

  @Test
  void testExtractTokenFromHeader_BearerWithWhitespace() {
    // Act & Assert
    assertThrows(SecurityException.class,
        () -> tokenJwtService.extractTokenFromHeader("Bearer    "));
  }

  @Test
  void testExtractTokenFromHeader_WithExtraSpaces() {
    // Arrange
    String authHeader = "Bearer    eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.test.token   ";

    // Act
    String token = tokenJwtService.extractTokenFromHeader(authHeader);

    // Assert
    assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.test.token", token);
  }

  @Test
  void testInvalidPrivateKey() {
    // Arrange
    TokenJwtService invalidService = new TokenJwtService();
    ReflectionTestUtils.setField(invalidService, "privateKeyString", "invalid-key");
    ReflectionTestUtils.setField(invalidService, "publicKeyString", publicKeyString);
    ReflectionTestUtils.setField(invalidService, "jwtExpiration", 3600L);

    // Act & Assert
    assertThrows(RuntimeException.class,
        () -> invalidService.generateToken("user123", "testuser", "STUDENT"));
  }

  @Test
  void testInvalidPublicKey() {
    // Arrange
    TokenDto validToken = tokenJwtService.generateToken("user123", "testuser", "STUDENT");

    TokenJwtService invalidService = new TokenJwtService();
    ReflectionTestUtils.setField(invalidService, "privateKeyString", privateKeyString);
    ReflectionTestUtils.setField(invalidService, "publicKeyString", "invalid-key");
    ReflectionTestUtils.setField(invalidService, "jwtExpiration", 3600L);

    // Act & Assert
    assertThrows(RuntimeException.class,
        () -> invalidService.extractUserId(validToken.token()));
  }
}