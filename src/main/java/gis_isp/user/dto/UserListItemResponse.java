package gis_isp.user.dto;

import gis_isp.user.UserEntity;
import gis_isp.user.UserStatus;

import java.util.UUID;

public record UserListItemResponse(
        UUID id,
        String firstName,
        String lastName,
        String username,
        String email,
        String roleName,
        UserStatus status
) {
    public static UserListItemResponse from(UserEntity u) {
        return new UserListItemResponse(
                u.getId(),
                u.getPerson().getFirstName(),
                u.getPerson().getLastName(),
                u.getUsername(),
                u.getEmail(),
                u.getRole().getName(),
                u.getStatus()
        );
    }
}
