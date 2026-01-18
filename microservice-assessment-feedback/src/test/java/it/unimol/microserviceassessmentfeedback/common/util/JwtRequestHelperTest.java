package it.unimol.microserviceassessmentfeedback.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unimol.microserviceassessmentfeedback.enums.RoleType;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtRequestHelperTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private JwtValidationService jwtValidationService;

  @InjectMocks
  private JwtRequestHelper jwtRequestHelper;

  @BeforeEach
  void setUp() {
    // Setup comune se necessario
  }

  @Test
  void testGetUsernameFromRequest_Success() {
    when(request.getAttribute("username")).thenReturn("testUser");

    String username = jwtRequestHelper.getUsernameFromRequest(request);

    assertEquals("testUser", username);
    verify(request).getAttribute("username");
  }

  @Test
  void testGetUsernameFromRequest_ThrowsException() {
    when(request.getAttribute("username")).thenReturn(null);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> jwtRequestHelper.getUsernameFromRequest(request)
    );

    assertTrue(exception.getMessage().contains("Username non trovato"));
  }

  @Test
  void testGetUserIdFromRequest_Success() {
    when(request.getAttribute("userId")).thenReturn("user123");

    String userId = jwtRequestHelper.getUserIdFromRequest(request);

    assertEquals("user123", userId);
    verify(request).getAttribute("userId");
  }

  @Test
  void testGetUserIdFromRequest_ThrowsException() {
    when(request.getAttribute("userId")).thenReturn(null);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> jwtRequestHelper.getUserIdFromRequest(request)
    );

    assertTrue(exception.getMessage().contains("UserId non trovato"));
  }

  @Test
  void testGetUserRoleFromRequest_Success() {
    when(request.getAttribute("userRole")).thenReturn("ROLE_STUDENT");

    String role = jwtRequestHelper.getUserRoleFromRequest(request);

    assertEquals("ROLE_STUDENT", role);
    verify(request).getAttribute("userRole");
  }

  @Test
  void testGetUserRoleFromRequest_ThrowsException() {
    when(request.getAttribute("userRole")).thenReturn(null);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> jwtRequestHelper.getUserRoleFromRequest(request)
    );

    assertTrue(exception.getMessage().contains("UserRole non trovato"));
  }

  @Test
  void testHasRole_True() {
    when(request.getAttribute("userRole")).thenReturn("ROLE_TEACHER");

    boolean result = jwtRequestHelper.hasRole(request, "ROLE_TEACHER");

    assertTrue(result);
  }

  @Test
  void testHasRole_False() {
    when(request.getAttribute("userRole")).thenReturn("ROLE_STUDENT");

    boolean result = jwtRequestHelper.hasRole(request, "ROLE_TEACHER");

    assertFalse(result);
  }

  @Test
  void testHasRole_NullRole() {
    when(request.getAttribute("userRole")).thenReturn("ROLE_STUDENT");

    boolean result = jwtRequestHelper.hasRole(request, null);

    assertFalse(result);
  }

  @Test
  void testHasRole_Exception() {
    when(request.getAttribute("userRole")).thenThrow(new RuntimeException("Error"));

    boolean result = jwtRequestHelper.hasRole(request, "ROLE_TEACHER");

    assertFalse(result);
  }

  @Test
  void testIsStudent_True() {
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_STUDENT);

    boolean result = jwtRequestHelper.isStudent(request);

    assertTrue(result);
  }

  @Test
  void testIsStudent_False() {
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_TEACHER);

    boolean result = jwtRequestHelper.isStudent(request);

    assertFalse(result);
  }

  @Test
  void testIsTeacher_True() {
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_TEACHER);

    boolean result = jwtRequestHelper.isTeacher(request);

    assertTrue(result);
  }

  @Test
  void testIsTeacher_False() {
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_STUDENT);

    boolean result = jwtRequestHelper.isTeacher(request);

    assertFalse(result);
  }

  @Test
  void testIsAdmin_True() {
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_ADMIN);

    boolean result = jwtRequestHelper.isAdmin(request);

    assertTrue(result);
  }

  @Test
  void testIsAdmin_False() {
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_STUDENT);

    boolean result = jwtRequestHelper.isAdmin(request);

    assertFalse(result);
  }

  @Test
  void testIsSuperAdmin_True() {
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_SUPER_ADMIN);

    boolean result = jwtRequestHelper.isSuperAdmin(request);

    assertTrue(result);
  }

  @Test
  void testIsSuperAdmin_False() {
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_ADMIN);

    boolean result = jwtRequestHelper.isSuperAdmin(request);

    assertFalse(result);
  }

  @Test
  void testExtractStudentIdFromRequest_FromAttribute() {
    when(request.getAttribute("studentId")).thenReturn("student123");

    String studentId = jwtRequestHelper.extractStudentIdFromRequest(request);

    assertEquals("student123", studentId);
    verify(request).getAttribute("studentId");
  }

  @Test
  void testExtractStudentIdFromRequest_FromUserId() {
    when(request.getAttribute("studentId")).thenReturn(null);
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_STUDENT);
    when(request.getAttribute("userId")).thenReturn("student456");

    String studentId = jwtRequestHelper.extractStudentIdFromRequest(request);

    assertEquals("student456", studentId);
  }

  @Test
  void testExtractStudentIdFromRequest_FromToken() {
    when(request.getAttribute("studentId")).thenReturn(null);
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_TEACHER);
    when(request.getHeader("Authorization")).thenReturn("Bearer token123");
    when(jwtValidationService.extractTokenFromHeader("Bearer token123")).thenReturn("token123");
    when(jwtValidationService.extractStudentId("token123")).thenReturn("student789");

    String studentId = jwtRequestHelper.extractStudentIdFromRequest(request);

    assertEquals("student789", studentId);
  }

  @Test
  void testExtractStudentIdFromRequest_ThrowsException() {
    when(request.getAttribute("studentId")).thenReturn(null);
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_TEACHER);
    when(request.getHeader("Authorization")).thenReturn(null);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> jwtRequestHelper.extractStudentIdFromRequest(request)
    );

    assertTrue(exception.getMessage().contains("StudentId non trovato") ||
        exception.getMessage().contains("Errore nell'estrazione dello studentId"));
  }

  @Test
  void testExtractStudentIdFromRequest_ExceptionHandling() {
    when(request.getAttribute("studentId")).thenThrow(new RuntimeException("Test error"));

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> jwtRequestHelper.extractStudentIdFromRequest(request)
    );

    assertTrue(exception.getMessage().contains("Errore nell'estrazione dello studentId"));
  }

  @Test
  void testExtractTeacherIdFromRequest_FromAttribute() {
    when(request.getAttribute("teacherId")).thenReturn("teacher123");

    String teacherId = jwtRequestHelper.extractTeacherIdFromRequest(request);

    assertEquals("teacher123", teacherId);
    verify(request).getAttribute("teacherId");
  }

  @Test
  void testExtractTeacherIdFromRequest_FromUserId() {
    when(request.getAttribute("teacherId")).thenReturn(null);
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_TEACHER);
    when(request.getAttribute("userId")).thenReturn("teacher456");

    String teacherId = jwtRequestHelper.extractTeacherIdFromRequest(request);

    assertEquals("teacher456", teacherId);
  }

  @Test
  void testExtractTeacherIdFromRequest_FromToken() {
    when(request.getAttribute("teacherId")).thenReturn(null);
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_STUDENT);
    when(request.getHeader("Authorization")).thenReturn("Bearer token123");
    when(jwtValidationService.extractTokenFromHeader("Bearer token123")).thenReturn("token123");
    when(jwtValidationService.extractTeacherId("token123")).thenReturn("teacher789");

    String teacherId = jwtRequestHelper.extractTeacherIdFromRequest(request);

    assertEquals("teacher789", teacherId);
  }

  @Test
  void testExtractTeacherIdFromRequest_ThrowsException() {
    when(request.getAttribute("teacherId")).thenReturn(null);
    when(request.getAttribute("userRole")).thenReturn(RoleType.ROLE_STUDENT);
    when(request.getHeader("Authorization")).thenReturn(null);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> jwtRequestHelper.extractTeacherIdFromRequest(request)
    );

    assertTrue(exception.getMessage().contains("TeacherId non trovato") ||
        exception.getMessage().contains("Errore nell'estrazione del teacherId"));
  }

  @Test
  void testExtractTeacherIdFromRequest_ExceptionHandling() {
    when(request.getAttribute("teacherId")).thenThrow(new RuntimeException("Test error"));

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> jwtRequestHelper.extractTeacherIdFromRequest(request)
    );

    assertTrue(exception.getMessage().contains("Errore nell'estrazione del teacherId"));
  }
}