package gis_isp.user.dto;

import gis_isp.person.PersonEntity;
import gis_isp.user.UserEntity;
import gis_isp.user.UserStatus;

import java.util.UUID;

public record UserDetailResponse(
        UUID id,

        // Data Person
        String firstName,
        String lastName,
        String phone,
        String ci,

        // Data User
        String username,
        String email,
        String avatarUrl,
        Long roleId,
        String roleName,
        UserStatus status,
        boolean twoFactorEnabled
) {
    public static UserDetailResponse from(UserEntity user) {
        PersonEntity person = user.getPerson();
        return new UserDetailResponse(
                user.getId(),
                person != null ? person.getFirstName() : null,
                person != null ? person.getLastName() : null,
                person != null ? person.getPhone() : null,
                person != null ? person.getCi() : null,
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole().getId(),
                user.getRole().getName(),
                user.getStatus(),
                user.isTwoFactorEnabled()
        );
    }
}