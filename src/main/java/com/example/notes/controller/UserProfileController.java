package com.example.notes.controller;

import com.example.notes.model.User;
import com.example.notes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestParam("username") String username) {
        User user = userRepository.findByUsername(username);
        if(user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        // Hide password in response
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody User updatedUser) {
        User user = userRepository.findByUsername(updatedUser.getUsername());
        if(user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        // Update only if provided
        if(updatedUser.getFullName() != null) user.setFullName(updatedUser.getFullName());
        if(updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
        if(updatedUser.getPhoneNumber() != null) user.setPhoneNumber(updatedUser.getPhoneNumber());
        if(updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(updatedUser.getPassword());
        }
        userRepository.save(user);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
}
