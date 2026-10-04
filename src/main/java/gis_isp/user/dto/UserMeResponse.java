package gis_isp.user.dto;

import gis_isp.user.UserEntity;

import java.util.List;
import java.util.UUID;

public record UserMeResponse(
        UUID id,
        String username,
        String email,
        String avatarUrl,
        String firstName,
        String lastName,
        String role,
        List<String> permissions,
        boolean twoFactorEnabled
) {
    public static UserMeResponse from(UserEntity user, List<String> permissionCodes) {
        return new UserMeResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getPerson().getFirstName(),
                user.getPerson().getLastName(),
                user.getRole().getName(),
                permissionCodes,
                user.isTwoFactorEnabled()
        );
    }
}
