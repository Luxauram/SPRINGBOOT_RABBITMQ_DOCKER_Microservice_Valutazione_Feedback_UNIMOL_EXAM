package it.unimol.microserviceuserrole.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceuserrole.dto.auth.LoginDto;
import it.unimol.microserviceuserrole.dto.auth.TokenDto;
import it.unimol.microserviceuserrole.exceptions.AuthException;
import it.unimol.microserviceuserrole.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private AuthService authService;

  @InjectMocks
  private AuthController authController;

  private LoginDto loginDto;
  private TokenDto tokenDto;
  private String authHeader;

  @BeforeEach
  void setUp() {
    loginDto = new LoginDto("testuser", "password123");
    tokenDto = new TokenDto("jwt.token.here", "Bearer", 3600L);
    authHeader = "Bearer jwt.token.here";
  }

  @Test
  void testLogin_Success() throws Exception {
    // Arrange
    when(authService.login("testuser", "password123")).thenReturn(tokenDto);

    // Act
    ResponseEntity<TokenDto> response = authController.login(loginDto);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("jwt.token.here", response.getBody().token());
    verify(authService).login("testuser", "password123");
  }

  @Test
  void testLogin_AuthException() throws Exception {
    // Arrange
    when(authService.login(anyString(), anyString()))
        .thenThrow(new AuthException("Invalid credentials"));

    // Act
    ResponseEntity<TokenDto> response = authController.login(loginDto);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNull(response.getBody());
    verify(authService).login("testuser", "password123");
  }

  @Test
  void testLogin_GenericException() throws Exception {
    // Arrange
    when(authService.login(anyString(), anyString()))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<TokenDto> response = authController.login(loginDto);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  void testLogout_Success() {
    // Arrange
    doNothing().when(authService).logout("jwt.token.here");

    // Act
    authController.logout(authHeader);

    // Assert
    verify(authService).logout("jwt.token.here");
  }

  @Test
  void testLogout_WithoutBearerPrefix() {
    // Arrange
    String tokenWithoutBearer = "jwt.token.here";
    doNothing().when(authService).logout("jwt.token.here");

    // Act
    authController.logout(tokenWithoutBearer);

    // Assert
    verify(authService).logout("jwt.token.here");
  }

  @Test
  void testRefreshToken_Success() {
    // Arrange
    TokenDto newToken = new TokenDto("new.jwt.token", "Bearer", 3600L);
    when(authService.refreshToken("jwt.token.here")).thenReturn(newToken);

    // Act
    ResponseEntity<TokenDto> response = authController.refreshToken(authHeader);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("new.jwt.token", response.getBody().token());
    verify(authService).refreshToken("jwt.token.here");
  }

  @Test
  void testRefreshToken_Exception() {
    // Arrange
    when(authService.refreshToken(anyString()))
        .thenThrow(new RuntimeException("Token expired"));

    // Act
    ResponseEntity<TokenDto> response = authController.refreshToken(authHeader);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  void testRefreshToken_WithoutBearerPrefix() {
    // Arrange
    String tokenWithoutBearer = "jwt.token.here";
    TokenDto newToken = new TokenDto("new.jwt.token", "Bearer", 3600L);
    when(authService.refreshToken("jwt.token.here")).thenReturn(newToken);

    // Act
    ResponseEntity<TokenDto> response = authController.refreshToken(tokenWithoutBearer);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }
}