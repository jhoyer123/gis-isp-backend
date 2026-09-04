package gis_isp.auth;

import gis_isp.auth.dto.LoginRequest;
import gis_isp.auth.dto.LoginResult;

public interface AuthService {

        LoginResult login(LoginRequest request);
        LoginResult refresh(String refreshToken);
        void logout(String refreshToken);
}
