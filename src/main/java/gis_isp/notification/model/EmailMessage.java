package gis_isp.notification.model;

// Provider-agnostic email data: who receives it, subject, and HTML/plain bodies.
public record EmailMessage(
        String to,
        String subject,
        String htmlBody,
        String textBody
) {
    public EmailMessage {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient is required");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (htmlBody == null || htmlBody.isBlank()) {
            throw new IllegalArgumentException("HTML body is required");
        }
    }
}
