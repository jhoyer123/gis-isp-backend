package gis_isp.auth.dto;

public record LoginResponse(
        String tokenType,
        long expiresIn
) {
    public static LoginResponse of(long expiresIn) {
        return new LoginResponse("Bearer", expiresIn);
    }
}