package com.portfolio.support.security;

import com.portfolio.support.shared.exception.NotFoundException;
import com.portfolio.support.user.User;
import com.portfolio.support.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUser {
    private final UserRepository users;

    public User get(Authentication authentication) {
        return users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Authenticated user no longer exists"));
    }
}
