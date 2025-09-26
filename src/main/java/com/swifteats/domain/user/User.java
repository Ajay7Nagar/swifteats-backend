package com.swifteats.domain.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public class User {
    private UUID id;
    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private String passwordHash;
    private OffsetDateTime createdAt;

    public User(UUID id,
                String firstName,
                String lastName,
                String mobile,
                String email,
                String passwordHash,
                OffsetDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}



