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

@Component
public class AuthCookies {

    public static final String ACCESS = "accessToken";
    public static final String REFRESH = "refreshToken";
    private static final String ACCESS_PATH = "/";
    private static final String REFRESH_PATH = "/api/auth";

    @Value("${app.security.cookie.secure:false}")
    private boolean secure;

    @Value("${app.security.cookie.same-site:Lax}")
    private String sameSite;

    public void set(HttpServletResponse res, LoginResult r) {
        add(res, ACCESS, r.accessToken(), ACCESS_PATH, r.expiresIn());
        add(res, REFRESH, r.refreshToken(), REFRESH_PATH, r.refreshExpiresIn());
    }

    public void clear(HttpServletResponse res) {
        add(res, ACCESS, "", ACCESS_PATH, 0);
        add(res, REFRESH, "", REFRESH_PATH, 0);
    }

    public String extract(HttpServletRequest req, String name) {
        Cookie c = WebUtils.getCookie(req, name);
        return c == null ? null : c.getValue();
    }

    private void add(HttpServletResponse res, String name, String value, String path, long maxAge) {
        res.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(name, value)
                .httpOnly(true).secure(secure).sameSite(sameSite)
                .path(path).maxAge(maxAge).build().toString());
    }
}