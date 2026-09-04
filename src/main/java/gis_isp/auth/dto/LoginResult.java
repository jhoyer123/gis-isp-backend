package gis_isp.auth.dto;

public record LoginResult(
        String accessToken,
        String refreshToken,
        long expiresIn,
        long refreshExpiresIn
) {}
