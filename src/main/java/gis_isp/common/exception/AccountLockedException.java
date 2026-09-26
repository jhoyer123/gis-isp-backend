package gis_isp.common.exception;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class AccountLockedException extends RuntimeException {

    private final OffsetDateTime lockedUntil;

    public AccountLockedException(OffsetDateTime lockedUntil) {
        super("Cuenta bloqueada temporalmente");
        this.lockedUntil = lockedUntil;
    }
}