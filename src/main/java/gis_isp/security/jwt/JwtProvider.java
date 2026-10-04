package gis_isp.security.jwt;

import gis_isp.user.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private final SecretKey key;

    @Getter
    private final long expirationMs;

    private static final String PURPOSE = "purpose";
    private static final String CHALLENGE = "2FA_CHALLENGE";
    private static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);

    // Creates the JWT key and sets the token expiration time
    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration}") long expirationMs) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("jwt.secret debe tener al menos 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    // Creates a new access token for the user
    public String generateAccessToken(UserEntity user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    // Validates the token and returns its claims
    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Generates a short-lived JWT token used as a 2FA challenge for a specific user
    public String generateTwoFactorChallenge(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(PURPOSE, CHALLENGE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(CHALLENGE_TTL)))
                .signWith(key)
                .compact();
    }

    // Validates the 2FA challenge token, verifies its specific purpose, and extracts the user ID
    public UUID validateTwoFactorChallenge(String token) {
        Claims c = validateAndGetClaims(token);
        if (!CHALLENGE.equals(c.get(PURPOSE, String.class))) throw new JwtException("Token no es un reto 2FA");
        return UUID.fromString(c.getSubject());
    }

    // Returns the lifetime of the 2FA challenge in seconds
    public long getChallengeSeconds() { return CHALLENGE_TTL.toSeconds(); }
}