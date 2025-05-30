package code81.Library_Management_System_Challenge.web.controller;

import code81.Library_Management_System_Challenge.application.service.UserActivityService;
import code81.Library_Management_System_Challenge.application.service.UserService;
import code81.Library_Management_System_Challenge.domain.model.Role;
import code81.Library_Management_System_Challenge.domain.model.User;
import code81.Library_Management_System_Challenge.domain.model.UserActivity;
import code81.Library_Management_System_Challenge.web.dto.UserUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserActivityService userActivityService ;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/activity")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UserActivity>> getAllLogActivity() {
        List<UserActivity> users = userActivityService.getAllActivities();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/activity/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UserActivity>> getAllLogActivity(@PathVariable Long id) {
        List<UserActivity> users = userActivityService.getUserActivities(id);
        return ResponseEntity.ok(users);
    }


}
