package gis_isp.user;

import gis_isp.exception.ResourceAlreadyExistsException;
import gis_isp.exception.ResourceNotFoundException;
import gis_isp.user.dto.CreateUserRequest;
import gis_isp.user.dto.UpdateUserRequest;
import gis_isp.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createUser(CreateUserRequest userRequest) {
        if (repository.existsByUsername(userRequest.username())) {
            throw new ResourceAlreadyExistsException("El nombre de usuario '" + userRequest.username() + "' ya está registrado");
        }

        if (repository.existsByEmail(userRequest.email())) {
            throw new ResourceAlreadyExistsException("El email '" + userRequest.email() + "' ya está registrado");
        }

        UserEntity user = UserEntity.builder()
                .username(userRequest.username())
                .email(userRequest.email())
                .passwordHash(passwordEncoder.encode(userRequest.password()))
                .build();

        repository.save(user);
    }

    @Override
    public void updateUser(UUID id, UpdateUserRequest userRequest) {
        UserEntity user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        user.setUsername(userRequest.username());
        user.setEmail(userRequest.email());

        repository.save(user);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        UserEntity user =  repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return repository.findAll()
                .stream()
                .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getEmail()))
                .toList();
    }

    @Override
    public void deleteUser(UUID id) {
        if(!repository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        repository.deleteById(id);
    }
}