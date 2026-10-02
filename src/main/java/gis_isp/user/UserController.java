package gis_isp.user;

import gis_isp.person.dto.UpdateMyPersonProfileRequest;
import gis_isp.person.dto.UpdatePersonProfileRequest;
import gis_isp.security.cookie.AuthCookies;
import gis_isp.user.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthCookies authCookies;

    // Create User
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@Valid @RequestBody CreateUserRequest user) {
        userService.createUser(user);
    }

    // Update data Person profile Admin
    @PutMapping("/me/profile/person")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePersonProfileAdmin(@AuthenticationPrincipal UUID userId, @Valid @RequestBody UpdatePersonProfileRequest request) {
        userService.updatePersonProfileAdmin(userId, request);
    }

    // Update data Person profile User
    @PatchMapping("/me/profile/person/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePersonProfileUser(@PathVariable UUID id, @Valid @RequestBody UpdateMyPersonProfileRequest request) {
        userService.updatePersonProfileUser(id, request);
    }

    // Update user - person
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateUser(@PathVariable UUID id, @Valid @RequestBody UserAdminUpdate user) {
        userService.updateUser(id, user);
    }

    // Get User By id
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable UUID id) {
        UserDetailResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    // update password profile
    @PatchMapping("/me/change-password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal UUID userId,
                                               @Valid @RequestBody ChangePasswordRequest request,
                                               HttpServletResponse response) {
        userService.updatePassword(userId, request);
        authCookies.clear(response);
        return ResponseEntity.noContent().build();
    }

    // update user data account profile
    @PatchMapping(value = "/me/profile/account", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserProfile(@AuthenticationPrincipal UUID userId,
                                  @Valid @ModelAttribute UpdateMyUserProfileRequest request) {
        userService.updateDataUserProfile(userId, request);
    }

    //------- aun esta sin usarse

    // Delete User
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }

    // Get All Users
    @GetMapping
    public ResponseEntity<List<UserListItemResponse>> getAllUsers() {
        List<UserListItemResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

}
