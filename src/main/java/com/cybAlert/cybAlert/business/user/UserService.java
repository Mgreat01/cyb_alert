package com.cybAlert.cybAlert.business.user;

import java.util.Optional;

public interface UserService {
    UserEntity createUser(UserEntity user);
    Optional<UserEntity> findByEmail(String email);
}
