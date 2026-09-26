package gis_isp.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    // Already exists role?
    boolean existsByName(String role);

}
