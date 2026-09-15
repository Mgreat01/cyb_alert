package com.cybAlert.cybAlert.application;

import com.cybAlert.cybAlert.business.auth.AuthService.AccountUnavailableException;
import com.cybAlert.cybAlert.business.auth.AuthService.InvalidCredentialsException;
import com.cybAlert.cybAlert.business.auth.AuthService.InvalidRefreshTokenException;
import com.cybAlert.cybAlert.business.user.DefaultUserService.UserAlreadyExistsException;
import com.cybAlert.cybAlert.business.user.DefaultUserService.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials() {
        return problem(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    ProblemDetail handleInvalidRefreshToken() {
        return problem(HttpStatus.UNAUTHORIZED, "Refresh token invalide ou expiré");
    }

    @ExceptionHandler(AccountUnavailableException.class)
    ProblemDetail handleUnavailableAccount() {
        return problem(HttpStatus.LOCKED, "Ce compte est désactivé ou verrouillé");
    }

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleNotFound(UserNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    ProblemDetail handleConflict(UserAlreadyExistsException exception) {
        return problem(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = problem(HttpStatus.UNPROCESSABLE_ENTITY,
                "La requête contient des données invalides");
        problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList());
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        return problem;
    }
}
