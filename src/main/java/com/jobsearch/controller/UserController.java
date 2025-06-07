package com.jobsearch.controller;

import com.jobsearch.dto.UserDTO;
import com.jobsearch.service.EmailService;
import com.jobsearch.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<UserDTO.UserResponse> createUser(@Valid @RequestBody UserDTO.CreateUserRequest request) {
        UserDTO.UserResponse user = userService.createUser(request);

        try {
            emailService.sendWelcomeEmail(mapToUserEntity(user));
        } catch (Exception e) {
            // Log error but don't fail user creation
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO.UserResponse> getUserById(@PathVariable Long id) {
        UserDTO.UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO.UserResponse> getUserByEmail(@PathVariable String email) {
        UserDTO.UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO.UserResponse>> getAllUsers() {
        List<UserDTO.UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    private com.jobsearch.entity.User mapToUserEntity(UserDTO.UserResponse userResponse) {
        com.jobsearch.entity.User user = new com.jobsearch.entity.User();
        user.setId(userResponse.getId());
        user.setName(userResponse.getName());
        user.setEmail(userResponse.getEmail());
        user.setCreatedAt(userResponse.getCreatedAt());
        user.setIsActive(userResponse.getIsActive());
        return user;
    }
}