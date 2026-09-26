package gis_isp.auth;

import gis_isp.auth.dto.LoginRequest;
import gis_isp.auth.dto.LoginResponse;
import gis_isp.auth.dto.LoginResult;
import gis_isp.common.exception.InvalidCredentialsException;
import gis_isp.common.exception.InvalidRefreshTokenException;
import gis_isp.user.UserService;
import gis_isp.user.dto.UserMeResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ACCESS_COOKIE = "accessToken";
    private static final String REFRESH_COOKIE = "refreshToken";
    private static final String REFRESH_PATH = "/api/auth"; // cubre /refresh y /logout

    private final AuthService authService;
    private final UserService userService;

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest http,
            HttpServletResponse response
    ) {
        LoginResult result = authService.login(request, clientIp(http), userAgent(http));
        setAuthCookies(response, result);
        return ResponseEntity.ok(LoginResponse.of(result.expiresIn()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            HttpServletRequest http,
            HttpServletResponse response
    ) {
        String refreshToken = extractCookie(http, REFRESH_COOKIE);
        if (refreshToken == null || refreshToken.isBlank()) {
            return unauthorized(response);
        }
        try {
            LoginResult result = authService.refresh(refreshToken, clientIp(http), userAgent(http));
            setAuthCookies(response, result);
            return ResponseEntity.ok(LoginResponse.of(result.expiresIn()));
        } catch (InvalidRefreshTokenException | InvalidCredentialsException e) {
            return unauthorized(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest http, HttpServletResponse response) {
        String refreshToken = extractCookie(http, REFRESH_COOKIE);
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserMe(userId));
    }

    // ---------- helpers ----------

    private void setAuthCookies(HttpServletResponse response, LoginResult result) {
        System.out.println("VALOR DE TOKEN SECONDS: " + result.expiresIn());
        System.out.println("VALOR DE REFRESH SECONDS: " + result.refreshExpiresIn());
        addCookie(response, ACCESS_COOKIE, result.accessToken(), "/", result.expiresIn());
        addCookie(response, REFRESH_COOKIE, result.refreshToken(), REFRESH_PATH, result.refreshExpiresIn());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, ACCESS_COOKIE, "", "/", 0);
        addCookie(response, REFRESH_COOKIE, "", REFRESH_PATH, 0);
    }

    private void addCookie(HttpServletResponse response, String name, String value, String path, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private <T> ResponseEntity<T> unauthorized(HttpServletResponse response) {
        clearAuthCookies(response);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (var cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr(); // detrás de proxy: ver forward-headers-strategy
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }
}