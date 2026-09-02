package com.cybAlert.cybAlert.business.user;

import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> findByEmail(String email);
}
