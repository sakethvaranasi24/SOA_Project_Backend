package com.klu.microservices.controller;

import com.klu.microservices.dto.AuthRequest;
import com.klu.microservices.dto.AuthResponse;
import com.klu.microservices.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        String role = (request.getRole() != null && !request.getRole().isEmpty())
                ? request.getRole().toUpperCase()
                : "ROLE_CANDIDATE";

        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        String token = jwtUtil.generateToken(request.getUsername(), role);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(request.getUsername())
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
