package gis_isp.auth.dto;

public record LoginOutcome(LoginResult session, String challengeToken, long challengeExpiresIn) {
    public boolean twoFactorRequired() {
        return challengeToken != null;
    }

    public static LoginOutcome ok(LoginResult s) {
        return new LoginOutcome(s, null, 0);
    }

    public static LoginOutcome challenge(String t, long secs) {
        return new LoginOutcome(null, t, secs);
    }
}
