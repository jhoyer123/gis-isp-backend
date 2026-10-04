package gis_isp.twofactor.dto;

public record SetupResponse(
        String secret,
        String otpauthUri
) {}
