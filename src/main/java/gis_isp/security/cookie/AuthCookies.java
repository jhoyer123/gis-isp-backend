package gis_isp.security.cookie;

import gis_isp.auth.dto.LoginResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

// Component responsible for managing authentication-related cookies (access token, refresh token, and 2FA challenge)
@Component
public class AuthCookies {

    public static final String ACCESS = "accessToken";
    public static final String REFRESH = "refreshToken";
    private static final String ACCESS_PATH = "/";
    private static final String REFRESH_PATH = "/api/auth";
    public static final String CHALLENGE = "2fa_challenge";

    @Value("${app.security.cookie.secure:false}")
    private boolean secure;

    @Value("${app.security.cookie.same-site:Lax}")
    private String sameSite;

    // Sets the access token and refresh token cookies in the response using data from the login result
    public void set(HttpServletResponse res, LoginResult r) {
        add(res, ACCESS, r.accessToken(), ACCESS_PATH, r.expiresIn());
        add(res, REFRESH, r.refreshToken(), REFRESH_PATH, r.refreshExpiresIn());
    }

    // Clears the access token and refresh token cookies by setting their max age to 0
    public void clear(HttpServletResponse res) {
        add(res, ACCESS, "", ACCESS_PATH, 0);
        add(res, REFRESH, "", REFRESH_PATH, 0);
    }

    // Extracts the value of a specific cookie by its name from the incoming HTTP request
    public String extract(HttpServletRequest req, String name) {
        Cookie c = WebUtils.getCookie(req, name);
        return c == null ? null : c.getValue();
    }

    // Private helper method to build, configure, and add a Set-Cookie header to the HTTP response
    private void add(HttpServletResponse res, String name, String value, String path, long maxAge) {
        res.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(name, value)
                .httpOnly(true).secure(secure).sameSite(sameSite)
                .path(path).maxAge(maxAge).build().toString());
    }

    // Sets a temporary cookie for the 2FA challenge with a specific expiration time
    public void setChallenge(HttpServletResponse res, String token, long seconds) {
        res.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(CHALLENGE, token)
                .httpOnly(true).secure(secure).sameSite("Strict")
                .path("/api/auth/2fa").maxAge(seconds).build().toString());
    }

    // Clears the 2FA challenge cookie by setting its max age to 0
    public void clearChallenge(HttpServletResponse res) {
        res.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(CHALLENGE, "")
                .httpOnly(true).secure(secure).sameSite("Strict")
                .path("/api/auth/2fa").maxAge(0).build().toString());
    }

}