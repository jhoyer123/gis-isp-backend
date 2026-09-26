package gis_isp.user.dto;

import gis_isp.person.PersonEntity;
import gis_isp.user.UserEntity;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        // Data person
        String firstName,
        String lastName,
        String phone,
        String ci
) {
    // Si UserEntity tiene la relación @OneToOne con PersonEntity:
    public static UserResponse from(UserEntity user) {
        PersonEntity person = user.getPerson();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                person != null ? person.getFirstName() : null,
                person != null ? person.getLastName() : null,
                person != null ? person.getPhone() : null,
                person != null ? person.getCi() : null
        );
    }

    // Si recibes ambas entidades por separado en el Service:
    public static UserResponse from(UserEntity user, PersonEntity person) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                person.getFirstName(),
                person.getLastName(),
                person.getPhone(),
                person.getCi()
        );
    }
}