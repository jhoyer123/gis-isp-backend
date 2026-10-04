package gis_isp.twofactor;

import gis_isp.twofactor.dto.SetupResponse;
import gis_isp.user.UserEntity;
import gis_isp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final TwoFactorRepository repo;
    private final UserRepository userRepository;
    private final TotpService totp;
    private final SecretCipher cipher;
    private final BackupCodeService backupCodes;
    private final PasswordEncoder passwordEncoder;

    // Genera un secreto nuevo (pendiente hasta confirmar con /enable).
    @Transactional
    public SetupResponse setup(UUID userId) {
        UserEntity user = getUser(userId);
        if (user.isTwoFactorEnabled()) throw error(HttpStatus.CONFLICT, "El 2FA ya está activo");

        String secret = totp.generateSecret();

        UserTwoFactorAuthEntity entity = repo.findById(userId)
                .orElseGet(() -> UserTwoFactorAuthEntity.builder().user(user).build());
        entity.setSecretKey(cipher.encrypt(secret));
        entity.setLastUsedStep(null);
        entity.setBackupCodes(null);

        repo.save(entity);

        return new SetupResponse(secret, totp.buildOtpAuthUri(secret, user.getEmail()));
    }

    // Confirma con el primer código y activa. Devuelve los backups codes en claro.
    @Transactional
    public List<String> enable(UUID userId, String code) {
        UserEntity user = getUser(userId);

        if (user.isTwoFactorEnabled()) throw error(HttpStatus.CONFLICT, "El 2FA ya está activo");

        UserTwoFactorAuthEntity entity = repo.findById(userId)
                .orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "Primero inicia la configuración"));

        if (entity.getUpdatedAt().isBefore(OffsetDateTime.now().minusMinutes(10)))
            throw error(HttpStatus.BAD_REQUEST, "La configuración expiró, genera un nuevo QR");

        long step = totp.verify(cipher.decrypt(entity.getSecretKey()), code, null)
                .orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "Código inválido"));

        List<String> plain = backupCodes.generate();
        entity.setLastUsedStep(step);
        entity.setBackupCodes(backupCodes.hashAll(plain));
        user.setTwoFactorEnabled(true);
        return plain;
    }

    // Requiere contraseña + (código TOTP o backup code).
    @Transactional
    public void disable(UUID userId, String password, String code) {
        UserEntity user = getUser(userId);
        UserTwoFactorAuthEntity entity = requireEnabled(user);

        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw error(HttpStatus.BAD_REQUEST, "Contraseña incorrecta");
        verifySecondFactor(entity, code);

        repo.delete(entity);
        user.setTwoFactorEnabled(false);
    }

    @Transactional
    public boolean verifyForLogin(UUID userId, String code) {
        UserTwoFactorAuthEntity e = repo.findById(userId).orElse(null);
        if (e == null) return false;
        if (code.matches("\\d{6}")) {
            OptionalLong step = totp.verify(cipher.decrypt(e.getSecretKey()), code, e.getLastUsedStep());
            step.ifPresent(e::setLastUsedStep);
            return step.isPresent();
        }
        return backupCodes.consume(e, code); // gasta el código de respaldo
    }

    // ---------- helpers ----------

    private void verifySecondFactor(UserTwoFactorAuthEntity entity, String code) {
        if (code.matches("\\d{6}")) verifyTotp(entity, code);
        else if (!backupCodes.consume(entity, code))
            throw error(HttpStatus.BAD_REQUEST, "Código inválido");
    }

    private void verifyTotp(UserTwoFactorAuthEntity entity, String code) {
        long step = totp.verify(cipher.decrypt(entity.getSecretKey()), code, entity.getLastUsedStep())
                .orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "Código inválido"));
        entity.setLastUsedStep(step);
    }

    private UserTwoFactorAuthEntity requireEnabled(UserEntity user) {
        if (!user.isTwoFactorEnabled()) throw error(HttpStatus.CONFLICT, "El 2FA no está activo");
        return repo.findById(user.getId())
                .orElseThrow(() -> error(HttpStatus.CONFLICT, "El 2FA no está configurado"));
    }

    private UserEntity getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private ResponseStatusException error(HttpStatus status, String msg) {
        return new ResponseStatusException(status, msg);
    }
}
