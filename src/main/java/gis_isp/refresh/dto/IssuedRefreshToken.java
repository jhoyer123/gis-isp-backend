package gis_isp.refresh.dto;

import gis_isp.refresh.RefreshTokenEntity;

public record IssuedRefreshToken(

        String rawToken,
        RefreshTokenEntity entity

) {}
