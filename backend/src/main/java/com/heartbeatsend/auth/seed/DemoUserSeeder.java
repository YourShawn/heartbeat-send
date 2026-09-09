package com.heartbeatsend.auth.seed;

import com.heartbeatsend.auth.entity.AppUser;
import com.heartbeatsend.auth.repository.UserRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class DemoUserSeeder implements ApplicationRunner {

    public static final String DEMO_USERNAME = "demo";
    public static final String DEMO_PASSWORD = "demo123";

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername(DEMO_USERNAME).isPresent()) {
            return;
        }
        Instant now = Instant.now();
        AppUser user = new AppUser();
        user.setUsername(DEMO_USERNAME);
        user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        user.setDisplayName("Demo Listener");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);
        log.info("Seeded demo user '{}' (password: {})", DEMO_USERNAME, DEMO_PASSWORD);
    }
}
