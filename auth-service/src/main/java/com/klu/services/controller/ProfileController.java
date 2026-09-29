package com.klu.services.controller;

import com.klu.services.model.User;
import com.klu.services.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("id", user.getId());
                    profile.put("username", user.getUsername());
                    profile.put("email", user.getEmail());
                    profile.put("role", user.getRole());
                    return ResponseEntity.ok(profile);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}")
    public ResponseEntity<?> updateProfile(@PathVariable String username, @RequestBody Map<String, String> body) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (body.containsKey("email")) {
                        user.setEmail(body.get("email"));
                    }
                    userRepository.save(user);
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Profile updated successfully");
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
