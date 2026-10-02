package gis_isp.notification.config;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

// Dedicated Thymeleaf engine for emails (HTML + plain text), using SpringEL.
@Configuration
public class EmailTemplateConfig {

    @Bean("emailTemplateEngine")
    public SpringTemplateEngine emailTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(resolver("html/*", ".html", TemplateMode.HTML, 1));
        engine.addTemplateResolver(resolver("text/*", ".txt", TemplateMode.TEXT, 2));
        return engine;
    }

    // Builds a classpath resolver: templates/email/{name}{suffix}.
    private ClassLoaderTemplateResolver resolver(String pattern, String suffix,
                                                 TemplateMode mode, int order) {
        ClassLoaderTemplateResolver r = new ClassLoaderTemplateResolver();
        r.setPrefix("templates/email/");
        r.setSuffix(suffix);
        r.setTemplateMode(mode);
        r.setCharacterEncoding(StandardCharsets.UTF_8.name());
        r.setResolvablePatterns(Set.of(pattern));
        r.setCheckExistence(true);
        r.setCacheable(true);
        r.setOrder(order);
        return r;
    }
}