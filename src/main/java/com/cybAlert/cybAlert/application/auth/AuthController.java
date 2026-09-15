package com.cybAlert.cybAlert.application.auth;

import com.cybAlert.cybAlert.business.auth.AuthService;
import com.cybAlert.cybAlert.business.auth.AuthService.TokenPair;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    TokenPair login(@Valid @RequestBody LoginRequest request) {
        return service.login(request.email(), request.password());
    }

    @PostMapping("/refresh")
    TokenPair refresh(@Valid @RequestBody RefreshRequest request) {
        return service.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@Valid @RequestBody RefreshRequest request) {
        service.logout(request.refreshToken());
    }

    record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    record RefreshRequest(@NotBlank String refreshToken) {
    }
}
