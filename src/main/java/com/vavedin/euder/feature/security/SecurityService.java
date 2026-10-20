package com.vavedin.euder.feature.security;

import com.vavedin.euder.feature.security.enums.Feature;
import com.vavedin.euder.feature.security.exceptions.UnauthorizedException;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.user.domain.UserRepository;
import com.vavedin.euder.feature.security.exceptions.FeatureAccessDeniedException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SecurityService {
    private final static String BASIC_PREFIX = "Basic ";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User validateAuthorization(String authorization, Feature feature) {
        User user = authenticate(authorization);
        if (!user.canHaveAccessTo(feature)) throw new FeatureAccessDeniedException(user.getEmail(), feature);
        return user;
    }

    private User authenticate(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) throw new UnauthorizedException();
        String encodedCredentials = authorization.substring(BASIC_PREFIX.length());
        String decodedCredentials = getDecodedCredentials(encodedCredentials);

        if (!decodedCredentials.contains(":")) {
            throw new UnauthorizedException();
        }
        String[] parts = decodedCredentials.split(":", 2);

        String username = parts[0];
        String password = parts[1];
        User user = userRepository.findByEmail(username).orElseThrow(UnauthorizedException::new);
        if (!user.doesPasswordMatch(password, passwordEncoder)) throw new UnauthorizedException();
        return user;
    }

    private String getDecodedCredentials(String encodedCredentials) {
        try {
            return new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException();
        }
    }
}
