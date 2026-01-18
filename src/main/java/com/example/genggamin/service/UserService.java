package com.example.genggamin.service;

import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.dto.UpdateUserRequest;
import com.example.genggamin.dto.LoginRequest;
import com.example.genggamin.dto.LoginResponse;
import com.example.genggamin.dto.UserResponse;
import com.example.genggamin.entity.Role;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.RoleRepository;
import com.example.genggamin.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  public UserService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      EmailService emailService) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
  }

  /**
   * Create user dengan cache eviction untuk refresh cache Menghapus cache users dan userByUsername
   * karena ada user baru
   */
  @Caching(
      evict = {
        @CacheEvict(value = "users", allEntries = true),
        @CacheEvict(value = "userByUsername", key = "#user.username")
      })
  public User createUser(User user) {
    if (user.getPassword() != null) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
    }
    return userRepository.saveAndFlush(user);
  }

  /**
   * Get all users dengan caching DTO (BUKAN Entity) Cache dengan key "allUsers" CACHE DTO untuk
   * menghindari Hibernate PersistentSet serialization issue
   */
  @Cacheable(value = "users", key = "'allUsers'")
  public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream()
        .map(this::mapToUserResponse)
        .collect(Collectors.toList());
  }

  /** Helper method untuk mapping Entity ke DTO */
  private UserResponse mapToUserResponse(User user) {
    UserResponse dto = new UserResponse();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setFullName(user.getFullName());
    dto.setIsActive(user.getIsActive());
    // Convert roles Set<Role> to List<String> role names
    dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
    return dto;
  }

  /**
   * Login dengan cache lookup untuk user DTO Menggunakan username sebagai cache key CACHE DTO bukan
   * Entity
   */
  public LoginResponse Login(LoginRequest req) {
    // Check cache first (will populate cache if not exists)
    UserResponse cachedUser = findByUsername(req.getUsername());

    // Perlu load full entity untuk verify password
    User user =
        userRepository
            .findByUsername(req.getUsername())
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

    if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
      throw new RuntimeException("Password salah");
    }

    LoginResponse res = new LoginResponse();
    res.setId(cachedUser.getId());
    res.setUsername(cachedUser.getUsername());
    res.setEmail(cachedUser.getEmail());
    res.setIsActive(cachedUser.getIsActive());
    return res;
  }

  /**
   * Authenticate dengan cache lookup untuk user DTO Returns full Entity karena diperlukan untuk JWT
   * token generation (needs roles)
   */
  public User authenticate(LoginRequest req) {
    UserResponse cachedUser = findByUsername(req.getUsername());

    // Load full entity for authentication
    User user =
        userRepository
            .findByUsername(req.getUsername())
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

    if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
      throw new RuntimeException("Password salah");
    }
    return user;
  }

  /**
   * Find user by username dengan caching DTO (BUKAN Entity) Method internal untuk reuse cache logic
   * CACHE DTO untuk menghindari Hibernate PersistentSet serialization issue
   */
  @Cacheable(value = "userByUsername", key = "#username")
  public UserResponse findByUsername(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    return mapToUserResponse(user);
  }

  /** Register user baru dengan cache eviction Menghapus cache users karena ada user baru */
  @Caching(
      evict = {
        @CacheEvict(value = "users", allEntries = true),
        @CacheEvict(value = "userByUsername", key = "#req.username")
      })
  public User register(CreateUserRequest req) {
    if (req.getUsername() == null || req.getEmail() == null || req.getPassword() == null) {
      throw new RuntimeException("Username, email, dan password diperlukan");
    }
    if (userRepository.existsByUsername(req.getUsername())) {
      throw new RuntimeException("Username sudah ada");
    }
    if (userRepository.existsByEmail(req.getEmail())) {
      throw new RuntimeException("Email sudah ada");
    }

    User user = new User();
    user.setUsername(req.getUsername());
    user.setEmail(req.getEmail());
    user.setFullName(req.getFullName());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setIsActive(true);

    // Forces default CUSTOMER role for public registration
    Role defaultRole =
        roleRepository
            .findByName("CUSTOMER")
            .orElseGet(
                () -> {
                  Role r =
                      Role.builder()
                          .name("CUSTOMER")
                          .description("Default customer role")
                          .build();
                  return roleRepository.save(r);
                });
    user.getRoles().add(defaultRole);

    User savedUser = userRepository.saveAndFlush(user);

    emailService.sendRegistrationConfirmationEmail(savedUser.getEmail(), savedUser.getUsername());

    return savedUser;
  }

  /**
   * Create staff user (Marketing, Branch Manager, Backoffice) - Admin only
   */
  @Caching(
      evict = {
        @CacheEvict(value = "users", allEntries = true),
        @CacheEvict(value = "userByUsername", key = "#req.username")
      })
  public User createStaffUser(CreateUserRequest req) {
    // Validate required roles
    if (req.getRoles() == null || req.getRoles().isEmpty()) {
      throw new RuntimeException("Role wajib dipilih untuk staff");
    }

    // Allowed roles for staff
    java.util.Set<String> allowedRoles = java.util.Set.of("MARKETING", "BRANCH_MANAGER", "BACK_OFFICE");
    
    // Validate that all requested roles are valid staff roles
    for (String roleName : req.getRoles()) {
        if (!allowedRoles.contains(roleName)) {
            throw new RuntimeException("Role tidak valid untuk staff: " + roleName + ". Valid roles: " + allowedRoles);
        }
    }

    return createUserFromRequest(req);
  }

  /**
   * Create user dari CreateUserRequest dengan role handling Method ini menangani role assignment
   * dari request
   */
  @Caching(
      evict = {
        @CacheEvict(value = "users", allEntries = true),
        @CacheEvict(value = "userByUsername", key = "#req.username")
      })
  public User createUserFromRequest(CreateUserRequest req) {
    if (req.getUsername() == null || req.getEmail() == null || req.getPassword() == null) {
      throw new RuntimeException("Username, email, dan password diperlukan");
    }
    if (userRepository.existsByUsername(req.getUsername())) {
      throw new RuntimeException("Username sudah ada");
    }
    if (userRepository.existsByEmail(req.getEmail())) {
      throw new RuntimeException("Email sudah ada");
    }

    User user = new User();
    user.setUsername(req.getUsername());
    user.setEmail(req.getEmail());
    user.setFullName(req.getFullName());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);

    // Handle roles assignment
    if (req.getRoles() != null && !req.getRoles().isEmpty()) {
      for (String roleName : req.getRoles()) {
        Role role =
            roleRepository
                .findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role " + roleName + " tidak ditemukan"));
        user.getRoles().add(role);
      }
    } else {
      // If no roles provided, assign default CUSTOMER role
      Role defaultRole =
          roleRepository
              .findByName("CUSTOMER")
              .orElseGet(
                  () -> {
                    Role r =
                        Role.builder()
                            .name("CUSTOMER")
                            .description("Default customer role")
                            .build();
                    return roleRepository.save(r);
                  });
      user.getRoles().add(defaultRole);
    }

    return userRepository.saveAndFlush(user);
  }

  /** Get User ID by username Used by CustomerController to get userId from JWT token */
  public Long getUserIdByUsername(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    return user.getId();
  }

  /**
   * Get user by ID dengan authorization check Hanya user yang sedang login (dengan ID yang sama)
   * yang bisa mengakses
   *
   * @param userId ID user yang akan diambil
   * @param currentUsername Username dari user yang sedang login (dari JWT token)
   * @return UserResponse DTO
   * @throws RuntimeException jika user tidak ditemukan atau tidak memiliki akses
   */
  @Cacheable(value = "userById", key = "#userId")
  public UserResponse getUserById(Long userId, String currentUsername) {
    // Get current logged in user
    User currentUser =
        userRepository
            .findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("User yang sedang login tidak ditemukan"));

    // Check if current user trying to access their own data
    if (!currentUser.getId().equals(userId)) {
      throw new RuntimeException("Anda tidak memiliki akses untuk melihat data user lain");
    }

    // Get the requested user
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

    return mapToUserResponse(user);
  }

  /**
   * Update staff user - Admin only
   */
  @Caching(
      evict = {
        @CacheEvict(value = "users", allEntries = true),
        @CacheEvict(value = "userByUsername", allEntries = true),
        @CacheEvict(value = "userById", key = "#id")
      })
  public User updateStaffUser(Long id, UpdateUserRequest req) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

    // Check username uniqueness if changed
    if (req.getUsername() != null && !req.getUsername().equals(user.getUsername())) {
      if (userRepository.existsByUsername(req.getUsername())) {
        throw new RuntimeException("Username sudah digunakan");
      }
      user.setUsername(req.getUsername());
    }

    // Check email uniqueness if changed
    if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
      if (userRepository.existsByEmail(req.getEmail())) {
        throw new RuntimeException("Email sudah digunakan");
      }
      user.setEmail(req.getEmail());
    }

    if (req.getFullName() != null) {
      user.setFullName(req.getFullName());
    }

    if (req.getPassword() != null && !req.getPassword().isEmpty()) {
      user.setPassword(passwordEncoder.encode(req.getPassword()));
    }

    if (req.getIsActive() != null) {
      user.setIsActive(req.getIsActive());
    }

    // Handle roles update if provided
    if (req.getRoles() != null && !req.getRoles().isEmpty()) {
      // Validate roles similar to register/staff
      java.util.Set<String> allowedRoles = java.util.Set.of("MARKETING", "BRANCH_MANAGER", "BACK_OFFICE");
      for (String roleName : req.getRoles()) {
        if (!allowedRoles.contains(roleName)) {
           throw new RuntimeException("Role tidak valid untuk staff: " + roleName + ". Valid roles: " + allowedRoles);
        }
      }
      
      user.getRoles().clear();
      for (String roleName : req.getRoles()) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role " + roleName + " tidak ditemukan"));
        user.getRoles().add(role);
      }
    }

    return userRepository.saveAndFlush(user);
  }
}
