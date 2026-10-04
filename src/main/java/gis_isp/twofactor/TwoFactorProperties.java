package gis_isp.twofactor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.twofactor")
@Validated
public record TwoFactorProperties(

        @NotBlank String issuer,
        @NotBlank String encryptionKey,
        @Min(1) int backupCodesCount

) {}