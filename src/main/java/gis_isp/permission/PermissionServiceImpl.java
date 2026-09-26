package gis_isp.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    // Get permissions by role id
    @Override
    public List<String> getPermissionsByRoleId(Long roleId) {
        return permissionRepository.findPermissionsByRoleId(roleId);
    }

    // Get all permissions
    @Override
    public List<PermissionEntity> getAllPermissions() {
        return permissionRepository.findAll();
    }

}