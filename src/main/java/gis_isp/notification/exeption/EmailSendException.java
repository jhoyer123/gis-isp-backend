package gis_isp.notification.exeption;

// Unchecked exception thrown when any provider fails to deliver an email.
public class EmailSendException extends RuntimeException {

    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}