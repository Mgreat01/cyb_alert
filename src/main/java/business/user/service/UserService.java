package business.user.service;

import business.user.model.User;

import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> findByEmail(String email);
}
