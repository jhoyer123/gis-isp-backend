package gis_isp.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    // Find a user by username
    Optional<UserEntity> findByUsername(String username);

    // Find a user by email
    Optional<UserEntity> findByEmail(String email);

    // Check if a user exists with the given username
    boolean existsByUsername(String username);

    // Check if a user exists with the given email
    boolean existsByEmail(String email);

    // Find a user by ID including their role
    @Query("select u from UserEntity u join fetch u.role where u.id = :id")
    Optional<UserEntity> findByIdWithRole(@Param("id") UUID id);

    // Get the permission codes assigned to a role
    @Query(value = """
        select p.code from role_permissions rp
        join permissions p on p.id = rp.permission_id
        where rp.role_id = :roleId
        """, nativeQuery = true)
    List<String> findPermissionCodesByRoleId(@Param("roleId") Long roleId);

    // Register a failed login attempt
    @Modifying
    @Query("""
    UPDATE UserEntity u SET u.failedAttempts = u.failedAttempts + 1,
        u.isLocked = CASE WHEN u.failedAttempts + 1 >= :max THEN true ELSE u.isLocked END,
        u.lockUntil = CASE WHEN u.failedAttempts + 1 >= :max THEN :lockUntil ELSE u.lockUntil END
    WHERE u.id = :id
    """)
    void registerFailedAttempt(@Param("id") UUID id, @Param("max") int max, @Param("lockUntil") OffsetDateTime lockUntil);

    // Reset failed attempts and unlock the user
    @Modifying
    @Query("UPDATE UserEntity u SET u.failedAttempts = 0, u.isLocked = false, u.lockUntil = null WHERE u.id = :id")
    void resetLoginAttempts(@Param("id") UUID id);

}
