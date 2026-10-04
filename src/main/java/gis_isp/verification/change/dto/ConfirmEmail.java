package gis_isp.verification.change.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmail(
        @NotBlank String token
) {
}
