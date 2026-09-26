package gis_isp.user;

import gis_isp.common.exception.ResourceAlreadyExistsException;
import gis_isp.common.exception.ResourceNotFoundException;
import gis_isp.permission.PermissionService;
import gis_isp.person.PersonEntity;
import gis_isp.person.PersonService;
import gis_isp.person.dto.CreatePersonRequest;
import gis_isp.role.RoleEntity;
import gis_isp.role.RoleService;
import gis_isp.user.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PersonService personService;
    private final PermissionService permissionService;

    // Create user
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        // Validation user
        if (userRepository.existsByEmail(request.email()))
            throw new ResourceAlreadyExistsException("El email ya está registrado");


        // Validation role
        RoleEntity role = roleService.getEntityById(request.roleId());

        // Create person
        PersonEntity personSaved = personService.createPerson(
                new CreatePersonRequest(
                        request.firstName(),
                        request.lastName(),
                        request.phone(),
                        request.ci()
                )
        );

        // Create user
        UserEntity user = UserEntity.builder()
                .email(request.email())
                .person(personSaved)
                .role(role)
                .user(userRepository.getReferenceById(getCurrentUserId()))
                .build();

        UserEntity userSaved = userRepository.save(user);

        return UserResponse.from(userSaved, personSaved);
    }

    // Get id user auth
    private UUID getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null)
            throw new IllegalStateException("Usuario no autenticado");


        return (UUID) authentication.getPrincipal();
    }

    // Update User
    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserAdminUpdate request) {

        // validation user
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validation role
        RoleEntity role = roleService.getEntityById(request.roleId());

        // Data user
        user.setUsername(request.username());
        user.setUsername(request.username());
        user.setRole(role);

        // Data person
        PersonEntity personUpdated = personService.updatePerson(
                user.getPerson().getId(),
                new CreatePersonRequest(
                        request.firstName(),
                        request.lastName(),
                        request.phone(),
                        request.ci()
                )
        );

        UserEntity userUpdated = userRepository.save(user);

        return UserResponse.from(userUpdated, personUpdated);
    }

    // Delete User
    @Override
    @Transactional
    public void deleteUser(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // pendiente verificar si realizo acciones entonces hacer softdelete o delete
        user.setStatus(UserStatus.INACTIVE); // o DELETED si agregas ese enum value
        userRepository.save(user);

        // opcional, pero recomendado:
        // - revocar todos sus refresh_tokens (que no pueda seguir logueado)
        // - registrar en audit_logs: action = "USER_DEACTIVATED"
    }

    // Get All Users
    @Override
    public List<UserListItemResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserListItemResponse::from)
                .toList();
    }

    // Get User By id
    @Override
    public UserAdminDetailResponse getUserById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return UserAdminDetailResponse.from(user);
    }

    // Get user me
    @Override
    public UserMeResponse getUserMe(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        List<String> codes = permissionService.getPermissionsByRoleId(user.getRole().getId());
        return UserMeResponse.from(user, codes);
    }
}