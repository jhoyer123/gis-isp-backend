package gis_isp.auth;

import gis_isp.auth.dto.LoginRequest;
import gis_isp.auth.dto.LoginResult;

public interface AuthService {

        LoginResult login(LoginRequest request, String ipAddress, String userAgent);
        LoginResult refresh(String refreshToken, String ipAddress, String userAgent);
        void logout(String refreshToken);

}
