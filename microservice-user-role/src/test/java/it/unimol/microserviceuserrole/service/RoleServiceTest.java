package it.unimol.microserviceuserrole.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceuserrole.dto.converter.RoleConverter;
import it.unimol.microserviceuserrole.dto.role.RoleDto;
import it.unimol.microserviceuserrole.enums.RoleType;
import it.unimol.microserviceuserrole.exceptions.InvalidRequestException;
import it.unimol.microserviceuserrole.exceptions.UnknownUserException;
import it.unimol.microserviceuserrole.model.Role;
import it.unimol.microserviceuserrole.model.User;
import it.unimol.microserviceuserrole.repository.RoleRepository;
import it.unimol.microserviceuserrole.repository.UserRepository;
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
class RoleServiceTest {

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private RoleConverter roleConverter;

  @Mock
  private TokenJwtService tokenService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private MessageService messageService;

  @InjectMocks
  private RoleService roleService;

  private Role testRole;
  private RoleDto testRoleDto;
  private User testUser;

  @BeforeEach
  void setUp() {
    testRole = new Role("STUDENT", "Student", "Student role");
    testRoleDto = new RoleDto("STUDENT", "Student", "Student role");

    testUser = new User();
    testUser.setId("user123");
    testUser.setUsername("testuser");
    testUser.setRole(testRole);
  }

  @Test
  void testGetAllRoles() {
    // Arrange
    List<Role> roles = Arrays.asList(
        new Role("STUDENT", "Student", "Student role"),
        new Role("TEACHER", "Teacher", "Teacher role")
    );
    when(roleRepository.findAll()).thenReturn(roles);
    when(roleConverter.toDto(any(Role.class))).thenReturn(testRoleDto);

    // Act
    List<RoleDto> result = roleService.getAllRoles();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(roleRepository).findAll();
    verify(roleConverter, times(2)).toDto(any(Role.class));
  }

  @Test
  void testFindById_Found() {
    // Arrange
    when(roleRepository.findById("STUDENT")).thenReturn(Optional.of(testRole));
    when(roleConverter.toDto(testRole)).thenReturn(testRoleDto);

    // Act
    RoleDto result = roleService.findById("STUDENT");

    // Assert
    assertNotNull(result);
    assertEquals("STUDENT", result.id());
    verify(roleRepository).findById("STUDENT");
  }

  @Test
  void testFindById_NotFound() {
    // Arrange
    when(roleRepository.findById("INVALID")).thenReturn(Optional.empty());

    // Act
    RoleDto result = roleService.findById("INVALID");

    // Assert
    assertNull(result);
    verify(roleRepository).findById("INVALID");
  }

  @Test
  void testFindByName_Found() {
    // Arrange
    when(roleRepository.findByName("Student")).thenReturn(Optional.of(testRole));
    when(roleConverter.toDto(testRole)).thenReturn(testRoleDto);

    // Act
    RoleDto result = roleService.findByName("Student");

    // Assert
    assertNotNull(result);
    assertEquals("Student", result.name());
    verify(roleRepository).findByName("Student");
  }

  @Test
  void testFindByName_NotFound() {
    // Arrange
    when(roleRepository.findByName("Invalid")).thenReturn(Optional.empty());

    // Act
    RoleDto result = roleService.findByName("Invalid");

    // Assert
    assertNull(result);
    verify(roleRepository).findByName("Invalid");
  }

  @Test
  void testInitializeRoles() {
    // Arrange
    when(roleRepository.existsById(anyString())).thenReturn(false);
    when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    roleService.initializeRoles();

    // Assert
    verify(roleRepository, times(RoleType.values().length)).save(any(Role.class));
  }

  @Test
  void testInitializeRoles_RolesAlreadyExist() {
    // Arrange
    when(roleRepository.existsById(anyString())).thenReturn(true);

    // Act
    roleService.initializeRoles();

    // Assert
    verify(roleRepository, never()).save(any(Role.class));
  }

