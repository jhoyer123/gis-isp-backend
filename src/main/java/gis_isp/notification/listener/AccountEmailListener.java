package gis_isp.notification.listener;

import gis_isp.notification.event.EmailChangeRequestedEvent;
import gis_isp.notification.event.PasswordResetRequestedEvent;
import gis_isp.notification.event.UserInvitedEvent;
import gis_isp.notification.exeption.EmailSendException;
import gis_isp.notification.service.AccountEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Sends emails after the DB transaction commits, on the email thread pool.
// Failures are logged only: the user can always request a new link.
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEmailListener {

    private final AccountEmailService emailService;

    // Reacts to a new user being created by an admin.
    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserInvited(UserInvitedEvent e) {
        run(() -> emailService.sendInvitation(e.email(), e.firstName(), e.token(), e.validFor()), e);
    }

    // Reacts to an email change request (mail goes to the new address).
    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEmailChangeRequested(EmailChangeRequestedEvent e) {
        run(() -> emailService.sendEmailChange(e.newEmail(), e.firstName(), e.token(), e.validFor()), e);
    }

    // Reacts to a forgot-password request.
    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPasswordResetRequested(PasswordResetRequestedEvent e) {
        run(() -> emailService.sendPasswordReset(e.email(), e.firstName(), e.token(), e.validFor()), e);
    }

    // Runs the send and swallows delivery errors so they never break the caller's flow.
    private void run(Runnable action, Object event) {
        try {
            action.run();
        } catch (EmailSendException ex) {
            log.error("Email delivery failed for {}", event, ex);
        }
    }
}
