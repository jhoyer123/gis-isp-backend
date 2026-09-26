package gis_isp.permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    // JPQL Get permissions by role id
    @Query("SELECT p.code FROM PermissionEntity p JOIN p.roles r WHERE r.id = :roleId")
    List<String> findPermissionsByRoleId(@Param("roleId") Long roleId);

}
