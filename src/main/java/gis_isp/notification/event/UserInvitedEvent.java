package gis_isp.notification.event;

import java.time.Duration;

// Published after an admin creates a user; triggers the invitation email.
public record UserInvitedEvent(String email, String firstName, String token, Duration validFor) {

    @Override
    public String toString() {
        return "UserInvitedEvent[email=" + email + "]"; // token intentionally hidden
    }
}