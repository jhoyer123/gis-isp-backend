package gis_isp.auth;

import gis_isp.auth.dto.*;
import gis_isp.common.exception.InvalidCredentialsException;
import gis_isp.common.exception.InvalidRefreshTokenException;
import gis_isp.security.cookie.AuthCookies;
import gis_isp.twofactor.dto.CodeRequest;
import gis_isp.user.UserService;
import gis_isp.user.dto.UserMeResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthCookies authCookies;

    // PASO 1 DEL LOGIN (público).
    // Sin 2FA: emite cookies de sesión (access + refresh) y listo.
    // Con 2FA: NO emite sesión. Solo pone la cookie "2fa_challenge" (5 min) y responde
    //          {twoFactorRequired: true} para que el front pida el código.
    // OJO: con 2FA los intentos fallidos NO se reinician aquí, solo al completar el paso 2.
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest http, HttpServletResponse response) {
        LoginOutcome out = authService.login(request, clientIp(http), userAgent(http));
        if (out.twoFactorRequired()) {
            authCookies.setChallenge(response, out.challengeToken(), out.challengeExpiresIn());
            return ResponseEntity.ok(Map.of("twoFactorRequired", true));
        }
        authCookies.set(response, out.session());
        return ResponseEntity.ok(LoginResponse.of(out.session().expiresIn()));
    }

    // PASO 2 DEL LOGIN (público, solo si el usuario tiene 2FA).
    // Lee la cookie "2fa_challenge" (la del paso 1) y valida el código: sirve el de la app
    // (6 dígitos) o un código de respaldo (se consume al usarlo).
    // Código malo = intento fallido (mismo contador y bloqueo que la contraseña).
    // Código bueno = reinicia intentos, borra el reto y emite la sesión normal.
    // Si la cookie falta o expiró, el usuario debe volver a hacer el paso 1.
    @PostMapping("/2fa/verify")
    public ResponseEntity<LoginResponse> verifyTwoFactor(@Valid @RequestBody CodeRequest req,
                                                         HttpServletRequest http, HttpServletResponse response) {
        String challenge = authCookies.extract(http, AuthCookies.CHALLENGE);
        if (challenge == null || challenge.isBlank()) {
            throw new InvalidCredentialsException("Sesión expirada o ausente. Inicie sesión otra vez.");
        }
        LoginResult result = authService.verifyTwoFactor(challenge, req.code(), clientIp(http), userAgent(http));
        authCookies.clearChallenge(response);
        authCookies.set(response, result);
        return ResponseEntity.ok(LoginResponse.of(result.expiresIn()));
    }

    // RENOVAR SESIÓN (usa la cookie refresh).
    // Rota el refresh token (el usado se revoca; si alguien lo reutiliza, se detecta).
    // Cualquier fallo = 401 + limpiar cookies. No pasa por 2FA: la sesión ya lo pasó al iniciar.
    // El front NO debe llamar a refresh cuando falla /login o /2fa/verify.
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest http, HttpServletResponse response) {
        String refreshToken = authCookies.extract(http, AuthCookies.REFRESH);
        if (refreshToken == null || refreshToken.isBlank()) {
            return unauthorized(response);
        }
        try {
            LoginResult result = authService.refresh(refreshToken, clientIp(http), userAgent(http));
            authCookies.set(response, result);
            return ResponseEntity.ok(LoginResponse.of(result.expiresIn()));
        } catch (InvalidRefreshTokenException | InvalidCredentialsException e) {
            return unauthorized(response);
        }
    }

    // CERRAR SESIÓN.
    // Revoca el refresh token en BD y limpia las cookies. Siempre responde 204,
    // aunque no haya cookie (el logout nunca debe fallar).
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest http, HttpServletResponse response) {
        String refreshToken = authCookies.extract(http, AuthCookies.REFRESH);
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        authCookies.clear(response);
        return ResponseEntity.noContent().build();
    }

    // USUARIO ACTUAL (autenticado).
    // El principal es el UUID que puso JwtFilter. El token del reto 2FA NO sirve aquí:
    // JwtFilter rechaza cualquier token con claim "purpose".
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserMe(userId));
    }

    // Respuesta 401 + limpia cookies de sesión.
    private <T> ResponseEntity<T> unauthorized(HttpServletResponse response) {
        authCookies.clear(response);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Get IP and useragent from client
    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }
}