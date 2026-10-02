package gis_isp.notification.port;

import gis_isp.notification.model.EmailMessage;

// Contract for sending emails; implement it once per provider.
public interface EmailSender {

    // Sends the message or throws EmailSendException on failure.
    void send(EmailMessage message);
}
