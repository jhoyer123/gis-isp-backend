package gis_isp.verification.change;

import gis_isp.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailChangeRequestRepository extends JpaRepository<EmailChangeRequestEntity, UUID> {

    Optional<EmailChangeRequestEntity> findByTokenHash(String tokenHash);

    Optional<EmailChangeRequestEntity> findByTokenHashAndUsedFalseAndExpiresAtAfter(String tokenHash, OffsetDateTime now);

    @Modifying
    @Query("UPDATE EmailChangeRequestEntity e SET e.used = true WHERE e.user = :user AND e.used = false")
    void invalidateAllPendingForUser(@Param("user") UserEntity user);

    void deleteByExpiresAtBefore(OffsetDateTime now);

}
