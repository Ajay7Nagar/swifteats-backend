package com.swifteats.adapters.persistence.user;

import com.swifteats.domain.user.User;
import com.swifteats.domain.user.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public UserPersistenceAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        UserEntity e = toEntity(user);
        UserEntity saved = jpa.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    private UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.id = u.getId();
        e.firstName = u.getFirstName();
        e.lastName = u.getLastName();
        e.mobile = u.getMobile();
        e.email = u.getEmail();
        e.passwordHash = u.getPasswordHash();
        e.createdAt = u.getCreatedAt();
        return e;
    }

    private User toDomain(UserEntity e) {
        return new User(
                e.id,
                e.firstName,
                e.lastName,
                e.mobile,
                e.email,
                e.passwordHash,
                e.createdAt
        );
    }
}



