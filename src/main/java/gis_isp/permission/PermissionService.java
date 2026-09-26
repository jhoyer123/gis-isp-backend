package gis_isp.permission;

import java.util.List;

public interface PermissionService {

    // Get permissions by role id
    List<String> getPermissionsByRoleId(Long roleId);

    // Get all permissions
    List<PermissionEntity> getAllPermissions();

}
