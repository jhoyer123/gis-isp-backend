package gis_isp.auth;

import gis_isp.auth.dto.LoginRequest;
import gis_isp.auth.dto.LoginResult;

public interface AuthService {

        // login service
        LoginResult login(LoginRequest request, String ipAddress, String userAgent);

        // refresh token service
        LoginResult refresh(String refreshToken, String ipAddress, String userAgent);

        // logout service
        void logout(String refreshToken);

}
