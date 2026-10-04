package gis_isp.common.audit;

public enum AuditAction {

    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,

    PASSWORD_CHANGED,
    PASSWORD_RESET_REQUESTED,

    USER_CREATED,
    USER_UPDATED,

    TWO_FACTOR_ENABLED,
    TWO_FACTOR_DISABLED,

}
