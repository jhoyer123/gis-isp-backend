package gis_isp.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String phone

) {}
