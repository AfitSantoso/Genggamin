package com.example.genggamin.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.dto.ForgotPasswordRequest;
import com.example.genggamin.dto.LoginRequest;
import com.example.genggamin.dto.LoginResponse;
import com.example.genggamin.dto.ResetPasswordRequest;
import com.example.genggamin.entity.Role;
import com.example.genggamin.entity.User;
import com.example.genggamin.security.JwtUtil;
import com.example.genggamin.service.PasswordResetService;
import com.example.genggamin.service.TokenBlacklistService;
import com.example.genggamin.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
      org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    })
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;

  @MockBean private JwtUtil jwtUtil;

  @MockBean private TokenBlacklistService tokenBlacklistService;

  @MockBean private PasswordResetService passwordResetService;

  // Mock security components to prevent SecurityConfig from interfering
  @MockBean(name = "jwtAuthenticationFilter")
  private com.example.genggamin.security.JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean
  private com.example.genggamin.security.RestAuthenticationEntryPoint restAuthenticationEntryPoint;

  @MockBean private com.example.genggamin.security.RestAccessDeniedHandler restAccessDeniedHandler;

  private User testUser;
  private Role testRole;
  private LoginRequest loginRequest;
  private LoginResponse loginResponse;

  @BeforeEach
  void setUp() {
    // Setup test role
    testRole = new Role();
    testRole.setId(1L);
    testRole.setName("ROLE_CUSTOMER");

    Set<Role> roles = new HashSet<>();
    roles.add(testRole);

    // Setup test user
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword("hashedPassword");
    testUser.setRoles(roles);

    // Setup login request
    loginRequest = new LoginRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("password123");

    // Setup login response
    loginResponse = new LoginResponse();
    loginResponse.setId(1L);
    loginResponse.setUsername("testuser");
    loginResponse.setEmail("test@example.com");
    loginResponse.setIsActive(true);
  }

  @Test
  void testLogin_Success() throws Exception {
    // Given
    String token = "jwt.token.here";
    when(userService.authenticate(any(LoginRequest.class))).thenReturn(testUser);
    when(userService.Login(any(LoginRequest.class))).thenReturn(loginResponse);
    when(jwtUtil.generateToken(anyString(), anySet())).thenReturn(token);

    // When & Then
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.isActive").value(true))
        .andExpect(jsonPath("$.token").value(token));

    verify(userService, times(1)).authenticate(any(LoginRequest.class));
    verify(userService, times(1)).Login(any(LoginRequest.class));
    verify(jwtUtil, times(1)).generateToken(eq("testuser"), anySet());
  }

  @Test
  void testLogin_InvalidCredentials() throws Exception {
    // Given
    when(userService.authenticate(any(LoginRequest.class)))
        .thenThrow(new RuntimeException("Invalid username or password"));

    // When & Then
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid username or password"));

    verify(userService, times(1)).authenticate(any(LoginRequest.class));
    verify(jwtUtil, never()).generateToken(anyString(), anySet());
  }

  @Test
  void testRegister_Success() throws Exception {
    // Given
    CreateUserRequest registerRequest = new CreateUserRequest();
    registerRequest.setUsername("newuser");
    registerRequest.setPassword("password123");
    registerRequest.setEmail("newuser@example.com");
    registerRequest.setRoles(Set.of("ROLE_CUSTOMER"));

    when(userService.register(any(CreateUserRequest.class))).thenReturn(testUser);

    // When & Then
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("User created"))
        .andExpect(jsonPath("$.id").value(1));

    verify(userService, times(1)).register(any(CreateUserRequest.class));
  }

  @Test
  void testRegister_UsernameAlreadyExists() throws Exception {
    // Given
    CreateUserRequest registerRequest = new CreateUserRequest();
    registerRequest.setUsername("existinguser");
    registerRequest.setPassword("password123");
    registerRequest.setEmail("existing@example.com");
    registerRequest.setRoles(Set.of("ROLE_CUSTOMER"));

    when(userService.register(any(CreateUserRequest.class)))
        .thenThrow(new RuntimeException("Username already exists"));

    // When & Then
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Username already exists"));

    verify(userService, times(1)).register(any(CreateUserRequest.class));
  }

  @Test
  void testLogout_Success() throws Exception {
    // Given
    String token = "jwt.token.here";
    String authHeader = "Bearer " + token;
    long expirationTime = System.currentTimeMillis() + 3600000; // 1 hour

    when(jwtUtil.getExpirationTimeFromToken(token)).thenReturn(expirationTime);
    doNothing().when(tokenBlacklistService).blacklistToken(token, expirationTime);

    // When & Then
    mockMvc
        .perform(post("/auth/logout").header("Authorization", authHeader))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Logout successful. Token has been invalidated."));

    verify(jwtUtil, times(1)).getExpirationTimeFromToken(token);
    verify(tokenBlacklistService, times(1)).blacklistToken(token, expirationTime);
  }

  @Test
  void testLogout_MissingAuthorizationHeader() throws Exception {
    // When & Then
    mockMvc
        .perform(post("/auth/logout"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Authorization header is missing or invalid"));

    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }

  @Test
  void testLogout_InvalidAuthorizationHeader() throws Exception {
    // Given
    String authHeader = "InvalidHeader token";

    // When & Then
    mockMvc
        .perform(post("/auth/logout").header("Authorization", authHeader))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Authorization header is missing or invalid"));

    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }

  @Test
  void testForgotPassword_Success() throws Exception {
    // Given
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail("test@example.com");

    doNothing().when(passwordResetService).processForgotPassword(anyString());

    // When & Then
    mockMvc
        .perform(
            post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "Link reset password telah dikirim ke email Anda. Silakan cek inbox atau spam"
                        + " folder."));

    verify(passwordResetService, times(1)).processForgotPassword("test@example.com");
  }

  @Test
  void testForgotPassword_EmailNotFound() throws Exception {
    // Given
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail("notfound@example.com");

    doThrow(new RuntimeException("Email tidak terdaftar"))
        .when(passwordResetService)
        .processForgotPassword(anyString());

    // When & Then
    mockMvc
        .perform(
            post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Email tidak terdaftar"));

    verify(passwordResetService, times(1)).processForgotPassword("notfound@example.com");
  }

  @Test
  void testResetPassword_Success() throws Exception {
    // Given
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken("valid-reset-token");
    request.setNewPassword("newPassword123");

    doNothing().when(passwordResetService).resetPassword(anyString(), anyString());

    // When & Then
    mockMvc
        .perform(
            post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(
            jsonPath("$.message")
                .value("Password berhasil direset. Silakan login dengan password baru Anda."));

    verify(passwordResetService, times(1)).resetPassword("valid-reset-token", "newPassword123");
  }

  @Test
  void testResetPassword_InvalidToken() throws Exception {
    // Given
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken("invalid-token");
    request.setNewPassword("newPassword123");

    doThrow(new RuntimeException("Token tidak valid atau sudah expired"))
        .when(passwordResetService)
        .resetPassword(anyString(), anyString());

    // When & Then
    mockMvc
        .perform(
            post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Token tidak valid atau sudah expired"));

    verify(passwordResetService, times(1)).resetPassword("invalid-token", "newPassword123");
  }

  @Test
  void testResetPassword_ExpiredToken() throws Exception {
    // Given
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken("expired-token");
    request.setNewPassword("newPassword123");

    doThrow(new RuntimeException("Token sudah expired"))
        .when(passwordResetService)
        .resetPassword(anyString(), anyString());

    // When & Then
    mockMvc
        .perform(
            post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Token sudah expired"));

    verify(passwordResetService, times(1)).resetPassword("expired-token", "newPassword123");
  }
}
