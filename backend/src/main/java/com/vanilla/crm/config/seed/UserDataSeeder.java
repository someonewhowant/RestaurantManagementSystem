package com.vanilla.crm.config.seed;

import com.vanilla.crm.entity.User;
import com.vanilla.crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(50)
@RequiredArgsConstructor
@Slf4j
public class UserDataSeeder implements DataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void seed() {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Database is empty. Seeding admin user...");
        User admin = User.builder()
                .email("admin@vanilla.crm")
                .passwordHash(passwordEncoder.encode("admin"))
                .firstName("Иван")
                .lastName("Иванов")
                .restaurantName("Ресторан Vanilla")
                .role(User.Role.OWNER)
                .build();
        userRepository.save(admin);
        log.info("Seeded default admin user (admin@vanilla.crm / admin).");
    }
}
