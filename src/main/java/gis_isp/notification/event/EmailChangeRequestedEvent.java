package gis_isp.notification.event;

import java.time.Duration;

// Published when a user asks to change email; the mail goes to the NEW address.
public record EmailChangeRequestedEvent(String newEmail, String firstName, String token, Duration validFor) {

    @Override
    public String toString() {
        return "EmailChangeRequestedEvent[newEmail=" + newEmail + "]"; // token intentionally hidden
    }
}
