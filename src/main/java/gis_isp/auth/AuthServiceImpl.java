package gis_isp.auth;

import gis_isp.auth.dto.LoginRequest;
import gis_isp.auth.dto.LoginResult;
import gis_isp.auth.token.RefreshTokenManager;
import gis_isp.security.jwt.JwtProvider;
import gis_isp.exception.InvalidCredentialsException;
import gis_isp.user.UserEntity;
import gis_isp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenManager refreshTokenManager;

    @Override
    public LoginResult login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String accessToken = jwtProvider.generateToken(user);
        String refreshToken = refreshTokenManager.generate(user);

        return new LoginResult(accessToken, refreshToken, jwtProvider.getExpirationMs() / 1000, refreshTokenManager.getRefreshExpirationMs() / 1000);
    }

    @Override
    public LoginResult refresh(String refreshToken) {
        // 1. Validar el refresh token recibido -> obtenemos el usuario dueño
        UserEntity user = refreshTokenManager.validate(refreshToken);

        // 2. Generar un nuevo access token
        String newAccessToken = jwtProvider.generateToken(user);

        // 3. Rotar el refresh token: revocamos el viejo y generamos uno nuevo.
        //    Esto es más seguro que reusar el mismo: si alguien roba un refresh token
        //    y lo usa, el dueño legítimo notará que su sesión se invalida en su próximo refresh,
        //    lo cual es una señal de alerta.
        String newRefreshToken = refreshTokenManager.generate(user);

        return new LoginResult(newAccessToken, newRefreshToken, jwtProvider.getExpirationMs() / 1000, refreshTokenManager.getRefreshExpirationMs() / 1000);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenManager.revoke(refreshToken);
    }

}
