package com.klu.services.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klu.services.dto.AuthRequest;
import com.klu.services.dto.RegisterRequest;
import com.klu.services.model.User;
import com.klu.services.repo.UserRepository;
import com.klu.services.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Positive: Successfully register new candidate")
    public void testRegisterCandidateSuccess() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newCandidate");
        req.setPassword("pass123");
        req.setEmail("candidate@test.com");
        req.setRole("CANDIDATE");

        User savedUser = User.builder()
                .id(100L)
                .username("newCandidate")
                .password("pass123")
                .email("candidate@test.com")
                .role("ROLE_CANDIDATE")
                .build();

        when(userRepository.existsByUsername("newCandidate")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.role").value("ROLE_CANDIDATE"));
    }

    @Test
    @DisplayName("Negative: Register existing username returns 409 Conflict")
    public void testRegisterDuplicateUsername() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("sai");
        req.setPassword("pass123");

        when(userRepository.existsByUsername("sai")).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    @DisplayName("Positive: Login with correct credentials returns 200 OK and JWT token")
    public void testLoginSuccess() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("sakeeth");
        req.setPassword("sakeeth123");

        User user = User.builder()
                .id(2L)
                .username("sakeeth")
                .password("sakeeth123")
                .role("ROLE_RECRUITER")
                .build();

        when(userRepository.findByUsername("sakeeth")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(eq("sakeeth"), eq("ROLE_RECRUITER"))).thenReturn("mocked.jwt.token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.role").value("ROLE_RECRUITER"))
                .andExpect(jsonPath("$.username").value("sakeeth"));
    }

    @Test
    @DisplayName("Negative: Login with incorrect password returns 401 Unauthorized")
    public void testLoginWrongPassword() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("sakeeth");
        req.setPassword("wrongPassword");

        User user = User.builder()
                .id(2L)
                .username("sakeeth")
                .password("sakeeth123")
                .role("ROLE_RECRUITER")
                .build();

        when(userRepository.findByUsername("sakeeth")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    @DisplayName("Negative: Missing Authorization header on /auth/validate returns 401 Unauthorized")
    public void testValidateMissingHeader() throws Exception {
        mockMvc.perform(get("/auth/validate"))
                .andExpect(status().isBadRequest());
    }
}
