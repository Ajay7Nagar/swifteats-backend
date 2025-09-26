package com.swifteats.infrastructure.config;

import com.swifteats.domain.user.User;
import com.swifteats.domain.user.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class TestUserSeeder {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        String email = "test@swifteats.local";
        if (!userRepository.existsByEmail(email)) {
            String hash = passwordEncoder.encode("test123");
            User u = new User(UUID.fromString("00000000-0000-0000-0000-000000000100"),
                    "Test", "User", "9999999999", email, hash, OffsetDateTime.now());
            userRepository.save(u);
        }
    }
}



