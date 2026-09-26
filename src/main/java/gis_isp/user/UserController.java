package gis_isp.user;

import gis_isp.user.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    // Create User
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@Valid @RequestBody CreateUserRequest user) {
        service.createUser(user);
    }

    // Update User
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateUser(@PathVariable UUID id,@Valid @RequestBody UserAdminUpdate user) {
        service.updateUser(id, user);
    }

    // Delete User
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        service.deleteUser(id);
    }

    // Get All Users
    @GetMapping
    public ResponseEntity<List<UserListItemResponse>> getAllUsers() {
        List<UserListItemResponse> users = service.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get User By id
    @GetMapping("/{id}")
    public ResponseEntity<UserAdminDetailResponse> getUserById(@PathVariable UUID id) {
        UserAdminDetailResponse response = service.getUserById(id);
        return ResponseEntity.ok(response);
    }

}
