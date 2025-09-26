package com.swifteats.adapters.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
class UserEntity {
    @Id
    UUID id;
    @Column(name = "first_name")
    String firstName;
    @Column(name = "last_name")
    String lastName;
    String mobile;
    String email;
    @Column(name = "password_hash")
    String passwordHash;
    @Column(name = "created_at")
    OffsetDateTime createdAt;
}



