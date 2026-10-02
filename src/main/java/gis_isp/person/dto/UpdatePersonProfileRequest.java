package gis_isp.person.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePersonProfileRequest (

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        String lastName,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String phone,

        @Size(max = 20, message = "La cédula no puede superar los 20 caracteres")
        String ci

){}
