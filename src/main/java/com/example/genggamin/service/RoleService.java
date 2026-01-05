package com.example.genggamin.service;

import com.example.genggamin.entity.Role;
import com.example.genggamin.repository.RoleRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

  private final RoleRepository roleRepository;

  public RoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  /** Create role dengan cache eviction Menghapus cache roles karena ada role baru */
  @CacheEvict(value = "roles", allEntries = true)
  public Role createRole(Role role) {
    if (role == null) {
      throw new IllegalArgumentException("Role cannot be null");
    }
    return roleRepository.save(role);
  }

  /** Get all roles dengan caching Cache dengan key "allRoles" TTL 30 menit (role jarang berubah) */
  @Cacheable(value = "roles", key = "'allRoles'")
  public List<Role> getAllRoles() {
    return roleRepository.findAll();
  }
}
