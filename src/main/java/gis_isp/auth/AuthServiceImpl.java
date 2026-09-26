package gis_isp.auth;

import gis_isp.auth.dto.LoginRequest;
import gis_isp.auth.dto.LoginResult;
import gis_isp.common.exception.AccountLockedException;
import gis_isp.common.exception.InvalidCredentialsException;
import gis_isp.refresh.RefreshTokenService;
import gis_isp.refresh.dto.IssuedRefreshToken;
import gis_isp.security.jwt.JwtProvider;
import gis_isp.user.UserEntity;
import gis_isp.user.UserRepository;
import gis_isp.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;

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

    @Override
    public LoginResult login(LoginRequest request, String ipAddress, String userAgent) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        assertNotLocked(user);
        assertActive(user);

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.registerFailedAttempt(
                    user.getId(), lockoutProperties.maxAttempts(), lockoutProperties.lockDuration());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        loginAttemptService.resetAttempts(user.getId());

        IssuedRefreshToken refresh = refreshTokenService.issue(user, ipAddress, userAgent);
        return buildResult(user, refresh);
    }

    private void assertNotLocked(UserEntity user) {
        if (user.isLocked()) {
            if (user.getLockUntil() != null && user.getLockUntil().isBefore(OffsetDateTime.now())) {
                loginAttemptService.resetAttempts(user.getId());
                return;
            }
            throw new AccountLockedException(user.getLockUntil());
        }
    }

    private void assertActive(UserEntity user) {
        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
    }

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

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

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