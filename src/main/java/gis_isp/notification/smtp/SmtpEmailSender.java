package gis_isp.notification.smtp;

import gis_isp.notification.config.MailProperties;
import gis_isp.notification.exeption.EmailSendException;
import gis_isp.notification.model.EmailMessage;
import gis_isp.notification.port.EmailSender;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

// EmailSender adapter over SMTP (Resend today); swap this class to change provider.
@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final MailProperties props;

    // Builds a MIME message (HTML + optional plain text) and sends it via SMTP.
    @Override
    public void send(EmailMessage message) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(props.fromAddress(), props.fromName());
            helper.setTo(message.to());
            helper.setSubject(message.subject());

            if (message.textBody() != null && !message.textBody().isBlank()) {
                helper.setText(message.textBody(), message.htmlBody());
            } else {
                helper.setText(message.htmlBody(), true);
            }

            mailSender.send(mime);
            log.info("Email sent: subject='{}'", message.subject());
        } catch (Exception e) {
            // Never log the recipient's token/link; only generic context.
            throw new EmailSendException("Failed to send email: " + message.subject(), e);
        }
    }
}