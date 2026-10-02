package gis_isp.user;

import gis_isp.person.dto.UpdateMyPersonProfileRequest;
import gis_isp.person.dto.UpdatePersonProfileRequest;
import gis_isp.user.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // Create User
    void createUser(CreateUserRequest userRequest);

    // update persone profile admin
    void updatePersonProfileAdmin(UUID id, UpdatePersonProfileRequest request);

    // update persone profile user
    void updatePersonProfileUser(UUID id, UpdateMyPersonProfileRequest request);

    // Update User
    void updateUser(UUID id, UserAdminUpdate userRequest);

    // Get user By Id
    UserDetailResponse getUserById(UUID id);

    // Get user me
    UserMeResponse getUserMe(UUID id);

    // update password profile
    void updatePassword(UUID userId, ChangePasswordRequest request);

    // Update user profile data
    void updateDataUserProfile(
            UUID id,
            UpdateMyUserProfileRequest request
    );

    // Get All users
    List<UserListItemResponse> getAllUsers();

    // Delete user
    void deleteUser(UUID id);

}
