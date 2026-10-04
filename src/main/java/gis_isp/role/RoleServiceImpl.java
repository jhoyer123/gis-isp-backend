package gis_isp.role;

import gis_isp.common.exception.ResourceAlreadyExistsException;
import gis_isp.common.exception.ResourceNotFoundException;
import gis_isp.role.dto.CreateRoleRequest;
import gis_isp.role.dto.RoleListResponse;
import gis_isp.role.dto.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    // Create Role
    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        if(roleRepository.existsByName(request.name()))
            throw new ResourceAlreadyExistsException("El rol ya existe");

        RoleEntity role = RoleEntity.builder()
                .name(request.name())
                .description(request.description())
                .build();

        RoleEntity roleSaved = roleRepository.save(role);

        return new RoleResponse(
                roleSaved.getId(),
                roleSaved.getName(),
                roleSaved.getDescription()
        );
    }

    // Update Role
    @Override
    public RoleResponse updateRole(Long id, CreateRoleRequest request) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));

        role.setName(request.name());
        role.setDescription(request.description());

        RoleEntity roleUpdated = roleRepository.save(role);

        return new RoleResponse(
                roleUpdated.getId(),
                roleUpdated.getName(),
                roleUpdated.getDescription()
        );
    }

    // Delete Role
    @Override
    public void deleteRole(Long id) {

        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));

        roleRepository.delete(role);
    }

    // Get Role by Id
    @Override
    public RoleResponse getRoleById (Long id) {

        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription()
        );
    }

    // Get Entity Role
    @Override
    public RoleEntity getEntityById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));
    }

    // Get All  Roles ********** no se usa
    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(role -> new RoleResponse(
                        role.getId(),
                        role.getName(),
                        role.getDescription()
                ))
                .toList();
    }

    // obtener lista de roles con paginación
    @Transactional(readOnly = true)
    public Page<RoleListResponse> getAllRoles(String search, Pageable pageable) {
        return roleRepository.findAllWithCounts(search == null ? "" : search.trim(), pageable);
    }
}
