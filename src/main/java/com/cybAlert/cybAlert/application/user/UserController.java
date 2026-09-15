package com.cybAlert.cybAlert.application.user;

import com.cybAlert.cybAlert.business.user.DefaultUserService.UserNotFoundException;
import com.cybAlert.cybAlert.business.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse create(@Valid @RequestBody UserRequest request) {
        return UserResponse.from(service.createUser(
                request.username(), request.email(), request.password()));
    }

    @GetMapping
    Page<UserResponse> findAll(Pageable pageable) {
        return service.findAll(pageable).map(UserResponse::from);
    }

    @GetMapping("/{id}")
    UserResponse findById(@PathVariable UUID id) {
        return service.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @PatchMapping("/{id}")
    UserResponse update(@PathVariable UUID id,
                        @Valid @RequestBody UserUpdateRequest request) {
        return UserResponse.from(service.updateUser(id, request.firstName(), request.lastName(),
                request.role(), request.status()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) {
        service.deleteUser(id);
    }
}
