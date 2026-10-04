package gis_isp.role;

import gis_isp.role.dto.CreateRoleRequest;
import gis_isp.role.dto.RoleListResponse;
import gis_isp.role.dto.RoleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

    // Create Role
    RoleResponse createRole(CreateRoleRequest request);

    // Update Role
    RoleResponse updateRole(Long id, CreateRoleRequest request);

    // Delete Role
    void deleteRole(Long id);

    // Get Role by id
    RoleResponse getRoleById (Long id);
    RoleEntity getEntityById (Long id);

    // Get All Roles
    List<RoleResponse> getAllRoles();

    // obtener roles con paginación
    Page<RoleListResponse> getAllRoles(String search, Pageable pageable);

}
