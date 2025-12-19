package com.example.genggamin.controller;

import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.entity.Role;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.RoleRepository;
import com.example.genggamin.service.UserService;
// import com.example.genggamin.dto.LoginRequest;
// import com.example.genggamin.dto.LoginResponse;

//import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest req) {
        Set<Role> roles = new HashSet<>();
        if (req.getRoles() != null) {
            for (String roleName : req.getRoles()) {
                Role r = roleRepository.findByName(roleName).orElse(null);
                if (r != null) roles.add(r);
            }
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(req.getPassword())
                .isActive(req.getIsActive())
                .roles(roles)
                .build();

        User saved = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // @PostMapping("/login")
    // public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
    //     LoginResponse res = userService.Login(req);
    //     return ResponseEntity.ok(res);
    // }

}
