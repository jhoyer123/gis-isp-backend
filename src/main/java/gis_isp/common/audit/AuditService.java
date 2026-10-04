package gis_isp.common.audit;

import gis_isp.common.exception.ResourceNotFoundException;
import gis_isp.user.UserEntity;
import gis_isp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(
            UUID actorId,
            AuditAction action,
            String entity,
            String entityId,
            String ipAddress,
            Map<String, Object> details
    ) {

        UserEntity actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado"));

        AuditLogEntity auditLog = AuditLogEntity.builder()
                .user(actor)
                .action(action.name())
                .entity(entity)
                .entityId(entityId)
                .ipAddress(ipAddress)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }
}