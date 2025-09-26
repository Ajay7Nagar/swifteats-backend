package com.swifteats.adapters.web.auth;

import com.swifteats.application.user.AuthService;
import com.swifteats.domain.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        User u = authService.register(req.firstName(), req.lastName(), req.mobile(), req.email(), req.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "id", u.getId(),
                        "firstName", u.getFirstName(),
                        "lastName", u.getLastName(),
                        "email", u.getEmail()
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return authService.loginAndIssueToken(req.email(), req.password())
                .<ResponseEntity<?>>map(t -> ResponseEntity.ok(Map.of("token", t)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "INVALID_CREDENTIALS")));
    }

    public record RegisterRequest(@NotBlank String firstName,
                                  @NotBlank String lastName,
                                  @NotBlank String mobile,
                                  @Email String email,
                                  @NotBlank String password) {}

    public record LoginRequest(@Email String email, @NotBlank String password) {}
}



