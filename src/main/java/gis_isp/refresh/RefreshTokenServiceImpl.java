package gis_isp.refresh;

import gis_isp.common.exception.InvalidRefreshTokenException;
import gis_isp.refresh.dto.IssuedRefreshToken;
import gis_isp.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Generates and persists a new secure refresh token for a user
    @Override
    @Transactional
    public IssuedRefreshToken issue(UserEntity user, String ipAddress, String userAgent) {
        String raw = generateToken();
        RefreshTokenEntity saved = refreshTokenRepository.save(RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(hash(raw))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(OffsetDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)))
                .build());
        return new IssuedRefreshToken(raw, saved);
    }

    // noRollbackFor: si detectamos reutilización, la revocación masiva debe persistir
    @Override
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public IssuedRefreshToken rotate(String rawToken, String ipAddress, String userAgent) {
        RefreshTokenEntity current = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido"));

        OffsetDateTime now = OffsetDateTime.now();

        if (current.getExpiresAt().isBefore(now)) {
            throw new InvalidRefreshTokenException("Refresh token expirado");
        }

        if (refreshTokenRepository.revokeIfActive(current.getId(), now) == 0) {
            // Token revoked - warning -> cerrar todas las sesiones
            refreshTokenRepository.revokeAllByUserId(current.getUser().getId(), now);
            throw new InvalidRefreshTokenException("Refresh token reutilizado");
        }

        return issue(current.getUser(), ipAddress, userAgent);
    }

    // Revoke a refresh token
    @Override
    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(t -> refreshTokenRepository.revokeIfActive(t.getId(), OffsetDateTime.now()));
    }

    // Revoke all  tokens for a user
    @Override
    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId, OffsetDateTime.now());
    }

    // Scheduled task that runs the cleanup of expired tokens every day at 3:00 AM
    @Override
    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpired() {
        refreshTokenRepository.deleteExpired(OffsetDateTime.now());
    }

    // Generates a random, cryptographically secure token ready for use in URLs
    private static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // Hashes a raw token using SHA-256
    private static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}