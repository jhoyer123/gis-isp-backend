package gis_isp.user;

import gis_isp.common.exception.*;
import gis_isp.permission.PermissionService;
import gis_isp.person.PersonEntity;
import gis_isp.person.PersonService;
import gis_isp.person.dto.CreatePersonRequest;
import gis_isp.person.dto.UpdateMyPersonProfileRequest;
import gis_isp.person.dto.UpdatePersonProfileRequest;
import gis_isp.refresh.RefreshTokenService;
import gis_isp.role.RoleEntity;
import gis_isp.role.RoleService;
import gis_isp.storage.SupabaseAvatarService;
import gis_isp.user.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PersonService personService;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final SupabaseAvatarService storageService;

    // Create user
    @Override
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(CreateUserRequest request) {

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

        userRepository.save(user);
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(UUID id, UserAdminUpdate request) {

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
        personService.updatePerson(
                user.getPerson().getId(),
                new CreatePersonRequest(
                        request.firstName(),
                        request.lastName(),
                        request.phone(),
                        request.ci()
                )
        );

        userRepository.save(user);
    }

    // Get User By id
    @Override
    public UserDetailResponse getUserById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return UserDetailResponse.from(user);
    }

    // Get user me
    @Override
    public UserMeResponse getUserMe(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        List<String> codes = permissionService.getPermissionsByRoleId(user.getRole().getId());
        return UserMeResponse.from(user, codes);
    }

    // update person profile admin
    @Override
    public void updatePersonProfileAdmin(UUID id, UpdatePersonProfileRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        personService.updatePerson(
                user.getPerson().getId(),
                new CreatePersonRequest(
                        request.firstName(),
                        request.lastName(),
                        request.phone(),
                        request.ci()
                )
        );
    }

    // update person profile user
    @Override
    @Transactional
    public void updatePersonProfileUser(UUID id, UpdateMyPersonProfileRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        personService.updatePersonUser(user.getPerson().getId(), request.phone());
    }

    // update password
    @Override
    @Transactional
    public void updatePassword(UUID id, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException("La nueva contraseña debe ser distinta a la actual");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustSetPassword(false);
        user.setFailedAttempts(0);

        refreshTokenService.revokeAllForUser(id);
    }

    // update data user profile - avatar nad username
    @Override
    @Transactional
    public void updateDataUserProfile(
            UUID id,
            UpdateMyUserProfileRequest request
    ) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );

        if (request.username() != null) {
            user.setUsername(request.username());
        }

        if (request.avatar() != null && !request.avatar().isEmpty()) {
            String oldAvatarPath = user.getAvatarUrl();
            try {
                String newAvatarPath = storageService.uploadAvatar(request.avatar(), oldAvatarPath);
                user.setAvatarUrl(newAvatarPath);
            } catch (IOException e) {
                throw new FileUploadException("Ocurrió un error al procesar el archivo del avatar en el servidor.");
            }
        }

        userRepository.save(user);
    }

    // *** aun sin uso ******

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

}