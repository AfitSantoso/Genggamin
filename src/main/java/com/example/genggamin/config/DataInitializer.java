package com.example.genggamin.config;

import com.example.genggamin.entity.Role;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.RoleRepository;
import com.example.genggamin.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Role admin = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));

        if (userRepository.findByUsername("admin").isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(admin);
            roles.add(userRole);

            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password("password")
                    .isActive(true)
                    .roles(roles)
                    .build();

            userRepository.save(adminUser);
        }
    }
}
