package gis_isp.user.dto;

import gis_isp.user.UserEntity;

import java.util.UUID;

public record UserResponse (

        UUID id,
        String username,
        String email

){

    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

}