  @Test
  void testCheckRole_ValidToken_SufficientPermissions() {
    // Arrange
    String token = "valid.token";
    when(tokenService.isTokenValid(token)).thenReturn(true);
    when(tokenService.extractRole(token)).thenReturn("Admin");

    // Act & Assert
    assertDoesNotThrow(() -> roleService.checkRole(token, RoleType.STUDENT));
    verify(tokenService).isTokenValid(token);
    verify(tokenService).extractRole(token);
  }

  @Test
  void testCheckRole_InvalidToken() {
    // Arrange
    String token = "invalid.token";
    when(tokenService.isTokenValid(token)).thenReturn(false);

    // Act & Assert
    assertThrows(SecurityException.class,
        () -> roleService.checkRole(token, RoleType.STUDENT));
    verify(tokenService).isTokenValid(token);
  }

  @Test
  void testCheckRole_InsufficientPermissions() {
    // Arrange
    String token = "valid.token";
    when(tokenService.isTokenValid(token)).thenReturn(true);
    when(tokenService.extractRole(token)).thenReturn("Student");

    // Act & Assert
    assertThrows(SecurityException.class,
        () -> roleService.checkRole(token, RoleType.ADMIN));
  }

  @Test
  void testCheckRole_UnrecognizedRole() {
    // Arrange
    String token = "valid.token";
    when(tokenService.isTokenValid(token)).thenReturn(true);
    when(tokenService.extractRole(token)).thenReturn("InvalidRole");

    // Act & Assert
    assertThrows(SecurityException.class,
        () -> roleService.checkRole(token, RoleType.STUDENT));
  }

  @Test
  void testAssignRole_Success() throws UnknownUserException, InvalidRequestException {
    // Arrange
    Role newRole = new Role("TEACHER", "Teacher", "Teacher role");
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(roleRepository.findById("TEACHER")).thenReturn(Optional.of(newRole));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    boolean result = roleService.assignRole("user123", "TEACHER");

    // Assert
    assertTrue(result);
    verify(userRepository).save(testUser);
    verify(messageService).publishRoleAssigned("user123", "TEACHER");
  }

  @Test
  void testAssignRole_UserNotFound() {
    // Arrange
    when(userRepository.findById("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UnknownUserException.class,
        () -> roleService.assignRole("invalid", "TEACHER"));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testAssignRole_RoleNotFound() {
    // Arrange
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(roleRepository.findById("INVALID")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(InvalidRequestException.class,
        () -> roleService.assignRole("user123", "INVALID"));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testAssignRole_UserAlreadyHasRole() throws UnknownUserException, InvalidRequestException {
    // Arrange
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(roleRepository.findById("STUDENT")).thenReturn(Optional.of(testRole));

    // Act
    boolean result = roleService.assignRole("user123", "STUDENT");

    // Assert
    assertFalse(result);
    verify(userRepository, never()).save(any(User.class));
    verify(messageService, never()).publishRoleAssigned(anyString(), anyString());
  }

  @Test
  void testRemoveRole_Success() throws UnknownUserException {
    // Arrange
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    boolean result = roleService.removeRole("user123");

    // Assert
    assertTrue(result);
    assertNull(testUser.getRole());
    verify(userRepository).save(testUser);
  }

  @Test
  void testRemoveRole_UserNotFound() {
    // Arrange
    when(userRepository.findById("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UnknownUserException.class,
        () -> roleService.removeRole("invalid"));
  }

  @Test
  void testRemoveRole_UserHasNoRole() throws UnknownUserException {
    // Arrange
    testUser.setRole(null);
    when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

    // Act
    boolean result = roleService.removeRole("user123");

    // Assert
    assertFalse(result);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testRoleExists_True() {
    // Arrange
    when(roleRepository.existsById("STUDENT")).thenReturn(true);

    // Act
    boolean result = roleService.roleExists("STUDENT");

    // Assert
    assertTrue(result);
    verify(roleRepository).existsById("STUDENT");
  }

  @Test
  void testRoleExists_False() {
    // Arrange
    when(roleRepository.existsById("INVALID")).thenReturn(false);

    // Act
    boolean result = roleService.roleExists("INVALID");

    // Assert
    assertFalse(result);
    verify(roleRepository).existsById("INVALID");
  }
}