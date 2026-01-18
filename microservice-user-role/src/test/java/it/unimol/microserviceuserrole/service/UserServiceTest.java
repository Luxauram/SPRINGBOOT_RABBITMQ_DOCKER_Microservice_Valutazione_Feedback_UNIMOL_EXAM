package it.unimol.microserviceuserrole.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceuserrole.dto.converter.UserConverter;
import it.unimol.microserviceuserrole.dto.role.RoleDto;
import it.unimol.microserviceuserrole.dto.user.CreateUserDto;
import it.unimol.microserviceuserrole.dto.user.UpdateUserProfileDto;
import it.unimol.microserviceuserrole.dto.user.UserDto;
import it.unimol.microserviceuserrole.dto.user.UserProfileDto;
import it.unimol.microserviceuserrole.exceptions.InvalidRequestException;
import it.unimol.microserviceuserrole.exceptions.UnknownUserException;
import it.unimol.microserviceuserrole.model.Role;
import it.unimol.microserviceuserrole.model.User;
import it.unimol.microserviceuserrole.repository.RoleRepository;
import it.unimol.microserviceuserrole.repository.UserRepository;
import it.unimol.microserviceuserrole.util.PasswordUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private UserConverter userConverter;

  @Mock
  private TokenJwtService tokenService;

  @Mock
  private MessageService messageService;

  @InjectMocks
  private UserService userService;

  private User testUser;
  private Role testRole;
  private UserDto testUserDto;
  private UserProfileDto testUserProfileDto;
  private CreateUserDto createUserDto;
  private UpdateUserProfileDto updateUserProfileDto;

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

    testUserProfileDto = new UserProfileDto("user123", "testuser", "test@example.com",
        "John", "Doe", "STUDENT", null, null);

    createUserDto = new CreateUserDto("newuser", "new@example.com",
        "John", "Doe", "password123", "STUDENT");

    updateUserProfileDto = new UpdateUserProfileDto("John", "Doe", null, null);
  }

  @Test
  void testCreateSuperAdminIfNotExists_Success() throws InvalidRequestException {
    // Arrange
    when(userRepository.countSuperAdmins()).thenReturn(0L);
    Role superAdminRole = new Role("SUPER_ADMIN", "Super Admin", "Admin");
    when(roleRepository.findById("SUPER_ADMIN")).thenReturn(Optional.of(superAdminRole));

    User superAdmin = new User();
    superAdmin.setId("000000");
    when(userConverter.toEntity(any(CreateUserDto.class), any(Role.class))).thenReturn(superAdmin);
    when(userRepository.save(any(User.class))).thenReturn(superAdmin);
    when(userConverter.toDto(any(User.class))).thenReturn(testUserDto);

    // Act
    UserDto result = userService.createSuperAdminIfNotExists(createUserDto);

    // Assert
    assertNotNull(result);
    verify(userRepository).countSuperAdmins();
    verify(userRepository).save(any(User.class));
    verify(messageService).publishUserCreated(any(UserDto.class));
  }

  @Test
  void testCreateSuperAdminIfNotExists_AlreadyExists() {
    // Arrange
    when(userRepository.countSuperAdmins()).thenReturn(1L);

    // Act & Assert
    assertThrows(InvalidRequestException.class,
        () -> userService.createSuperAdminIfNotExists(createUserDto));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testCreateSuperAdminIfNotExists_RoleNotFound() {
    // Arrange
    when(userRepository.countSuperAdmins()).thenReturn(0L);
    when(roleRepository.findById("SUPER_ADMIN")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(InvalidRequestException.class,
        () -> userService.createSuperAdminIfNotExists(createUserDto));
  }

  @Test
  void testCreateUser_Success() throws InvalidRequestException {
    // Arrange
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(roleRepository.findById("STUDENT")).thenReturn(Optional.of(testRole));
    when(userConverter.toEntity(any(CreateUserDto.class), any(Role.class))).thenReturn(testUser);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userConverter.toDto(any(User.class))).thenReturn(testUserDto);

    // Act
    UserDto result = userService.createUser(createUserDto);

    // Assert
    assertNotNull(result);
    assertEquals("testuser", result.username());
    verify(messageService).publishUserCreated(any(UserDto.class));
  }

  @Test
  void testCreateUser_UsernameExists() {
    // Arrange
    when(userRepository.existsByUsername(anyString())).thenReturn(true);

    // Act & Assert
    assertThrows(InvalidRequestException.class,
        () -> userService.createUser(createUserDto));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testCreateUser_EmailExists() {
    // Arrange
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(true);

    // Act & Assert
    assertThrows(InvalidRequestException.class,
        () -> userService.createUser(createUserDto));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testCreateUser_WithCustomRole() throws InvalidRequestException {
    CreateUserDto customRoleDto = new CreateUserDto("newuser", "new@example.com",
        "John", "Doe", "password123", "TEACHER");
    Role teacherRole = new Role("TEACHER", "Teacher", "Teacher role");

    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(roleRepository.findById("TEACHER")).thenReturn(Optional.of(teacherRole));
    when(userConverter.toEntity(any(CreateUserDto.class), any(Role.class))).thenReturn(testUser);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userConverter.toDto(any(User.class))).thenReturn(testUserDto);

    // Act
    UserDto result = userService.createUser(customRoleDto);

    // Assert
    assertNotNull(result);
    verify(roleRepository).findById("TEACHER");
  }

  @Test
  void testCreateUser_RoleNotFound() {
    CreateUserDto customRoleDto = new CreateUserDto("newuser", "new@example.com",
        "John", "Doe", "password123", "INVALID_ROLE");
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(roleRepository.findById("INVALID_ROLE")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(InvalidRequestException.class,
        () -> userService.createUser(customRoleDto));
  }

  @Test
  void testGetAllUsers() {
    // Arrange
    List<User> users = Arrays.asList(testUser);
    when(userRepository.findAll()).thenReturn(users);
    when(userConverter.toProfileDto(any(User.class))).thenReturn(testUserProfileDto);

    // Act
    List<UserProfileDto> result = userService.getAllUsers();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(userRepository).findAll();
  }

  @Test
  void testFindById_Success() throws UnknownUserException {
    // Arrange
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(userConverter.toDto(any(User.class))).thenReturn(testUserDto);

    // Act
    UserDto result = userService.findById("user123");

    // Assert
    assertNotNull(result);
    assertEquals("user123", result.id());
    verify(userRepository).findById("user123");
  }

  @Test
  void testFindById_NotFound() {
    // Arrange
    when(userRepository.findById("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UnknownUserException.class,
        () -> userService.findById("invalid"));
  }

  @Test
  void testFindByUsername_Success() throws UnknownUserException {
    // Arrange
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(userConverter.toDto(any(User.class))).thenReturn(testUserDto);

    // Act
    UserDto result = userService.findByUsername("testuser");

    // Assert
    assertNotNull(result);
    assertEquals("testuser", result.username());
  }

  @Test
  void testFindByUsername_NotFound() {
    // Arrange
    when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UnknownUserException.class,
        () -> userService.findByUsername("invalid"));
  }

  @Test
  void testExistsById_True() {
    // Arrange
    when(userRepository.existsById("user123")).thenReturn(true);

    // Act
    boolean result = userService.existsById("user123");

    // Assert
    assertTrue(result);
  }

  @Test
  void testExistsById_False() {
    // Arrange
    when(userRepository.existsById("invalid")).thenReturn(false);

    // Act
    boolean result = userService.existsById("invalid");

    // Assert
    assertFalse(result);
  }

  @Test
  void testUpdateUser_Success() throws UnknownUserException {
    // Arrange
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userConverter.toDto(any(User.class))).thenReturn(testUserDto);
    doNothing().when(userConverter).updateEntity(any(User.class), any(UpdateUserProfileDto.class));

    // Act
    UserDto result = userService.updateUser("user123", updateUserProfileDto);

    // Assert
    assertNotNull(result);
    verify(userConverter).updateEntity(any(User.class), any(UpdateUserProfileDto.class));
    verify(messageService).publishUserUpdated(any(UserDto.class));
  }

  @Test
  void testUpdateUser_NotFound() {
    // Arrange
    when(userRepository.findById("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UnknownUserException.class,
        () -> userService.updateUser("invalid", updateUserProfileDto));
  }

  @Test
  void testDeleteUser_Success() {
    // Arrange
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    doNothing().when(userRepository).delete(any(User.class));

    // Act
    boolean result = userService.deleteUser("user123");

    // Assert
    assertTrue(result);
    verify(userRepository).delete(testUser);
    verify(messageService).publishUserDeleted("user123");
  }

  @Test
  void testDeleteUser_NotFound() {
    // Arrange
    when(userRepository.findById("invalid")).thenReturn(Optional.empty());

    // Act
    boolean result = userService.deleteUser("invalid");

    // Assert
    assertFalse(result);
    verify(userRepository, never()).delete(any(User.class));
    verify(messageService, never()).publishUserDeleted(anyString());
  }

  @Test
  void testGetCurrentUserProfile_Success() throws UnknownUserException {
    // Arrange
    String token = "valid.token.here";
    when(tokenService.extractUserId(token)).thenReturn("user123");
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(userConverter.toProfileDto(any(User.class))).thenReturn(testUserProfileDto);

    // Act
    UserProfileDto result = userService.getCurrentUserProfile(token);

    // Assert
    assertNotNull(result);
    assertEquals("user123", result.id());
  }

  @Test
  void testGetCurrentUserProfile_UserNotFound() {
    // Arrange
    String token = "valid.token.here";
    when(tokenService.extractUserId(token)).thenReturn("invalid");
    when(userRepository.findById("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UnknownUserException.class,
        () -> userService.getCurrentUserProfile(token));
  }

  @Test
  void testUpdateCurrentUserProfile_Success() throws UnknownUserException {
    // Arrange
    String token = "valid.token.here";
    when(tokenService.extractUserId(token)).thenReturn("user123");
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userConverter.toProfileDto(any(User.class))).thenReturn(testUserProfileDto);
    doNothing().when(userConverter).updateEntity(any(User.class), any(UpdateUserProfileDto.class));

    // Act
    UserProfileDto result = userService.updateCurrentUserProfile(token, updateUserProfileDto);

    // Assert
    assertNotNull(result);
    verify(messageService).publishProfileUpdated(any(UserProfileDto.class));
  }

  @Test
  void testChangePassword_Success() throws UnknownUserException {
    // Arrange
    String token = "valid.token.here";
    String currentPassword = "password123";
    String newPassword = "newPassword456";

    when(tokenService.extractUserId(token)).thenReturn("user123");
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    boolean result = userService.changePassword(token, currentPassword, newPassword);

    // Assert
    assertTrue(result);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void testChangePassword_WrongCurrentPassword() throws UnknownUserException {
    // Arrange
    String token = "valid.token.here";
    String wrongPassword = "wrongPassword";
    String newPassword = "newPassword456";

    when(tokenService.extractUserId(token)).thenReturn("user123");
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

    // Act
    boolean result = userService.changePassword(token, wrongPassword, newPassword);

    // Assert
    assertFalse(result);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testChangePassword_UserNotFound() {
    // Arrange
    String token = "valid.token.here";
    when(tokenService.extractUserId(token)).thenReturn("invalid");
    when(userRepository.findById("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UnknownUserException.class,
        () -> userService.changePassword(token, "password", "newPassword"));
  }

  @Test
  void testResetPassword_Success() throws UnknownUserException {
    // Arrange
    String token = "valid.token.here";
    String currentPassword = "password123";

    when(tokenService.extractUserId(token)).thenReturn("user123");
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    String tempPassword = userService.resetPassword(token, currentPassword);

    // Assert
    assertNotNull(tempPassword);
    assertEquals(12, tempPassword.length());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void testResetPassword_WrongPassword() {
    // Arrange
    String token = "valid.token.here";
    String wrongPassword = "wrongPassword";

    when(tokenService.extractUserId(token)).thenReturn("user123");
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

    // Act & Assert
    assertThrows(SecurityException.class,
        () -> userService.resetPassword(token, wrongPassword));
  }

  @Test
  void testResetPassword_UserNotFound() {
    // Arrange
    String token = "valid.token.here";
    when(tokenService.extractUserId(token)).thenReturn("invalid");
    when(userRepository.findById("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UnknownUserException.class,
        () -> userService.resetPassword(token, "password"));
  }
}