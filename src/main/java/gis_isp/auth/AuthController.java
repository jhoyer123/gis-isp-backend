package gis_isp.auth;

import gis_isp.auth.dto.*;
import gis_isp.common.exception.InvalidCredentialsException;
import gis_isp.common.exception.InvalidRefreshTokenException;
import gis_isp.security.cookie.AuthCookies;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthCookies authCookies;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest http,
                                               HttpServletResponse response) {
        LoginResult result = authService.login(request, clientIp(http), userAgent(http));
        authCookies.set(response, result);
        return ResponseEntity.ok(LoginResponse.of(result.expiresIn()));
    }

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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest http, HttpServletResponse response) {
        String refreshToken = authCookies.extract(http, AuthCookies.REFRESH);
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        authCookies.clear(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserMe(userId));
    }

    private <T> ResponseEntity<T> unauthorized(HttpServletResponse response) {
        authCookies.clear(response);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }
}