package gis_isp.refresh;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    // Find User by tokenhash
    @EntityGraph(attributePaths = "user")
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    // Revoke token
    @Modifying
    @Query("""
        update RefreshTokenEntity t
           set t.revoked = true, t.revokedAt = :now
         where t.id = :id and t.revoked = false
        """)
    int revokeIfActive(@Param("id") UUID id, @Param("now") OffsetDateTime now);

    // Revoke all token
    @Modifying
    @Query("""
        update RefreshTokenEntity t
           set t.revoked = true, t.revokedAt = :now
         where t.user.id = :userId and t.revoked = false
        """)
    int revokeAllByUserId(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);

    // Delete all toker expired
    @Modifying
    @Query("delete from RefreshTokenEntity t where t.expiresAt < :now")
    int deleteExpired(@Param("now") OffsetDateTime now);
}
