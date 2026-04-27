package com.example.auth.service;

import com.example.auth.model.AppUser;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final Map<String, AppUser> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedPassword = normalizePassword(password);

        AppUser existing = users.putIfAbsent(
            normalizedUsername,
            new AppUser(normalizedUsername, passwordEncoder.encode(normalizedPassword))
        );
        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }
    }

    public boolean authenticate(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedPassword = normalizePassword(password);

        AppUser user = users.get(normalizedUsername);
        return user != null && passwordEncoder.matches(normalizedPassword, user.passwordHash());
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is required");
        }
        return username.trim().toLowerCase();
    }

    private String normalizePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
        }
        if (password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password must be at least 8 characters");
        }
        return password;
    }
}
