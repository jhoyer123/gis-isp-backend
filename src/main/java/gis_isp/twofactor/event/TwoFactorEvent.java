package gis_isp.twofactor.event;

import java.util.UUID;

public record TwoFactorEvent(UUID userId, Type type, String ip) {
    public enum Type { ENABLED, DISABLED }
}
