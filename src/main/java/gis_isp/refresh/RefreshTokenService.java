package gis_isp.refresh;

import gis_isp.refresh.dto.IssuedRefreshToken;
import gis_isp.user.UserEntity;
import java.util.UUID;

public interface RefreshTokenService {

    IssuedRefreshToken issue(UserEntity user, String ipAddress, String userAgent);

    // Validates, revokes the used token, and issues a new one.
    IssuedRefreshToken rotate(String rawToken, String ipAddress, String userAgent);

    // Single session logout (idempotent).
    void revoke(String rawToken);

    // Global logout / password change.
    void revokeAllForUser(UUID userId);

    void purgeExpired();

}
