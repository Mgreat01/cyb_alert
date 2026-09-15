package com.cybAlert.cybAlert.business.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DefaultUserService implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserEntity createUser(String username, String email, String rawPassword) {
        String normalizedUsername = username.strip();
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);

        if (repository.existsByUsername(normalizedUsername)) {
            throw new UserAlreadyExistsException("Ce nom d'utilisateur est déjà utilisé");
        }
        if (repository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("Cette adresse e-mail est déjà utilisée");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        return repository.save(new UserEntity(normalizedUsername, normalizedEmail, passwordHash));
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return repository.findByEmail(email.strip().toLowerCase(Locale.ROOT));
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return repository.findByUsername(username.strip());
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        return repository.findById(id);
    }

    public static class UserAlreadyExistsException extends IllegalArgumentException {

        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }
}
