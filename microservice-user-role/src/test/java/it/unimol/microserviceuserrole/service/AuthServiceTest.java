package it.unimol.microserviceuserrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceuserrole.dto.auth.TokenDto;
import it.unimol.microserviceuserrole.dto.converter.UserConverter;
import it.unimol.microserviceuserrole.dto.role.RoleDto;
import it.unimol.microserviceuserrole.dto.user.UserDto;
import it.unimol.microserviceuserrole.exceptions.AuthException;
import it.unimol.microserviceuserrole.exceptions.UnknownUserException;
import it.unimol.microserviceuserrole.model.Role;
import it.unimol.microserviceuserrole.model.User;
import it.unimol.microserviceuserrole.repository.UserRepository;
import it.unimol.microserviceuserrole.util.PasswordUtils;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private TokenJwtService tokenService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserConverter userConverter;

  @Mock
  private MessageService messageService;

  @InjectMocks
  private AuthService authService;

  private User testUser;
  private Role testRole;
  private UserDto testUserDto;
  private TokenDto testTokenDto;

  @BeforeEach
  void setUp() {
    testRole = new Role("STUDENT", "Student", "Student role");

    testUser = new User();
    testUser.setId("user123");
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword(PasswordUtils.hashPassword("password123"));
    testUser.setRole(testRole);

    RoleDto testRoleDto = new RoleDto("STUDENT", "Student", "Student role");
    testUserDto = new UserDto("user123", "testuser", "test@example.com",
        "John", "Doe", null, null, testRoleDto);

    testTokenDto = new TokenDto("token.jwt.string", "Bearer", 3600L);
  }

  @Test
  void testRegister_Success() throws AuthException {
    // Arrange
    User newUser = new User();
    newUser.setUsername("newuser");
    newUser.setPassword("plainPassword");
    newUser.setRole(testRole);

    when(userRepository.save(any(User.class))).thenReturn(newUser);
    when(userConverter.toDto(any(User.class))).thenReturn(testUserDto);

    // Act
    authService.register(newUser);

    // Assert
    verify(userRepository).save(any(User.class));
    verify(messageService).publishUserCreated(any(UserDto.class));
    // Verify password was hashed
    assertNotEquals("plainPassword", newUser.getPassword());
  }

  @Test
  void testRegister_SuperAdminAttempt() {
    // Arrange
    Role superAdminRole = new Role("sadmin", "Super Admin", "Super Admin");
    User superAdminUser = new User();
    superAdminUser.setUsername("admin");
    superAdminUser.setPassword("password");
    superAdminUser.setRole(superAdminRole);

    // Act & Assert
    assertThrows(AuthException.class, () -> authService.register(superAdminUser));
    verify(userRepository, never()).save(any(User.class));
    verify(messageService, never()).publishUserCreated(any(UserDto.class));
  }

  @Test
  void testRegister_RepositoryException() {
    // Arrange
    User newUser = new User();
    newUser.setUsername("newuser");
    newUser.setPassword("plainPassword");
    newUser.setRole(testRole);

    when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

    // Act & Assert
    assertThrows(AuthException.class, () -> authService.register(newUser));
    verify(messageService, never()).publishUserCreated(any(UserDto.class));
  }

  @Test
  void testLogin_Success() throws AuthException, UnknownUserException {
    // Arrange
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(tokenService.generateToken(anyString(), anyString(), anyString())).thenReturn(testTokenDto);

    // Act
    TokenDto result = authService.login("testuser", "password123");

    // Assert
    assertNotNull(result);
    assertEquals("token.jwt.string", result.token());
    verify(userRepository).save(any(User.class));
    verify(tokenService).generateToken("user123", "testuser", "STUDENT");
    assertNotNull(testUser.getLastLogin());
  }

  @Test
  void testLogin_UserNotFound() {
    // Arrange
    when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(AuthException.class,
        () -> authService.login("invaliduser", "password123"));
    verify(tokenService, never()).generateToken(anyString(), anyString(), anyString());
  }

  @Test
  void testLogin_WrongPassword() {
    // Arrange
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    // Act & Assert
    assertThrows(AuthException.class,
        () -> authService.login("testuser", "wrongpassword"));
    verify(tokenService, never()).generateToken(anyString(), anyString(), anyString());
  }

  @Test
  @SuppressWarnings("JavaUtilDate")
  void testLogin_UpdatesLastLogin() throws AuthException, UnknownUserException {
    // Arrange
    LocalDateTime beforeLogin = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(1);
    testUser.setLastLogin(beforeLogin);

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(tokenService.generateToken(anyString(), anyString(), anyString())).thenReturn(testTokenDto);

    // Act
    authService.login("testuser", "password123");

    // Assert
    assertNotNull(testUser.getLastLogin());
    assertTrue(testUser.getLastLogin().isAfter(beforeLogin));
  }

  @Test
  void testLogout() {
    // Arrange
    String token = "token.to.invalidate";

    // Act
    authService.logout(token);

    // Assert
    verify(tokenService).invalidateToken(token);
  }

  @Test
  void testRefreshToken_Success() {
    // Arrange
    String oldToken = "old.token.string";
    TokenDto newTokenDto = new TokenDto("new.token.string", "Bearer", 3600L);
    when(tokenService.refreshToken(oldToken)).thenReturn(newTokenDto);

    // Act
    TokenDto result = authService.refreshToken(oldToken);

    // Assert
    assertNotNull(result);
    assertEquals("new.token.string", result.token());
    verify(tokenService).refreshToken(oldToken);
  }

  @Test
  void testRefreshToken_Failure() {
    // Arrange
    String invalidToken = "invalid.token";
    when(tokenService.refreshToken(invalidToken))
        .thenThrow(new RuntimeException("Token già invalidato"));

    // Act & Assert
    assertThrows(RuntimeException.class,
        () -> authService.refreshToken(invalidToken));
  }

  @Test
  void testUpdateLastLogin_UserExists() {
    // Arrange
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    authService.updateLastLogin("user123");

    // Assert
    verify(userRepository).findById("user123");
    verify(userRepository).save(testUser);
  }

  @Test
  void testUpdateLastLogin_UserNotFound() {
    // Arrange
    when(userRepository.findById("invalid")).thenReturn(Optional.empty());

    // Act
    authService.updateLastLogin("invalid");

    // Assert
    verify(userRepository).findById("invalid");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @SuppressWarnings("JavaUtilDate")
  void testUpdateLastLogin_UpdatesTimestamp() {
    // Arrange
    LocalDateTime before = LocalDateTime.now(ZoneId.systemDefault()).minusHours(1);
    testUser.setLastLogin(before);

    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User savedUser = invocation.getArgument(0);
      assertTrue(savedUser.getLastLogin().isAfter(before));
      return savedUser;
    });

    // Act
    authService.updateLastLogin("user123");

    // Assert
    verify(userRepository).save(testUser);
  }

  @Test
  void testRegister_WithNullRole() {
    // Arrange
    User userWithNullRole = new User();
    userWithNullRole.setUsername("newuser");
    userWithNullRole.setPassword("password");
    userWithNullRole.setRole(null);

    // Act & Assert
    assertThrows(AuthException.class, () -> authService.register(userWithNullRole));
  }

  @Test
  void testLogin_EmptyPassword() {
    // Arrange
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    // Act & Assert
    assertThrows(AuthException.class,
        () -> authService.login("testuser", ""));
  }

  @Test
  void testLogin_NullPassword() {
    // Arrange
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    // Act & Assert
    assertThrows(NullPointerException.class,
        () -> authService.login("testuser", null));
  }
}