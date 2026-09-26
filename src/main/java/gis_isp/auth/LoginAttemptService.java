package gis_isp.auth;

import gis_isp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailedAttempt(UUID userId, int maxAttempts, java.time.Duration lockDuration) {
        userRepository.registerFailedAttempt(
                userId, maxAttempts, OffsetDateTime.now().plus(lockDuration));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetAttempts(UUID userId) {
        userRepository.resetLoginAttempts(userId);
    }
}