package com.cybAlert.cybAlert.business.user;

import com.cybAlert.cybAlert.business.audit.AuditService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final AuditService audit;

    public DefaultUserService(UserRepository repository, PasswordEncoder passwordEncoder,
                              AuditService audit) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
    }

    @Override
    @Transactional
    public UserEntity createUser(String username, String email, String rawPassword,
                                 String firstName, String lastName) {
        String normalizedUsername = username.strip();
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);

        if (repository.existsByUsername(normalizedUsername)) {
            throw new UserAlreadyExistsException("Ce nom d'utilisateur est déjà utilisé");
        }
        if (repository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("Cette adresse e-mail est déjà utilisée");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        UserEntity created = repository.save(new UserEntity(normalizedUsername, normalizedEmail,
                passwordHash, firstName, lastName, UserEntity.Role.VIEWER,
                UserEntity.Status.ACTIVE));
        audit.recordCurrentActor("USER_CREATED", "USER", String.valueOf(created.getId()));
        return created;
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

    @Override
    public Page<UserEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional
    public UserEntity updateUser(UUID id, String firstName, String lastName,
                                 UserEntity.Role role, UserEntity.Status status) {
        UserEntity user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.updateProfile(firstName, lastName, role, status);
        UserEntity updated = repository.save(user);
        audit.recordCurrentActor("USER_UPDATED", "USER", id.toString());
        return updated;
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        UserEntity user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        repository.delete(user);
        audit.recordCurrentActor("USER_DELETED", "USER", id.toString());
    }

    public static class UserAlreadyExistsException extends IllegalArgumentException {

        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends IllegalArgumentException {

        public UserNotFoundException(UUID id) {
            super("Utilisateur introuvable : " + id);
        }
    }
}
