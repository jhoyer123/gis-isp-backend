package gis_isp.notification.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// Type-safe holder for app.mail.* settings; fails at startup if any is missing.
@Validated
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        @NotBlank String fromAddress,
        @NotBlank String fromName,
        @NotBlank String frontendUrl
) {}
