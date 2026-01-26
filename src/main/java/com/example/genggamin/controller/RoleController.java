package com.example.genggamin.controller;

import com.example.genggamin.dto.ApiResponse;

import com.example.genggamin.entity.Role;
import com.example.genggamin.service.RoleService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
public class RoleController {

  private final RoleService roleService;

  public RoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
    List<Role> roles = roleService.getAllRoles();
    return ResponseEntity.ok(
        new ApiResponse<>(true, "Roles retrieved successfully", roles));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Role>> createRole(@RequestBody Role role) {
    Role saved = roleService.createRole(role);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse<>(true, "Role created successfully", saved));
  }
}
