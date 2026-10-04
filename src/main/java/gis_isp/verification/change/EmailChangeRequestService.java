package gis_isp.verification.change;

import gis_isp.common.exception.InvalidCurrentPasswordException;
import gis_isp.common.exception.ResourceAlreadyExistsException;
import gis_isp.common.exception.ResourceNotFoundException;
import gis_isp.notification.event.EmailChangeRequestedEvent;
import gis_isp.user.UserEntity;
import gis_isp.user.UserRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

import gis_isp.verification.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// Handles the two-step email change: request (sends link to new address) and confirm.
@Service
@RequiredArgsConstructor
public class EmailChangeRequestService {

    private static final Duration VALIDITY = Duration.ofHours(24);

    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final UserRepository userRepository;
    private final TokenGenerator tokenGenerator;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;

    // Validates the request, stores a hashed token and publishes the event that sends the email.
    @Transactional
    public void requestChange(UUID userId, String newEmailInput, String currentPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException("Contraseña actual incorrecta");
        }

        String newEmail = normalize(newEmailInput);
        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            throw new ResourceAlreadyExistsException("El nuevo correo es igual al actual");
        }

        if (userRepository.existsByEmailIgnoreCase((newEmail))) {
            throw new ResourceAlreadyExistsException("Ese correo ya está en uso");
        }

        emailChangeRequestRepository.invalidateAllPendingForUser(user);

        String rawToken = tokenGenerator.generate();
        emailChangeRequestRepository.save(EmailChangeRequestEntity.builder()
                .user(user)
                .newEmail(newEmail)
                .tokenHash(tokenGenerator.hash(rawToken))
                .expiresAt(OffsetDateTime.now().plus(VALIDITY))
                .used(false)
                .build());

        // Sent after commit, asynchronously, to the NEW address.
        events.publishEvent(new EmailChangeRequestedEvent(
                newEmail, user.getPerson().getFirstName(), rawToken, VALIDITY));
    }

    // Applies the change if the token is valid, unused and not expired; re-checks email availability.
    @Transactional
    public void confirm(String rawToken) {
        EmailChangeRequestEntity request = emailChangeRequestRepository
                .findByTokenHashAndUsedFalseAndExpiresAtAfter(tokenGenerator.hash(rawToken), OffsetDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Token inválido, expirado o ya utilizado"));

        if (userRepository.existsByEmailIgnoreCase(request.getNewEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está en uso");
        }

        request.setUsed(true);
        UserEntity user = request.getUser();
        user.setEmail(request.getNewEmail());
        user.setEmailVerified(true);
        // Managed entities: JPA flushes both changes on commit.
    }

    // Trims and lowercases an email so comparisons and uniqueness are consistent.
    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}