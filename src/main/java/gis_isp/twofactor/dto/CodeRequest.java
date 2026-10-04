package gis_isp.twofactor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodeRequest(
        @NotBlank @Size(max = 20)
        String code
) {
}
