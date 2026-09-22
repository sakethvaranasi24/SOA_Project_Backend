package com.klu.microservices.controller;

import com.klu.microservices.dto.AuthRequest;
import com.klu.microservices.dto.AuthResponse;
import com.klu.microservices.dto.RegisterRequest;
import com.klu.microservices.model.User;
import com.klu.microservices.repository.UserRepository;
import com.klu.microservices.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username already exists"));
        }

        String rawRole = (request.getRole() != null && !request.getRole().trim().isEmpty())
                ? request.getRole().trim().toUpperCase()
                : "ROLE_CANDIDATE";

        String role;
        if (rawRole.contains("HR") || rawRole.contains("RECRUIT") || rawRole.contains("ADMIN")) {
            role = "ROLE_RECRUITER";
        } else {
            role = "ROLE_CANDIDATE";
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("userId", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("role", savedUser.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        String role = user.getRole();
        String token = jwtUtil.generateToken(user.getUsername(), role);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(role)
                .message("Authentication successful. JWT Token generated.")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                result.put("valid", false);
                result.put("error", "Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.validateAndExtractClaims(token);

            result.put("valid", true);
            result.put("username", claims.getSubject());
            result.put("role", claims.get("role"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
    }
}
