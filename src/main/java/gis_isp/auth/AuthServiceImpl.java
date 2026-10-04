package gis_isp.auth;

import gis_isp.auth.dto.LoginOutcome;
import gis_isp.auth.dto.LoginRequest;
import gis_isp.auth.dto.LoginResult;
import gis_isp.common.exception.AccountLockedException;
import gis_isp.common.exception.InvalidCredentialsException;
import gis_isp.refresh.RefreshTokenService;
import gis_isp.refresh.dto.IssuedRefreshToken;
import gis_isp.security.jwt.JwtProvider;
import gis_isp.twofactor.TwoFactorService;
import gis_isp.user.UserEntity;
import gis_isp.user.UserRepository;
import gis_isp.user.UserStatus;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final LoginLockoutProperties lockoutProperties;
    private final LoginAttemptService loginAttemptService;
    private final TwoFactorService twoFactorService;

    // login service
    @Override
    public LoginOutcome login(LoginRequest request, String ipAddress, String userAgent) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        assertNotLocked(user);
        assertActive(user);

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.registerFailedAttempt(
                    user.getId(), lockoutProperties.maxAttempts(), lockoutProperties.lockDuration());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        // Con 2FA NO se reinician los intentos aquí: solo al completar el segundo paso
        if (user.isTwoFactorEnabled()) {
            return LoginOutcome.challenge(
                    jwtProvider.generateTwoFactorChallenge(user.getId()), jwtProvider.getChallengeSeconds());
        }

        loginAttemptService.resetAttempts(user.getId());
        IssuedRefreshToken refresh = refreshTokenService.issue(user, ipAddress, userAgent);
        return LoginOutcome.ok(buildResult(user, refresh));
    }

    @Override
    public LoginResult verifyTwoFactor(String challengeToken, String code, String ipAddress, String userAgent) {
        UUID userId;
        try {
            userId = jwtProvider.validateTwoFactorChallenge(challengeToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidCredentialsException("Reto expirado, inicia sesión de nuevo");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));
        assertNotLocked(user);
        assertActive(user);

        if (!user.isTwoFactorEnabled() || !twoFactorService.verifyForLogin(userId, code)) {
            loginAttemptService.registerFailedAttempt(
                    userId, lockoutProperties.maxAttempts(), lockoutProperties.lockDuration());
            throw new InvalidCredentialsException("Código inválido");
        }

        loginAttemptService.resetAttempts(userId);
        IssuedRefreshToken refresh = refreshTokenService.issue(user, ipAddress, userAgent);
        return buildResult(user, refresh);
    }

    // verify if user is locked
    private void assertNotLocked(UserEntity user) {
        if (user.isLocked()) {
            if (user.getLockUntil() != null && user.getLockUntil().isBefore(OffsetDateTime.now())) {
                loginAttemptService.resetAttempts(user.getId());
                return;
            }
            throw new AccountLockedException(user.getLockUntil());
        }
    }

    // verify if user is active
    private void assertActive(UserEntity user) {
        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
    }

    // refreshToken service
    @Override
    public LoginResult refresh(String refreshToken, String ipAddress, String userAgent) {
        // Valída, revoca el token usado y emite uno nuevo (con detección de reutilización)
        IssuedRefreshToken refresh = refreshTokenService.rotate(refreshToken, ipAddress, userAgent);
        UserEntity user = refresh.entity().getUser();

        try {
            assertActive(user);
        } catch (InvalidCredentialsException e) {
            refreshTokenService.revokeAllForUser(user.getId());
            throw e;
        }

        return buildResult(user, refresh);
    }

    // logout service
    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    // Builds the login result object by generating a JWT access token for the user
    private LoginResult buildResult(UserEntity user, IssuedRefreshToken refresh) {
        String accessToken = jwtProvider.generateAccessToken(user);
        long refreshSeconds = Duration.between(OffsetDateTime.now(), refresh.entity().getExpiresAt()).toSeconds();
        return new LoginResult(
                accessToken,
                refresh.rawToken(),
                jwtProvider.getExpirationMs() / 1000,
                refreshSeconds);
    }
}