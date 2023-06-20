package com.example.FoodDeliveryDemoApp.security.service;

import com.example.FoodDeliveryDemoApp.component.userItems.Role;
import com.example.FoodDeliveryDemoApp.component.userItems.admin.domain.Admin;
import com.example.FoodDeliveryDemoApp.component.userItems.user.domain.User;
import com.example.FoodDeliveryDemoApp.component.userItems.admin.repository.AdminRepository;
import com.example.FoodDeliveryDemoApp.component.userItems.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
public class AdminByDefaultService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    public AdminByDefaultService(UserRepository userRepository,
                                 AdminRepository adminRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
    }

    @PostConstruct
    public void init() {
        createDefaultAdminIfNotExist();
    }

    @Transactional
    public void createDefaultAdminIfNotExist() {
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            // Create the default admin user
            User user = User.builder()
                    .username("admin")
                    .firstname("Default")
                    .lastname("Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("root"))
                    .role(Role.ADMIN)
                    .createdAt(Instant.now())
                    .build();

            Admin defaultAdmin = Admin.builder()
                    .level(3L)
                    .user(user)
                    .build();

            adminRepository.save(defaultAdmin); // Save the Admin entity first
            userRepository.save(user); // Save the User entity
        }
    }


}
