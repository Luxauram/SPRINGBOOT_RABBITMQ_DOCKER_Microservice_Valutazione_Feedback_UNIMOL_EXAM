package it.unimol.microserviceuserrole.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceuserrole.dto.role.AssignRoleDto;
import it.unimol.microserviceuserrole.dto.role.RoleDto;
import it.unimol.microserviceuserrole.enums.RoleType;
import it.unimol.microserviceuserrole.exceptions.InvalidRequestException;
import it.unimol.microserviceuserrole.exceptions.UnknownUserException;
import it.unimol.microserviceuserrole.service.RoleService;
import it.unimol.microserviceuserrole.service.TokenJwtService;
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
class RoleControllerTest {

  @Mock
  private RoleService roleService;

  @Mock
  private TokenJwtService tokenService;

  @InjectMocks
  private RoleController roleController;

  private String authHeader;
  private String token;
  private RoleDto studentRole;
  private RoleDto teacherRole;
  private AssignRoleDto assignRoleDto;

  @BeforeEach
  void setUp() {
    authHeader = "Bearer jwt.token.here";
    token = "jwt.token.here";

    studentRole = new RoleDto("STUDENT", "Student", "Student role");
    teacherRole = new RoleDto("TEACHER", "Teacher", "Teacher role");
    assignRoleDto = new AssignRoleDto("TEACHER");
  }

  @Test
  void testGetAllRoles_Success() {
    // Arrange
    List<RoleDto> roles = Arrays.asList(studentRole, teacherRole);
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.getAllRoles()).thenReturn(roles);

    // Act
    ResponseEntity<?> response = roleController.getAllRoles(authHeader);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(roles, response.getBody());
    verify(roleService).checkRole(token, RoleType.ADMIN);
    verify(roleService).getAllRoles();
  }

  @Test
  void testGetAllRoles_Forbidden() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.ADMIN);

    // Act
    ResponseEntity<?> response = roleController.getAllRoles(authHeader);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody() instanceof Map);
    verify(roleService, never()).getAllRoles();
  }

  @Test
  void testGetAllRoles_InternalServerError() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.getAllRoles()).thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<?> response = roleController.getAllRoles(authHeader);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetRoleById_Success() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.findById("STUDENT")).thenReturn(studentRole);

    // Act
    ResponseEntity<?> response = roleController.getRoleById("STUDENT", authHeader);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(studentRole, response.getBody());
    verify(roleService).findById("STUDENT");
  }

  @Test
  void testGetRoleById_NotFound() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.findById("INVALID")).thenReturn(null);

    // Act
    ResponseEntity<?> response = roleController.getRoleById("INVALID", authHeader);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(response.getBody() instanceof Map);
  }

  @Test
  void testGetRoleById_Forbidden() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.ADMIN);

    // Act
    ResponseEntity<?> response = roleController.getRoleById("STUDENT", authHeader);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(roleService, never()).findById(anyString());
  }

  @Test
  void testGetRoleById_InternalServerError() {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.ADMIN);
    when(roleService.findById(anyString())).thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<?> response = roleController.getRoleById("STUDENT", authHeader);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testAssignRole_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.SUPER_ADMIN);
    when(roleService.assignRole("user123", "TEACHER")).thenReturn(true);

    // Act
    ResponseEntity<?> response = roleController.assignRole("user123", assignRoleDto, authHeader);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody() instanceof Map);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertEquals("Ruolo assegnato con successo", body.get("message"));
    verify(roleService).assignRole("user123", "TEACHER");
  }

  @Test
  void testAssignRole_AlreadyAssigned() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.SUPER_ADMIN);
    when(roleService.assignRole("user123", "TEACHER")).thenReturn(false);

    // Act
    ResponseEntity<?> response = roleController.assignRole("user123", assignRoleDto, authHeader);

    // Assert
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertTrue(response.getBody() instanceof Map);
  }

  @Test
  void testAssignRole_Forbidden() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.SUPER_ADMIN);

    // Act
    ResponseEntity<?> response = roleController.assignRole("user123", assignRoleDto, authHeader);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(roleService, never()).assignRole(anyString(), anyString());
  }

  @Test
  void testAssignRole_UserNotFound() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.SUPER_ADMIN);
    when(roleService.assignRole("invalid", "TEACHER"))
        .thenThrow(new UnknownUserException("User not found"));

    // Act
    ResponseEntity<?> response = roleController.assignRole("invalid", assignRoleDto, authHeader);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(response.getBody() instanceof Map);
  }

  @Test
  void testAssignRole_RoleNotFound() throws Exception {
    // Arrange
    AssignRoleDto invalidRole = new AssignRoleDto("INVALID");
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.SUPER_ADMIN);
    when(roleService.assignRole("user123", "INVALID"))
        .thenThrow(new InvalidRequestException("Role not found"));

    // Act
    ResponseEntity<?> response = roleController.assignRole("user123", invalidRole, authHeader);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testAssignRole_InternalServerError() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.SUPER_ADMIN);
    when(roleService.assignRole(anyString(), anyString()))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<?> response = roleController.assignRole("user123", assignRoleDto, authHeader);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testRemoveRole_Success() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.SUPER_ADMIN);
    when(roleService.removeRole("user123")).thenReturn(true);

    // Act
    ResponseEntity<?> response = roleController.removeRole("user123", authHeader);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody() instanceof Map);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertEquals("Ruolo rimosso con successo", body.get("message"));
  }

  @Test
  void testRemoveRole_NoRoleAssigned() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.SUPER_ADMIN);
    when(roleService.removeRole("user123")).thenReturn(false);

    // Act
    ResponseEntity<?> response = roleController.removeRole("user123", authHeader);

    // Assert
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void testRemoveRole_Forbidden() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doThrow(new SecurityException("Insufficient permissions"))
        .when(roleService).checkRole(token, RoleType.SUPER_ADMIN);

    // Act
    ResponseEntity<?> response = roleController.removeRole("user123", authHeader);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(roleService, never()).removeRole(anyString());
  }

  @Test
  void testRemoveRole_UserNotFound() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.SUPER_ADMIN);
    when(roleService.removeRole("invalid"))
        .thenThrow(new UnknownUserException("User not found"));

    // Act
    ResponseEntity<?> response = roleController.removeRole("invalid", authHeader);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testRemoveRole_InternalServerError() throws Exception {
    // Arrange
    when(tokenService.extractTokenFromHeader(authHeader)).thenReturn(token);
    doNothing().when(roleService).checkRole(token, RoleType.SUPER_ADMIN);
    when(roleService.removeRole(anyString()))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    ResponseEntity<?> response = roleController.removeRole("user123", authHeader);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}