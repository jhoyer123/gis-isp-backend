package gis_isp.notification.template;

import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

// Renders html/{name} and text/{name} templates with the given variables.
@Component
public class EmailTemplateRenderer {

    private final TemplateEngine engine;

    public EmailTemplateRenderer(@Qualifier("emailTemplateEngine") TemplateEngine engine) {
        this.engine = engine;
    }

    // Returns both HTML and plain-text versions of the same template name.
    public RenderedEmail render(String templateName, Map<String, Object> variables) {
        Context ctx = new Context();
        ctx.setVariables(variables);
        return new RenderedEmail(
                engine.process("html/" + templateName, ctx),
                engine.process("text/" + templateName, ctx));
    }
}
