package gis_isp.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UpdateMyUserProfileRequest(

        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 30 caracteres")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username inválido"
        )
        String username,

        MultipartFile avatar

) {
}
