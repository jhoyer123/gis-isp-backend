package gis_isp.auth.token;

import gis_isp.exception.InvalidTokenException;
import gis_isp.user.UserEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Getter
@Setter
public class RefreshTokenManager {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Genera un nuevo refresh token para el usuario, lo guarda hasheado en BD,
     * y devuelve el token CRUDO (sin hashear) para enviarlo al cliente.
     */
    public String generate(UserEntity user) {
        String rawToken = generateRawToken();
        String hashedToken = hash(rawToken);
        Instant newExpiration = Instant.now().plusMillis(refreshExpirationMs);

        // Buscamos si el usuario ya tiene un token en la BD
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    // Si existe, modificamos el registro actual (UPDATE)
                    existingToken.setTokenHash(hashedToken);
                    existingToken.setExpiresAt(newExpiration);
                    existingToken.setRevoked(false); // Por si estaba revocado
                    return existingToken;
                })
                .orElseGet(() -> {
                    // Si no existe, creamos uno nuevo (INSERT)
                    return RefreshTokenEntity.builder()
                            .user(user)
                            .tokenHash(hashedToken)
                            .expiresAt(newExpiration)
                            .build();
                });

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    /**
     * Valida un refresh token crudo recibido del cliente.
     * Si es válido (existe, no expiró, no fue revocado), devuelve el usuario asociado.
     * Si no, lanza InvalidTokenException.
     */
    public UserEntity validate(String rawToken) {
        String hashedToken = hash(rawToken);

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token inválido"));

        if (refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token revocado");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expirado");
        }

        return refreshToken.getUser();
    }

    /**
     * Revoca un refresh token (usado en logout).
     */
    public void revoke(String rawToken) {
        String hashedToken = hash(rawToken);

        refreshTokenRepository.findByTokenHash(hashedToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private String generateRawToken() {
        byte[] randomBytes = new byte[64];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre existe en cualquier JVM estándar; esto nunca debería pasar
            throw new IllegalStateException("Error al hashear el token", e);
        }
    }

}
