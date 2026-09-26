package gis_isp.user;

import gis_isp.user.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // Create User
    UserResponse createUser(CreateUserRequest userRequest);

    // Update User
    UserResponse updateUser(UUID id, UserAdminUpdate userRequest);

    // Get user By Id
    UserAdminDetailResponse getUserById(UUID id);

    // Get user me
    UserMeResponse getUserMe(UUID id);

    // Get All users
    List<UserListItemResponse> getAllUsers();

    // Delete user
    void deleteUser(UUID id);

}
