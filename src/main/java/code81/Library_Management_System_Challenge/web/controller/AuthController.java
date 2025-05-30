package code81.Library_Management_System_Challenge.web.controller;

import code81.Library_Management_System_Challenge.application.service.UserService;
import code81.Library_Management_System_Challenge.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo() {
        User currentUser = userService.getCurrentUser();

        Map<String, Object> response = new HashMap<>();
        response.put("id", currentUser.getId());
        response.put("username", currentUser.getUsername());
        response.put("email", currentUser.getEmail());
        response.put("firstName", currentUser.getFirstName());
        response.put("lastName", currentUser.getLastName());
        response.put("role", currentUser.getRole());
        response.put("active", currentUser.isActive());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login() {
        User currentUser = userService.getCurrentUser();

        // Update last login
        userService.updateLastLogin(currentUser.getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("username", currentUser.getUsername());

        return ResponseEntity.ok(response);
    }
}
