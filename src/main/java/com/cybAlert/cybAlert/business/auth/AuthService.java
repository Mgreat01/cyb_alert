package com.cybAlert.cybAlert.business.auth;

import com.cybAlert.cybAlert.business.user.UserEntity;
import com.cybAlert.cybAlert.business.user.UserRepository;
import com.cybAlert.cybAlert.infrastructure.auth.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
@Transactional
public class AuthService {

    private static final int MAXIMUM_LOGIN_ATTEMPTS = 5;

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokens;
    private final Duration refreshTokenDuration;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens,
                       PasswordEncoder passwordEncoder, JwtTokenService jwtTokens,
                       @Value("${cyberwatch.security.refresh-token-duration}") Duration duration) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokens = jwtTokens;
        this.refreshTokenDuration = duration;
    }

    public TokenPair login(String email, String password) {
        UserEntity user = users.findByEmail(email.strip().toLowerCase(Locale.ROOT))
                .orElseThrow(InvalidCredentialsException::new);
        if (user.getStatus() != UserEntity.Status.ACTIVE) {
            throw new AccountUnavailableException();
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.registerFailedLogin(MAXIMUM_LOGIN_ATTEMPTS);
            users.save(user);
            throw new InvalidCredentialsException();
        }
        user.resetFailedLogins();
        users.save(user);
        return issueTokens(user);
    }

    public TokenPair refresh(String rawRefreshToken) {
        RefreshTokenEntity token = refreshTokens.findByTokenHash(hash(rawRefreshToken))
                .filter(candidate -> candidate.isUsableAt(Instant.now()))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (token.getUser().getStatus() != UserEntity.Status.ACTIVE) {
            throw new AccountUnavailableException();
        }
        token.revoke();
        refreshTokens.save(token);
        return issueTokens(token.getUser());
    }

    public void logout(String rawRefreshToken) {
        refreshTokens.findByTokenHash(hash(rawRefreshToken)).ifPresent(token -> {
            token.revoke();
            refreshTokens.save(token);
        });
    }

    private TokenPair issueTokens(UserEntity user) {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        String rawRefreshToken = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(randomBytes);
        refreshTokens.save(new RefreshTokenEntity(user, hash(rawRefreshToken),
                Instant.now().plus(refreshTokenDuration)));
        return new TokenPair(jwtTokens.createAccessToken(user), rawRefreshToken,
                jwtTokens.accessTokenExpiresInSeconds());
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponible", exception);
        }
    }

    public record TokenPair(String accessToken, String refreshToken, long expiresIn) {
    }

    public static class InvalidCredentialsException extends RuntimeException {
    }

    public static class InvalidRefreshTokenException extends RuntimeException {
    }

    public static class AccountUnavailableException extends RuntimeException {
    }
}
