package gis_isp.role;

import gis_isp.role.dto.CreateRoleRequest;
import gis_isp.role.dto.RoleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // Create role
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse createRole(@Valid @RequestBody CreateRoleRequest createRoleRequest) {
        return roleService.createRole(createRoleRequest);
    }

    //update role
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RoleResponse updateRole(@PathVariable Long id, @Valid @RequestBody CreateRoleRequest createRoleRequest) {
        return roleService.updateRole(id, createRoleRequest);
    }

    // Delete Role
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
    }

    //Get by id veremos si se usara o no********

    // Get all Roles
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RoleResponse> findAllRoles() {
        return roleService.getAllRoles();
    }

}
