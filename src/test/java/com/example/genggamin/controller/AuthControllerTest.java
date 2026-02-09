package com.example.genggamin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.genggamin.dto.GoogleLoginRequest;
import com.example.genggamin.dto.LoginRequest;
import com.example.genggamin.dto.LoginResponse;
import com.example.genggamin.entity.Role;
import com.example.genggamin.entity.User;
import com.example.genggamin.security.JwtUtil;
import com.example.genggamin.service.GoogleAuthService;
import com.example.genggamin.service.PasswordResetService;
import com.example.genggamin.service.TokenBlacklistService;
import com.example.genggamin.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
      org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    })
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;

  @MockBean private JwtUtil jwtUtil;

  @MockBean private TokenBlacklistService tokenBlacklistService;

  @MockBean private PasswordResetService passwordResetService;

  @MockBean private GoogleAuthService googleAuthService;

  private User testUser;
  private LoginRequest loginRequest;
  private LoginResponse loginResponse;
  private String testToken;

  @BeforeEach
  void setUp() {
    // Setup test data
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword("encodedPassword");
    testUser.setIsActive(true);

    // Setup roles
    Role userRole = new Role();
    userRole.setId(1L);
    userRole.setName("ROLE_USER");

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);
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

    // Setup test token
    testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
  }

  @Test
  void login_WithValidCredentials_ShouldReturnTokenAndUserInfo() throws Exception {
    // Arrange
    when(userService.authenticate(any(LoginRequest.class))).thenReturn(testUser);
    when(userService.Login(any(LoginRequest.class))).thenReturn(loginResponse);
    when(jwtUtil.generateToken(anyString(), anySet())).thenReturn(testToken);

    // Act & Assert
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
        .andExpect(jsonPath("$.token").value(testToken));
  }

  @Test
  void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
    // Arrange
    when(userService.authenticate(any(LoginRequest.class)))
        .thenThrow(new RuntimeException("Invalid credentials"));

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid credentials"));
  }

  @Test
  void login_WithNonExistentUser_ShouldReturnUnauthorized() throws Exception {
    // Arrange
    when(userService.authenticate(any(LoginRequest.class)))
        .thenThrow(new RuntimeException("User not found"));

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("User not found"));
  }

  @Test
  void login_WithInactiveUser_ShouldReturnUnauthorized() throws Exception {
    // Arrange
    when(userService.authenticate(any(LoginRequest.class)))
        .thenThrow(new RuntimeException("Account is inactive"));

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Account is inactive"));
  }

  @Test
  void login_WithEmptyUsername_ShouldReturnBadRequest() throws Exception {
    // Arrange
    loginRequest.setUsername("");

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_WithNullPassword_ShouldReturnBadRequest() throws Exception {
    // Arrange
    loginRequest.setPassword(null);

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_WithValidCredentialsAndMultipleRoles_ShouldReturnToken() throws Exception {
    // Arrange
    Role adminRole = new Role();
    adminRole.setId(2L);
    adminRole.setName("ROLE_ADMIN");

    testUser.getRoles().add(adminRole);

    when(userService.authenticate(any(LoginRequest.class))).thenReturn(testUser);
    when(userService.Login(any(LoginRequest.class))).thenReturn(loginResponse);
    when(jwtUtil.generateToken(anyString(), anySet())).thenReturn(testToken);

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(testToken))
        .andExpect(jsonPath("$.username").value("testuser"));
  }

  @Test
  void forgotPassword_ShouldReturnSuccessAndToken() throws Exception {
    // Arrange
    String email = "test@example.com";
    String token = "reset-token-123";
    com.example.genggamin.dto.ForgotPasswordRequest request =
        new com.example.genggamin.dto.ForgotPasswordRequest();
    request.setEmail(email);

    when(passwordResetService.processForgotPassword(email)).thenReturn(token);

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.token").value(token));
  }

  @Test
  void googleLogin_WithValidToken_ShouldReturnSuccess() throws Exception {
    // Arrange
    String idToken = "valid-google-id-token";
    GoogleLoginRequest googleRequest = new GoogleLoginRequest();
    googleRequest.setIdToken(idToken);

    when(googleAuthService.loginWithGoogle(idToken, null)).thenReturn(loginResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(googleRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("testuser"))
        .andExpect(jsonPath("$.data.token").exists());
  }

  @Test
  void googleLogin_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
    // Arrange
    String idToken = "invalid-token";
    GoogleLoginRequest googleRequest = new GoogleLoginRequest();
    googleRequest.setIdToken(idToken);

    when(googleAuthService.loginWithGoogle(idToken, null))
        .thenThrow(new RuntimeException("Invalid ID token."));

    // Act & Assert
    mockMvc
        .perform(
            post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(googleRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Google login failed: Invalid ID token."));
  }
}
