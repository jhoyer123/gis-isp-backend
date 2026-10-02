package gis_isp.person.dto;

import jakarta.validation.constraints.Size;

public record UpdateMyPersonProfileRequest(

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String phone

) {}
