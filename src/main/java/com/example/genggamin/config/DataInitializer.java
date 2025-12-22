package com.example.genggamin.config;

import com.example.genggamin.entity.Role;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.RoleRepository;
import com.example.genggamin.repository.UserRepository;
import com.example.genggamin.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize all required roles
        Role admin = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ADMIN")
                        .description("Administrator with full access")
                        .build()));
        
        Role customer = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("CUSTOMER")
                        .description("Customer who can submit loan applications")
                        .build()));
        
        Role marketing = roleRepository.findByName("MARKETING")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("MARKETING")
                        .description("Marketing staff who can review loan applications")
                        .build()));
        
        Role branchManager = roleRepository.findByName("BRANCH_MANAGER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("BRANCH_MANAGER")
                        .description("Branch manager who can approve/reject loans")
                        .build()));
        
        Role backOffice = roleRepository.findByName("BACK_OFFICE")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("BACK_OFFICE")
                        .description("Back office staff who can disburse loans")
                        .build()));

        // Create default admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(admin);

            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password("password")
                    .isActive(true)
                    .roles(roles)
                    .build();

            userService.createUser(adminUser);
        }

        // Create sample users for each role
        if (userRepository.findByUsername("customer1").isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(customer);

            User customerUser = User.builder()
                    .username("customer1")
                    .email("customer1@example.com")
                    .password("password")
                    .isActive(true)
                    .roles(roles)
                    .build();

            userService.createUser(customerUser);
        }

        if (userRepository.findByUsername("marketing1").isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(marketing);

            User marketingUser = User.builder()
                    .username("marketing1")
                    .email("marketing1@example.com")
                    .password("password")
                    .isActive(true)
                    .roles(roles)
                    .build();

            userService.createUser(marketingUser);
        }

        if (userRepository.findByUsername("manager1").isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(branchManager);

            User managerUser = User.builder()
                    .username("manager1")
                    .email("manager1@example.com")
                    .password("password")
                    .isActive(true)
                    .roles(roles)
                    .build();

            userService.createUser(managerUser);
        }

        if (userRepository.findByUsername("backoffice1").isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(backOffice);

            User backOfficeUser = User.builder()
                    .username("backoffice1")
                    .email("backoffice1@example.com")
                    .password("password")
                    .isActive(true)
                    .roles(roles)
                    .build();

            userService.createUser(backOfficeUser);
        }
    }
}
