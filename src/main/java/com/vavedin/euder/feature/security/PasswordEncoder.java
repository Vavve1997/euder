package com.vavedin.euder.feature.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) throw new IllegalArgumentException("Password must not be null or blank");
        return bCryptPasswordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) return false;
        return bCryptPasswordEncoder.matches(rawPassword, hashedPassword);
    }
}
