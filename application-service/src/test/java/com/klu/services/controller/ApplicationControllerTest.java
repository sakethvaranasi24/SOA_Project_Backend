package com.klu.services.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klu.services.dto.ApplicationRequestDTO;
import com.klu.services.dto.StatusUpdateDTO;
import com.klu.services.exception.BadRequestException;
import com.klu.services.exception.DuplicateApplicationException;
import com.klu.services.exception.GlobalExceptionHandler;
import com.klu.services.exception.ResourceNotFoundException;
import com.klu.services.model.Application;
import com.klu.services.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationController applicationController;

    private Application sampleApp;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(applicationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        sampleApp = new Application(1L, 10L, 100L, "https://storage.talentsync.com/resumes/c100.pdf", "APPLIED");
    }

    @Test
    @DisplayName("POST /applications - Should return 201 Created with valid payload")
    void testApplyForJob_Valid() throws Exception {
        ApplicationRequestDTO request = new ApplicationRequestDTO(10L, 100L, "https://storage.talentsync.com/resumes/c100.pdf");
        when(applicationService.applyForJob(any(Application.class))).thenReturn(sampleApp);

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId", is(1)))
                .andExpect(jsonPath("$.jobId", is(10)))
                .andExpect(jsonPath("$.candidateId", is(100)))
                .andExpect(jsonPath("$.status", is("APPLIED")));
    }

    @Test
    @DisplayName("POST /applications - Should return 400 Bad Request when required fields are null or blank")
    void testApplyForJob_ValidationFailure() throws Exception {
        ApplicationRequestDTO request = new ApplicationRequestDTO(null, null, "");

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Validation failed")))
                .andExpect(jsonPath("$.details", hasSize(3)));
    }

    @Test
    @DisplayName("POST /applications - Should return 409 Conflict when duplicate application is submitted")
    void testApplyForJob_Duplicate() throws Exception {
        ApplicationRequestDTO request = new ApplicationRequestDTO(10L, 100L, "https://storage.talentsync.com/resumes/c100.pdf");
        when(applicationService.applyForJob(any(Application.class)))
                .thenThrow(new DuplicateApplicationException("Candidate 100 has already applied for Job 10"));

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already applied")));
    }

    @Test
    @DisplayName("POST /applications - Should return 400 Bad Request when job is closed")
    void testApplyForJob_ClosedJob() throws Exception {
        ApplicationRequestDTO request = new ApplicationRequestDTO(10L, 100L, "https://storage.talentsync.com/resumes/c100.pdf");
        when(applicationService.applyForJob(any(Application.class)))
                .thenThrow(new BadRequestException("Cannot apply to closed job with ID: 10"));

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("closed job")));
    }

    @Test
    @DisplayName("GET /applications/{id} - Should return 200 OK when application exists")
    void testGetApplicationById_Found() throws Exception {
        when(applicationService.getApplicationOrThrow(1L)).thenReturn(sampleApp);

        mockMvc.perform(get("/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId", is(1)))
                .andExpect(jsonPath("$.jobId", is(10)));
    }

    @Test
    @DisplayName("GET /applications/{id} - Should return 404 Not Found when ID does not exist")
    void testGetApplicationById_NotFound() throws Exception {
        when(applicationService.getApplicationOrThrow(99L))
                .thenThrow(new ResourceNotFoundException("Application not found with ID: 99"));

        mockMvc.perform(get("/applications/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Application not found with ID: 99")));
    }

    @Test
    @DisplayName("GET /applications/candidate/{candidateId} - Should return 200 OK with list")
    void testGetApplicationsByCandidate() throws Exception {
        when(applicationService.getApplicationsByCandidateId(100L)).thenReturn(Collections.singletonList(sampleApp));

        mockMvc.perform(get("/applications/candidate/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].candidateId", is(100)));
    }

    @Test
    @DisplayName("GET /applications/job/{jobId} - Should return 200 OK with list")
    void testGetApplicationsByJob() throws Exception {
        when(applicationService.getApplicationsByJobId(10L)).thenReturn(Collections.singletonList(sampleApp));

        mockMvc.perform(get("/applications/job/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].jobId", is(10)));
    }

    @Test
    @DisplayName("PUT /applications/{id}/status - Should return 200 OK on valid status update")
    void testUpdateApplicationStatus_Success() throws Exception {
        StatusUpdateDTO updateDTO = new StatusUpdateDTO("SHORTLISTED");
        Application updated = new Application(1L, 10L, 100L, "https://storage.talentsync.com/resumes/c100.pdf", "SHORTLISTED");
        when(applicationService.updateApplicationStatus(eq(1L), eq("SHORTLISTED"))).thenReturn(updated);

        mockMvc.perform(put("/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHORTLISTED")));
    }

    @Test
    @DisplayName("PUT /applications/{id}/status - Should return 400 Bad Request on invalid transition")
    void testUpdateApplicationStatus_InvalidTransition() throws Exception {
        StatusUpdateDTO updateDTO = new StatusUpdateDTO("SELECTED");
        when(applicationService.updateApplicationStatus(eq(1L), eq("SELECTED")))
                .thenThrow(new BadRequestException("Invalid status transition from APPLIED to SELECTED"));

        mockMvc.perform(put("/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Invalid status transition")));
    }
}
