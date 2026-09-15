package com.cybAlert.cybAlert.infrastructure.auth;

import com.cybAlert.cybAlert.business.user.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class JwtTokenService {

    private final JwtEncoder encoder;
    private final Duration accessTokenDuration;

    public JwtTokenService(JwtEncoder encoder,
                           @Value("${cyberwatch.security.access-token-duration}") Duration duration) {
        this.encoder = encoder;
        this.accessTokenDuration = duration;
    }

    public String createAccessToken(UserEntity user) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cyberwatch")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(accessTokenDuration))
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("roles", user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long accessTokenExpiresInSeconds() {
        return accessTokenDuration.toSeconds();
    }
}
