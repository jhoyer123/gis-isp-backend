package gis_isp.notification.event;

import java.time.Duration;

// Published when a user requests a password reset; triggers the reset email.
public record PasswordResetRequestedEvent(String email, String firstName, String token, Duration validFor) {

    @Override
    public String toString() {
        return "PasswordResetRequestedEvent[email=" + email + "]"; // token intentionally hidden
    }
}