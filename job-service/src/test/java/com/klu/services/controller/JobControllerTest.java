package com.klu.services.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klu.services.dto.JobRequestDTO;
import com.klu.services.exception.GlobalExceptionHandler;
import com.klu.services.exception.ResourceNotFoundException;
import com.klu.services.model.Job;
import com.klu.services.service.JobService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /jobs - Should return 201 Created with valid payload")
    void testCreateJob_Valid() throws Exception {
        JobRequestDTO request = new JobRequestDTO("Google", "SWE", "Hyderabad", "Backend dev", "OPEN");
        Job created = new Job(1L, "Google", "SWE", "Hyderabad", "Backend dev", "OPEN");

        when(jobService.createJob(any(Job.class))).thenReturn(created);

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId", is(1)))
                .andExpect(jsonPath("$.companyName", is("Google")))
                .andExpect(jsonPath("$.role", is("SWE")))
                .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    @DisplayName("POST /jobs - Should return 400 Bad Request when required fields are blank")
    void testCreateJob_ValidationFailure() throws Exception {
        JobRequestDTO request = new JobRequestDTO("", "", "", "Backend dev", "OPEN");

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Validation failed")))
                .andExpect(jsonPath("$.details", not(empty())));
    }

    @Test
    @DisplayName("POST /jobs - Should return 400 Bad Request when status pattern is invalid")
    void testCreateJob_InvalidStatusPattern() throws Exception {
        JobRequestDTO request = new JobRequestDTO("Google", "SWE", "Hyderabad", "Backend dev", "INVALID_STATUS");

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", hasItem(containsString("Status must be either OPEN or CLOSED"))));
    }

    @Test
    @DisplayName("GET /jobs - Should return 200 OK with list of jobs")
    void testGetAllJobs() throws Exception {
        Job job1 = new Job(1L, "Google", "SWE", "Hyderabad", "Backend", "OPEN");
        Job job2 = new Job(2L, "Microsoft", "PM", "Bangalore", "Cloud", "OPEN");

        when(jobService.getAllJobs()).thenReturn(Arrays.asList(job1, job2));

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].companyName", is("Google")))
                .andExpect(jsonPath("$[1].companyName", is("Microsoft")));
    }

    @Test
    @DisplayName("GET /jobs/{id} - Should return 200 OK when job exists")
    void testGetJobById_Found() throws Exception {
        Job job = new Job(1L, "Google", "SWE", "Hyderabad", "Backend", "OPEN");
        when(jobService.getJobOrThrow(1L)).thenReturn(job);

        mockMvc.perform(get("/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId", is(1)))
                .andExpect(jsonPath("$.companyName", is("Google")));
    }

    @Test
    @DisplayName("GET /jobs/{id} - Should return 404 Not Found with error body when job does not exist")
    void testGetJobById_NotFound() throws Exception {
        when(jobService.getJobOrThrow(99L)).thenThrow(new ResourceNotFoundException("Job not found with ID: 99"));

        mockMvc.perform(get("/jobs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Job not found with ID: 99")));
    }

    @Test
    @DisplayName("PUT /jobs/{id} - Should return 200 OK on successful update")
    void testUpdateJob_Success() throws Exception {
        JobRequestDTO request = new JobRequestDTO("Google Inc", "Lead SWE", "Hyderabad", "Tech Lead", "CLOSED");
        Job updated = new Job(1L, "Google Inc", "Lead SWE", "Hyderabad", "Tech Lead", "CLOSED");

        when(jobService.updateJob(eq(1L), any(Job.class))).thenReturn(updated);

        mockMvc.perform(put("/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName", is("Google Inc")))
                .andExpect(jsonPath("$.role", is("Lead SWE")))
                .andExpect(jsonPath("$.status", is("CLOSED")));
    }

    @Test
    @DisplayName("PUT /jobs/{id} - Should return 404 Not Found when ID does not exist")
    void testUpdateJob_NotFound() throws Exception {
        JobRequestDTO request = new JobRequestDTO("Google", "SWE", "Hyderabad", "Dev", "OPEN");
        when(jobService.updateJob(eq(99L), any(Job.class)))
                .thenThrow(new ResourceNotFoundException("Job not found with ID: 99"));

        mockMvc.perform(put("/jobs/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Job not found with ID: 99")));
    }

    @Test
    @DisplayName("DELETE /jobs/{id} - Should return 204 No Content on successful deletion")
    void testDeleteJob_Success() throws Exception {
        doNothing().when(jobService).deleteJob(1L);

        mockMvc.perform(delete("/jobs/1"))
                .andExpect(status().isNoContent());

        verify(jobService, times(1)).deleteJob(1L);
    }

    @Test
    @DisplayName("DELETE /jobs/{id} - Should return 404 Not Found when job does not exist")
    void testDeleteJob_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Job not found with ID: 99")).when(jobService).deleteJob(99L);

        mockMvc.perform(delete("/jobs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Job not found with ID: 99")));
    }
}
