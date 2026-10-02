package gis_isp.notification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Registers MailProperties as a bean so it can be injected anywhere.
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {
}