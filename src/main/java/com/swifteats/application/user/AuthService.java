package com.swifteats.application.user;

import com.swifteats.domain.user.User;
import com.swifteats.domain.user.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Key;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Key jwtKey;
    private final long jwtTtlSeconds;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       String jwtSecretBase64,
                       long jwtTtlSeconds) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretBase64));
        this.jwtTtlSeconds = jwtTtlSeconds;
    }

    public User register(String firstName, String lastName, String mobile, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("EMAIL_EXISTS");
        }
        String hash = passwordEncoder.encode(rawPassword);
        User toSave = new User(UUID.randomUUID(), firstName, lastName, mobile, email, hash, OffsetDateTime.now());
        return userRepository.save(toSave);
    }

    public Optional<String> loginAndIssueToken(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return Optional.empty();
        User user = userOpt.get();
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return Optional.empty();
        }
        Date now = new Date();
        Date exp = new Date(now.getTime() + (jwtTtlSeconds * 1000));
        String token = Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
        return Optional.of(token);
    }
}



