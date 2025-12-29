package com.example.genggamin.service;

import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.dto.LoginRequest;
import com.example.genggamin.dto.LoginResponse;
import com.example.genggamin.dto.UserResponse;
import com.example.genggamin.entity.User;
import com.example.genggamin.entity.Role;
import com.example.genggamin.repository.UserRepository;
import com.example.genggamin.repository.RoleRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create user dengan cache eviction untuk refresh cache
     * Menghapus cache users dan userByUsername karena ada user baru
     */
    @Caching(evict = {
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
     * Get all users dengan caching DTO (BUKAN Entity)
     * Cache dengan key "allUsers"
     * CACHE DTO untuk menghindari Hibernate PersistentSet serialization issue
     */
    @Cacheable(value = "users", key = "'allUsers'")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper method untuk mapping Entity ke DTO
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setIsActive(user.getIsActive());
        // Convert roles Set<Role> to List<String> role names
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Login dengan cache lookup untuk user DTO
     * Menggunakan username sebagai cache key
     * CACHE DTO bukan Entity
     */
    public LoginResponse Login(LoginRequest req){
        // Check cache first (will populate cache if not exists)
        UserResponse cachedUser = findByUsername(req.getUsername());
        
        // Perlu load full entity untuk verify password
        User user = userRepository.findByUsername(req.getUsername())
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
     * Authenticate dengan cache lookup untuk user DTO
     * Returns full Entity karena diperlukan untuk JWT token generation (needs roles)
     */
    public User authenticate(LoginRequest req) {
        UserResponse cachedUser = findByUsername(req.getUsername());
        
        // Load full entity for authentication
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
                
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah");
        }
        return user;
    }

    /**
     * Find user by username dengan caching DTO (BUKAN Entity)
     * Method internal untuk reuse cache logic
     * CACHE DTO untuk menghindari Hibernate PersistentSet serialization issue
     */
    @Cacheable(value = "userByUsername", key = "#username")
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return mapToUserResponse(user);
    }

    /**
     * Register user baru dengan cache eviction
     * Menghapus cache users karena ada user baru
     */
    @Caching(evict = {
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
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setIsActive(true);
        user.setPhone(req.getPhone());

        // Handle roles assignment
        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            // If roles are provided, assign them
            for (String roleName : req.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role " + roleName + " tidak ditemukan"));
                user.getRoles().add(role);
            }
        } else {
            // If no roles provided, assign default CUSTOMER role
            Role defaultRole = roleRepository.findByName("CUSTOMER").orElseGet(() -> {
                Role r = Role.builder().name("CUSTOMER").description("Default customer role").build();
                return roleRepository.save(r);
            });
            user.getRoles().add(defaultRole);
        }

        return userRepository.saveAndFlush(user);
    }

    /**
     * Create user dari CreateUserRequest dengan role handling
     * Method ini menangani role assignment dari request
     */
    @Caching(evict = {
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
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        user.setPhone(req.getPhone());

        // Handle roles assignment
        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            for (String roleName : req.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role " + roleName + " tidak ditemukan"));
                user.getRoles().add(role);
            }
        } else {
            // If no roles provided, assign default CUSTOMER role
            Role defaultRole = roleRepository.findByName("CUSTOMER").orElseGet(() -> {
                Role r = Role.builder().name("CUSTOMER").description("Default customer role").build();
                return roleRepository.save(r);
            });
            user.getRoles().add(defaultRole);
        }

        return userRepository.saveAndFlush(user);
    }

    /**
     * Get User ID by username
     * Used by CustomerController to get userId from JWT token
     */
    public Long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return user.getId();
    }

    /**
     * Get user by ID dengan authorization check
     * Hanya user yang sedang login (dengan ID yang sama) yang bisa mengakses
     * @param userId ID user yang akan diambil
     * @param currentUsername Username dari user yang sedang login (dari JWT token)
     * @return UserResponse DTO
     * @throws RuntimeException jika user tidak ditemukan atau tidak memiliki akses
     */
    @Cacheable(value = "userById", key = "#userId")
    public UserResponse getUserById(Long userId, String currentUsername) {
        // Get current logged in user
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User yang sedang login tidak ditemukan"));
        
        // Check if current user trying to access their own data
        if (!currentUser.getId().equals(userId)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk melihat data user lain");
        }
        
        // Get the requested user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        
        return mapToUserResponse(user);
    }
}
