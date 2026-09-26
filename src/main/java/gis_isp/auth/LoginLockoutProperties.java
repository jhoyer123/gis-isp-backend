package gis_isp.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.login")
public record LoginLockoutProperties(int maxAttempts, java.time.Duration lockDuration) {}
