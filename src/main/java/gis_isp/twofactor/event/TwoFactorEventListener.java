package gis_isp.twofactor.event;

import gis_isp.common.audit.AuditAction;
import gis_isp.common.audit.AuditService;
import gis_isp.notification.service.AccountEmailService;
import gis_isp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TwoFactorEventListener {

    private final AuditService audit;
    private final UserRepository userRepository;
    private final AccountEmailService mail;

    @EventListener
    @Transactional
    public void on(TwoFactorEvent e) {
        AuditAction action = switch (e.type()) {
            case ENABLED -> AuditAction.TWO_FACTOR_ENABLED;
            case DISABLED -> AuditAction.TWO_FACTOR_DISABLED;
        };
        audit.log(e.userId(), action, "USER", e.userId().toString(), e.ip(), null);

        if (e.type() != TwoFactorEvent.Type.DISABLED) return;

        try {
            userRepository.findById(e.userId()).ifPresent(u -> mail.sendSecurityNotice(
                    u.getEmail(), u.getPerson().getFirstName(),
                    "Cambio de seguridad en tu cuenta",
                    "Se desactivó la verificación en dos pasos en tu cuenta. "
                            + "Si no fuiste tú, contacta al administrador de inmediato."));
        } catch (Exception ex) {
            log.warn("No se pudo enviar el aviso de 2FA a {}", e.userId(), ex);
        }
    }
}