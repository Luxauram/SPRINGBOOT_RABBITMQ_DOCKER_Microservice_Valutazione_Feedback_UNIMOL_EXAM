package it.unimol.microserviceuserrole.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceuserrole.dto.auth.ChangePasswordRequestDto;
import it.unimol.microserviceuserrole.dto.role.AssignRoleDto;
import it.unimol.microserviceuserrole.dto.role.RoleDto;
import it.unimol.microserviceuserrole.dto.user.CreateUserDto;
import it.unimol.microserviceuserrole.dto.user.UpdateUserProfileDto;
import it.unimol.microserviceuserrole.dto.user.UserDto;
import it.unimol.microserviceuserrole.dto.user.UserProfileDto;
import it.unimol.microserviceuserrole.enums.RoleType;
import it.unimol.microserviceuserrole.exceptions.InvalidRequestException;
import it.unimol.microserviceuserrole.exceptions.UnknownUserException;
import it.unimol.microserviceuserrole.service.RoleService;
import it.unimol.microserviceuserrole.service.TokenJwtService;
import it.unimol.microserviceuserrole.service.UserService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock
  private UserService userService;

  @Mock
  private RoleService roleService;

  @Mock
  private TokenJwtService tokenService;

  @InjectMocks
  private UserController userController;

  private String authHeader;
  private String token;
  private CreateUserDto createUserDto;
  private UpdateUserProfileDto updateUserProfileDto;
  private UserDto userDto;
  private UserProfileDto userProfileDto;
  private RoleDto roleDto;
  private ChangePasswordRequestDto changePasswordDto;
  private AssignRoleDto assignRoleDto;

  @BeforeEach
  @SuppressWarnings("JavaUtilDate")
  void setUp() {
    authHeader = "Bearer jwt.token.here";
    token = "jwt.token.here";

    roleDto = new RoleDto("STUDENT", "Student", "Student role");

    createUserDto = new CreateUserDto(
        "testuser",
        "test@example.com",
        "John",
        "Doe",
        "password123",
        "STUDENT"
    );

    updateUserProfileDto = new UpdateUserProfileDto("John", "Doe", null, null);

    userDto = new UserDto(
        "user123",
        "testuser",
        "test@example.com",
        "John",
        "Doe",
        LocalDateTime.now(ZoneId.systemDefault()),
        LocalDateTime.now(ZoneId.systemDefault()),
        roleDto
    );

    userProfileDto = new UserProfileDto(
        "user123",
        "testuser",
        "test@example.com",
        "John",
        "Doe",
        "STUDENT",
        LocalDateTime.now(ZoneId.systemDefault()),
        LocalDateTime.now(ZoneId.systemDefault())
    );

    changePasswordDto = new ChangePasswordRequestDto("oldPassword", "newPassword123");
    assignRoleDto = new AssignRoleDto("TEACHER");
  }

  // ========== SUPERADMIN TESTS ==========

  @Test
  void testCreateSuperAdmin_Success() throws Exception {
    // Arrange
    when(userService.createSuperAdminIfNotExists(any(CreateUserDto.class))).thenReturn(userDto);

    // Act
    ResponseEntity<UserDto> response = userController.createSuperAdmin(createUserDto);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("user123", response.getBody().id());
    verify(userService).createSuperAdminIfNotExists(createUserDto);
  }

  @Test
  void testCreateSuperAdmin_AlreadyExists() throws Exception {
    // Arrange
    when(userService.createSuperAdminIfNotExists(any(CreateUserDto.class)))
        .thenThrow(new InvalidRequestException("SuperAdmin already exists"));

    // Act
    ResponseEntity<UserDto> response = userController.createSuperAdmin(createUserDto);

    // Assert
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  void testCreateSuperAdmin_GenericException() throws Exception {
    // Arrange
    when(userService.createSuperAdminIfNotExists(any(CreateUserDto.class)))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<UserDto> response = userController.createSuperAdmin(createUserDto);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // ========== CREATE USER TESTS ==========

  @Test
  void testCreateUser_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.createUser(any(CreateUserDto.class))).thenReturn(userDto);

    // Act
    ResponseEntity<UserDto> response = userController.createUser(authHeader, createUserDto);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(roleService).checkRole(token, RoleType.ADMIN);
    verify(userService).createUser(createUserDto);
  }

  @Test
  void testCreateUser_Forbidden() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.ADMIN);

    // Act
    ResponseEntity<UserDto> response = userController.createUser(authHeader, createUserDto);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(userService, never()).createUser(any());
  }

  @Test
  void testCreateUser_Conflict() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.createUser(any(CreateUserDto.class)))
        .thenThrow(new InvalidRequestException("Username already exists"));

    // Act
    ResponseEntity<UserDto> response = userController.createUser(authHeader, createUserDto);

    // Assert
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void testCreateUser_BadRequest() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.createUser(any(CreateUserDto.class)))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<UserDto> response = userController.createUser(authHeader, createUserDto);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // ========== GET ALL USERS TESTS ==========

  @Test
  void testGetAllUsers_Success() {
    // Arrange
    List<UserProfileDto> users = Arrays.asList(userProfileDto);
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.getAllUsers()).thenReturn(users);

    // Act
    ResponseEntity<List<UserProfileDto>> response = userController.getAllUsers(authHeader);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(userService).getAllUsers();
  }

  @Test
  void testGetAllUsers_Forbidden() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.ADMIN);

    // Act
    ResponseEntity<List<UserProfileDto>> response = userController.getAllUsers(authHeader);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(userService, never()).getAllUsers();
  }

  @Test
  void testGetAllUsers_InternalServerError() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.getAllUsers()).thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<List<UserProfileDto>> response = userController.getAllUsers(authHeader);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  // ========== GET USER BY ID TESTS ==========

  @Test
  void testGetUserById_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.findById("user123")).thenReturn(userDto);

    // Act
    ResponseEntity<?> response = userController.getUserById(authHeader, "user123");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(userDto, response.getBody());
    verify(userService).findById("user123");
  }

  @Test
  void testGetUserById_NotFound() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.findById("invalid"))
        .thenThrow(new UnknownUserException("User not found"));

    // Act
    ResponseEntity<?> response = userController.getUserById(authHeader, "invalid");

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(response.getBody() instanceof Map);
  }

  @Test
  void testGetUserById_Forbidden() throws UnknownUserException {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.ADMIN);

    // Act
    ResponseEntity<?> response =
        userController.getUserById(authHeader, "user123");

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(userService, never()).findById(anyString());
  }


  @Test
  void testGetUserById_TokenExtractionError() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader))
        .thenThrow(new RuntimeException("Invalid token"));

    // Act
    ResponseEntity<?> response = userController.getUserById(authHeader, "user123");

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetUserById_GenericError() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.findById(anyString()))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<?> response = userController.getUserById(authHeader, "user123");

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  // ========== UPDATE USER TESTS ==========

  @Test
  void testUpdateUser_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.updateUser("user123", updateUserProfileDto)).thenReturn(userDto);

    // Act
    ResponseEntity<UserDto> response = userController.updateUser(authHeader, "user123", updateUserProfileDto);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(userService).updateUser("user123", updateUserProfileDto);
  }

  @Test
  void testUpdateUser_Forbidden() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.ADMIN);

    // Act
    ResponseEntity<UserDto> response = userController.updateUser(authHeader, "user123", updateUserProfileDto);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(userService, never()).updateUser(anyString(), any());
  }

  @Test
  void testUpdateUser_NotFound() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.updateUser(anyString(), any()))
        .thenThrow(new UnknownUserException("User not found"));

    // Act
    ResponseEntity<UserDto> response = userController.updateUser(authHeader, "invalid", updateUserProfileDto);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testUpdateUser_BadRequest() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.updateUser(anyString(), any()))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<UserDto> response = userController.updateUser(authHeader, "user123", updateUserProfileDto);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // ========== DELETE USER TESTS ==========

  @Test
  void testDeleteUser_Success() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(tokenService.extractUserId(token)).thenReturn("admin123");
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.deleteUser("user123")).thenReturn(true);

    // Act
    ResponseEntity<Void> response = userController.deleteUser(authHeader, "user123");

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(userService).deleteUser("user123");
  }

  @Test
  void testDeleteUser_SelfDeletion() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(tokenService.extractUserId(token)).thenReturn("user123");
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);

    // Act
    ResponseEntity<Void> response = userController.deleteUser(authHeader, "user123");

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(userService, never()).deleteUser(anyString());
  }

  @Test
  void testDeleteUser_NotFound() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(tokenService.extractUserId(token)).thenReturn("admin123");
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.deleteUser("invalid")).thenReturn(false);

    // Act
    ResponseEntity<Void> response = userController.deleteUser(authHeader, "invalid");

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testDeleteUser_Forbidden() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.ADMIN);

    // Act
    ResponseEntity<Void> response = userController.deleteUser(authHeader, "user123");

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testDeleteUser_InternalServerError() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(tokenService.extractUserId(token)).thenReturn("admin123");
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(userService.deleteUser(anyString()))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<Void> response = userController.deleteUser(authHeader, "user123");

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  // ========== PROFILE TESTS ==========

  @Test
  void testGetUserProfile_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(userService.getCurrentUserProfile(token)).thenReturn(userProfileDto);

    // Act
    ResponseEntity<UserProfileDto> response = userController.getUserProfile(authHeader);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(userService).getCurrentUserProfile(token);
  }

  @Test
  void testGetUserProfile_NotFound() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(userService.getCurrentUserProfile(token))
        .thenThrow(new UnknownUserException("User not found"));

    // Act
    ResponseEntity<UserProfileDto> response = userController.getUserProfile(authHeader);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testGetUserProfile_Unauthorized() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader))
        .thenThrow(new RuntimeException("Invalid token"));

    // Act
    ResponseEntity<UserProfileDto> response = userController.getUserProfile(authHeader);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testUpdateUserProfile_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(userService.updateCurrentUserProfile(token, updateUserProfileDto))
        .thenReturn(userProfileDto);

    // Act
    ResponseEntity<UserProfileDto> response = userController.updateUserProfile(authHeader, updateUserProfileDto);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void testUpdateUserProfile_NotFound() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(userService.updateCurrentUserProfile(any(), any()))
        .thenThrow(new UnknownUserException("User not found"));

    // Act
    ResponseEntity<UserProfileDto> response = userController.updateUserProfile(authHeader, updateUserProfileDto);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testUpdateUserProfile_Unauthorized() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader))
        .thenThrow(new RuntimeException("Invalid token"));

    // Act
    ResponseEntity<UserProfileDto> response = userController.updateUserProfile(authHeader, updateUserProfileDto);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  // ========== PASSWORD TESTS ==========

  @Test
  void testChangePassword_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(userService.changePassword(token, "oldPassword", "newPassword123"))
        .thenReturn(true);

    // Act
    ResponseEntity<Void> response = userController.changePassword(authHeader, changePasswordDto);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testChangePassword_BadRequest() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(userService.changePassword(any(), anyString(), anyString())).thenReturn(false);

    // Act
    ResponseEntity<Void> response = userController.changePassword(authHeader, changePasswordDto);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testChangePassword_Unauthorized() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(userService.changePassword(any(), anyString(), anyString()))
        .thenThrow(new UnknownUserException("User not found"));

    // Act
    ResponseEntity<Void> response = userController.changePassword(authHeader, changePasswordDto);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testResetPassword_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(userService.resetPassword(token, "oldPassword")).thenReturn("tempPassword123");

    // Act
    ResponseEntity<String> response = userController.resetPassword(authHeader, changePasswordDto);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("tempPassword123", response.getBody());
  }

  @Test
  void testResetPassword_BadRequest() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(userService.resetPassword(any(), anyString()))
        .thenThrow(new SecurityException("Wrong password"));

    // Act
    ResponseEntity<String> response = userController.resetPassword(authHeader, changePasswordDto);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testResetPassword_Unauthorized() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    when(userService.resetPassword(any(), anyString()))
        .thenThrow(new UnknownUserException("User not found"));

    // Act
    ResponseEntity<String> response = userController.resetPassword(authHeader, changePasswordDto);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  // ========== ROLE ASSIGNMENT TESTS ==========

  @Test
  void testAssignRole_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.assignRole("user123", "TEACHER")).thenReturn(true);

    // Act
    ResponseEntity<Void> response = userController.assignRole(authHeader, "user123", assignRoleDto);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(roleService).assignRole("user123", "TEACHER");
  }

  @Test
  void testAssignRole_NotFound() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.assignRole(anyString(), anyString())).thenReturn(false);

    // Act
    ResponseEntity<Void> response = userController.assignRole(authHeader, "user123", assignRoleDto);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testAssignRole_Forbidden() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.ADMIN);

    // Act
    ResponseEntity<Void> response = userController.assignRole(authHeader, "user123", assignRoleDto);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testAssignRole_BadRequest() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.assignRole(anyString(), anyString()))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<Void> response = userController.assignRole(authHeader, "user123", assignRoleDto);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testUpdateUserRole_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.assignRole("user123", "TEACHER")).thenReturn(true);

    // Act
    ResponseEntity<Void> response = userController.updateUserRole(authHeader, "user123", assignRoleDto);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(roleService).assignRole("user123", "TEACHER");
  }

  @Test
  void testUpdateUserRole_NotFound() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.assignRole(anyString(), anyString())).thenReturn(false);

    // Act
    ResponseEntity<Void> response = userController.updateUserRole(authHeader, "user123", assignRoleDto);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testUpdateUserRole_Forbidden() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.ADMIN);

    // Act
    ResponseEntity<Void> response = userController.updateUserRole(authHeader, "user123", assignRoleDto);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testUpdateUserRole_BadRequest() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.assignRole(anyString(), anyString()))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<Void> response = userController.updateUserRole(authHeader, "user123", assignRoleDto);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}