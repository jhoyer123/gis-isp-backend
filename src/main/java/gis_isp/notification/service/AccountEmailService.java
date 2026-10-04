package gis_isp.notification.service;

import gis_isp.notification.config.MailProperties;
import gis_isp.notification.model.EmailMessage;
import gis_isp.notification.port.EmailSender;
import gis_isp.notification.template.EmailTemplateRenderer;
import gis_isp.notification.template.RenderedEmail;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

// Builds account-related emails (invite, email change, password reset) and sends them.
@Service
@RequiredArgsConstructor
public class AccountEmailService {

    private static final String TEMPLATE = "action";

    private final EmailSender emailSender;
    private final EmailTemplateRenderer renderer;
    private final MailProperties props;

    // Invitation email: lets a newly created user set a password and activate the account.
    public void sendInvitation(String to, String firstName, String token, Duration validFor) {
        send(to, "Activa tu cuenta en Veranet GIS", "Activa tu cuenta", firstName,
                "Un administrador creó tu cuenta. Define tu contraseña para activarla.",
                "Activar cuenta", "/accept-invite", token, validFor);
    }

    // Confirmation email sent to the NEW address when a user changes their email.
    public void sendEmailChange(String newEmail, String firstName, String token, Duration validFor) {
        send(newEmail, "Confirma tu nuevo correo", "Confirma tu nuevo correo", firstName,
                "Recibimos una solicitud para usar este correo en tu cuenta. Confírmalo para completar el cambio.",
                "Confirmar correo", "/verify-email", token, validFor);
    }

    // Password reset email with a short-lived link.
    public void sendPasswordReset(String to, String firstName, String token, Duration validFor) {
        send(to, "Restablecer contraseña", "Restablecer contraseña", firstName,
                "Recibimos una solicitud para restablecer tu contraseña.",
                "Restablecer contraseña", "/reset-password", token, validFor);
    }

    // Renders the shared "action" template and delegates delivery to the EmailSender port.
    private void send(String to, String subject, String title, String firstName, String message,
                      String buttonLabel, String path, String token, Duration validFor) {
        Map<String, Object> vars = Map.of(
                "title", title,
                "name", firstName,
                "message", message,
                "buttonLabel", buttonLabel,
                "actionUrl", buildUrl(path, token),
                "expiresIn", humanize(validFor));

        RenderedEmail body = renderer.render(TEMPLATE, vars);
        emailSender.send(new EmailMessage(to, subject, body.html(), body.text()));
    }

    // Builds {frontendUrl}{path}?token=... with proper URL encoding.
    private String buildUrl(String path, String token) {
        return UriComponentsBuilder.fromUriString(props.frontendUrl())
                .path(path)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();
    }

    // Converts a Duration to a Spanish label like "48 horas" or "30 minutos".
    private String humanize(Duration d) {
        long hours = d.toHours();
        if (hours >= 1) {
            return hours + (hours == 1 ? " hora" : " horas");
        }
        long minutes = Math.max(1, d.toMinutes());
        return minutes + (minutes == 1 ? " minuto" : " minutos");
    }

    // Informational email with no action link (security notices).
    public void sendSecurityNotice(String to, String firstName, String subject, String message) {
        Map<String, Object> vars = Map.of("title", subject, "name", firstName, "message", message);
        RenderedEmail body = renderer.render("notice", vars);
        emailSender.send(new EmailMessage(to, subject, body.html(), body.text()));
    }
}
