package com.example.genggamin.controller;

import com.example.genggamin.entity.Role;
import com.example.genggamin.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role saved = roleService.createRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
