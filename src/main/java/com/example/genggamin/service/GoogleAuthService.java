package com.example.genggamin.service;

import com.example.genggamin.dto.LoginResponse;
import com.example.genggamin.entity.Role;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.RoleRepository;
import com.example.genggamin.repository.UserRepository;
import com.example.genggamin.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleAuthService {

  private final GoogleIdTokenVerifier verifier;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final JwtUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;

  public GoogleAuthService(
      GoogleIdTokenVerifier verifier,
      UserRepository userRepository,
      RoleRepository roleRepository,
      JwtUtil jwtUtil,
      PasswordEncoder passwordEncoder) {
    this.verifier = verifier;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.jwtUtil = jwtUtil;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public LoginResponse loginWithGoogle(String idTokenString, String fcmToken)
      throws GeneralSecurityException, IOException {
    GoogleIdToken idToken = verifier.verify(idTokenString);
    if (idToken != null) {
      GoogleIdToken.Payload payload = idToken.getPayload();
      String email = payload.getEmail();
      String name = (String) payload.get("name");

      User user = userRepository.findByEmail(email).orElse(null);
      if (user == null) {
        user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setFullName(name);
        user.setIsActive(true);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        Role customerRole =
            roleRepository
                .findByName("CUSTOMER")
                .orElseGet(
                    () -> {
                      Role r = new Role();
                      r.setName("CUSTOMER");
                      r.setDescription("Default customer role");
                      return roleRepository.save(r);
                    });
        user.getRoles().add(customerRole);
        user = userRepository.save(user);
      }

      // Update FCM Token if present
      if (fcmToken != null && !fcmToken.isEmpty()) {
        user.setFcmToken(fcmToken);
        user = userRepository.save(user);
      }

      java.util.Set<String> roles =
          user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
      String jwt = jwtUtil.generateToken(user.getUsername(), roles);

      LoginResponse response = new LoginResponse();
      response.setId(user.getId());
      response.setUsername(user.getUsername());
      response.setEmail(user.getEmail());
      response.setIsActive(user.getIsActive());
      response.setToken(jwt);
      response.setExpiresAt(jwtUtil.getExpirationTimeFromToken(jwt));
      return response;

    } else {
      throw new IllegalArgumentException("Invalid ID token.");
    }
  }
}
