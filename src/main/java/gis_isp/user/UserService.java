package gis_isp.user;

import gis_isp.user.dto.CreateUserRequest;
import gis_isp.user.dto.UpdateUserRequest;
import gis_isp.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    void createUser(CreateUserRequest userRequest);
    void updateUser(UUID id, UpdateUserRequest userRequest);
    UserResponse getUserById(UUID id);
    List<UserResponse> getAllUsers();
    void deleteUser(UUID id);

}
