package gis_isp.user.dto;

import gis_isp.common.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(

        @NotBlank(message = "La contraseña anterior es obligatoria")
        String currentPassword,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @ValidPassword
        String newPassword

) {}
