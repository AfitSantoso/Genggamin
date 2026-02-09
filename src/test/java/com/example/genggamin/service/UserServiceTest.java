package com.example.genggamin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.dto.LoginRequest;
import com.example.genggamin.dto.UserResponse;
import com.example.genggamin.entity.Role;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.RoleRepository;
import com.example.genggamin.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit Tests for UserService
 *
 * <p>Best Practices yang diikuti:
 *
 * <ul>
 *   <li>Menggunakan Mockito (bukan @SpringBootTest) untuk kecepatan eksekusi
 *   <li>AAA Pattern (Arrange, Act, Assert)
 *   <li>Satu skenario per metode @Test
 *   <li>Penamaan deskriptif: shouldXxx_whenYyy() atau givenXxx_whenYyy_thenZzz()
 *   <li>Nested class untuk mengelompokkan test berdasarkan method
 *   <li>Mock untuk Repository/External dependencies
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private RoleRepository roleRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private EmailService emailService;

  @Mock private NotificationService notificationService;

  @InjectMocks private UserService userService;

  private User testUser;
  private Role customerRole;
  private CreateUserRequest createUserRequest;

  @BeforeEach
  void setUp() {
    // Arrange: Setup common test data
    customerRole =
        Role.builder().id(1L).name("CUSTOMER").description("Default customer role").build();

    Set<Role> roles = new HashSet<>();
    roles.add(customerRole);

    testUser =
        User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .fullName("Test User")
            .password("encodedPassword123")
            .isActive(true)
            .roles(roles)
            .build();

    createUserRequest = new CreateUserRequest();
    createUserRequest.setUsername("newuser");
    createUserRequest.setEmail("new@example.com");
    createUserRequest.setFullName("New User");
    createUserRequest.setPassword("password123");
  }

  // =========================================================================
  // Tests for getAllUsers()
  // =========================================================================
  @Nested
  @DisplayName("getAllUsers()")
  class GetAllUsersTests {

    @Test
    @DisplayName("should return list of UserResponse when users exist")
    void shouldReturnListOfUserResponse_whenUsersExist() {
      // Arrange
      given(userRepository.findAll()).willReturn(List.of(testUser));

      // Act
      List<UserResponse> result = userService.getAllUsers();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getUsername()).isEqualTo("testuser");
      assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
      assertThat(result.get(0).getRoles()).contains("CUSTOMER");
      verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("should return empty list when no users exist")
    void shouldReturnEmptyList_whenNoUsersExist() {
      // Arrange
      given(userRepository.findAll()).willReturn(List.of());

      // Act
      List<UserResponse> result = userService.getAllUsers();

      // Assert
      assertThat(result).isEmpty();
      verify(userRepository, times(1)).findAll();
    }
  }

  // =========================================================================
  // Tests for register()
  // =========================================================================
  @Nested
  @DisplayName("register()")
  class RegisterTests {

    @Test
    @DisplayName("should create user successfully when valid input provided")
    void shouldCreateUser_whenValidInputProvided() {
      // Arrange
      given(userRepository.existsByUsername(anyString())).willReturn(false);
      given(userRepository.existsByEmail(anyString())).willReturn(false);
      given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
      given(roleRepository.findByName("CUSTOMER")).willReturn(Optional.of(customerRole));
      given(userRepository.saveAndFlush(any(User.class))).willReturn(testUser);

      // Act
      User result = userService.register(createUserRequest);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("testuser");
      verify(userRepository).saveAndFlush(any(User.class));
      verify(emailService).sendRegistrationConfirmationEmail(anyString(), anyString());
      verify(notificationService).sendNotification(any(User.class), any(), any());
    }

    @Test
    @DisplayName("should throw exception when username already exists")
    void shouldThrowException_whenUsernameAlreadyExists() {
      // Arrange
      given(userRepository.existsByUsername(anyString())).willReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> userService.register(createUserRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Username sudah ada");

      verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("should throw exception when email already exists")
    void shouldThrowException_whenEmailAlreadyExists() {
      // Arrange
      given(userRepository.existsByUsername(anyString())).willReturn(false);
      given(userRepository.existsByEmail(anyString())).willReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> userService.register(createUserRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Email sudah ada");

      verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("should throw exception when required fields are null")
    void shouldThrowException_whenRequiredFieldsAreNull() {
      // Arrange
      CreateUserRequest invalidRequest = new CreateUserRequest();
      invalidRequest.setUsername(null);
      invalidRequest.setEmail(null);
      invalidRequest.setPassword(null);

      // Act & Assert
      assertThatThrownBy(() -> userService.register(invalidRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Username, email, dan password diperlukan");

      verify(userRepository, never()).saveAndFlush(any(User.class));
    }
  }

  // =========================================================================
  // Tests for authenticate()
  // =========================================================================
  @Nested
  @DisplayName("authenticate()")
  class AuthenticateTests {

    @Test
    @DisplayName("should return user when credentials are valid")
    void shouldReturnUser_whenCredentialsAreValid() {
      // Arrange
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setUsername("testuser");
      loginRequest.setPassword("password123");

      given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));
      given(passwordEncoder.matches("password123", testUser.getPassword())).willReturn(true);

      // Act
      User result = userService.authenticate(loginRequest);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("testuser");
      assertThat(result.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void shouldThrowException_whenUserNotFound() {
      // Arrange
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setUsername("nonexistent");
      loginRequest.setPassword("password123");

      given(userRepository.findByUsername("nonexistent")).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.authenticate(loginRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("User tidak ditemukan");
    }

    @Test
    @DisplayName("should throw exception when password is incorrect")
    void shouldThrowException_whenPasswordIncorrect() {
      // Arrange
      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setUsername("testuser");
      loginRequest.setPassword("wrongpassword");

      given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));
      given(passwordEncoder.matches("wrongpassword", testUser.getPassword())).willReturn(false);

      // Act & Assert
      assertThatThrownBy(() -> userService.authenticate(loginRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Password salah");
    }

    @Test
    @DisplayName("should throw exception when user is inactive")
    void shouldThrowException_whenUserIsInactive() {
      // Arrange
      testUser.setIsActive(false);

      LoginRequest loginRequest = new LoginRequest();
      loginRequest.setUsername("testuser");
      loginRequest.setPassword("password123");

      given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));
      given(passwordEncoder.matches("password123", testUser.getPassword())).willReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> userService.authenticate(loginRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("User sudah tidak aktif");
    }
  }

  // =========================================================================
  // Tests for findByUsername()
  // =========================================================================
  @Nested
  @DisplayName("findByUsername()")
  class FindByUsernameTests {

    @Test
    @DisplayName("should return UserResponse when user exists")
    void shouldReturnUserResponse_whenUserExists() {
      // Arrange
      given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));

      // Act
      UserResponse result = userService.findByUsername("testuser");

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("testuser");
      assertThat(result.getEmail()).isEqualTo("test@example.com");
      assertThat(result.getFullName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void shouldThrowException_whenUserNotFound() {
      // Arrange
      given(userRepository.findByUsername("nonexistent")).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.findByUsername("nonexistent"))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("User tidak ditemukan");
    }
  }

  // =========================================================================
  // Tests for createStaffUser()
  // =========================================================================
  @Nested
  @DisplayName("createStaffUser()")
  class CreateStaffUserTests {

    @Test
    @DisplayName("should create staff user when valid role provided")
    void shouldCreateStaffUser_whenValidRoleProvided() {
      // Arrange
      Role marketingRole =
          Role.builder().id(2L).name("MARKETING").description("Marketing role").build();

      createUserRequest.setRoles(Set.of("MARKETING"));
      given(userRepository.existsByUsername(anyString())).willReturn(false);
      given(userRepository.existsByEmail(anyString())).willReturn(false);
      given(roleRepository.findByName("MARKETING")).willReturn(Optional.of(marketingRole));
      given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
      given(userRepository.saveAndFlush(any(User.class))).willReturn(testUser);

      // Act
      User result = userService.createStaffUser(createUserRequest);

      // Assert
      assertThat(result).isNotNull();
      verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("should throw exception when role is missing")
    void shouldThrowException_whenRoleIsMissing() {
      // Arrange
      createUserRequest.setRoles(null);

      // Act & Assert
      assertThatThrownBy(() -> userService.createStaffUser(createUserRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Role wajib dipilih untuk staff");
    }

    @Test
    @DisplayName("should throw exception when invalid staff role provided")
    void shouldThrowException_whenInvalidStaffRoleProvided() {
      // Arrange
      createUserRequest.setRoles(Set.of("CUSTOMER"));

      // Act & Assert
      assertThatThrownBy(() -> userService.createStaffUser(createUserRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Role tidak valid untuk staff");
    }
  }

  // =========================================================================
  // Tests for getUserIdByUsername()
  // =========================================================================
  @Nested
  @DisplayName("getUserIdByUsername()")
  class GetUserIdByUsernameTests {

    @Test
    @DisplayName("should return user id when user exists")
    void shouldReturnUserId_whenUserExists() {
      // Arrange
      given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));

      // Act
      Long result = userService.getUserIdByUsername("testuser");

      // Assert
      assertThat(result).isEqualTo(1L);
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void shouldThrowException_whenUserNotFound() {
      // Arrange
      given(userRepository.findByUsername("nonexistent")).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.getUserIdByUsername("nonexistent"))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("User tidak ditemukan");
    }
  }
}
