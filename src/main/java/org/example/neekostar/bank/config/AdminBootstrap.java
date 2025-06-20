package org.example.neekostar.bank.config;

import org.example.neekostar.bank.entity.Role;
import org.example.neekostar.bank.entity.User;
import org.example.neekostar.bank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${admin.email}") String adminEmail,
                          @Value("${admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createAdminIfMissing() {
        userRepository.findByEmail(adminEmail)
                .ifPresentOrElse(
                        user -> {
                        },
                        () -> {
                            User admin = new User();
                            admin.setEmail(adminEmail);
                            admin.setPassword(passwordEncoder.encode(adminPassword));
                            admin.setFirstName("Admin");
                            admin.setLastName("Admin");
                            admin.setRole(Role.ADMIN);

                            userRepository.save(admin);
                        }
                );
    }
}
