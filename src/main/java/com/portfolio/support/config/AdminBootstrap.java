package com.portfolio.support.config;

import com.portfolio.support.user.Role;
import com.portfolio.support.user.User;
import com.portfolio.support.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!users.existsByEmailIgnoreCase(properties.adminEmail())) {
            users.save(new User(properties.adminEmail(), passwordEncoder.encode(properties.adminPassword()),
                    "System Administrator", Role.ADMIN));
        }
    }
}
