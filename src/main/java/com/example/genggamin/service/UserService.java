package com.example.genggamin.service;

import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.dto.LoginRequest;
import com.example.genggamin.dto.LoginResponse;
import com.example.genggamin.entity.User;
import com.example.genggamin.entity.Role;
import com.example.genggamin.repository.UserRepository;
import com.example.genggamin.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public User createUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.saveAndFlush(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public LoginResponse Login(LoginRequest req){
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> new RuntimeException("User tidak di temukan"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah");
        }

        LoginResponse res = new LoginResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setIsActive(user.getIsActive());
        return res;
    }

    public User authenticate(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> new RuntimeException("User tidak di temukan"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah");
        }
        return user;
    }

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

        Role defaultRole = roleRepository.findByName("CUSTOMER").orElseGet(() -> {
            Role r = Role.builder().name("CUSTOMER").description("Default role").build();
            return roleRepository.save(r);
        });
        user.getRoles().add(defaultRole);

        return userRepository.saveAndFlush(user);
    }
}
