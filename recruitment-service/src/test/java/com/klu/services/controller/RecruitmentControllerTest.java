package com.klu.services.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klu.services.dto.FinalStatusDTO;
import com.klu.services.dto.InterviewUpdateDTO;
import com.klu.services.dto.RecruitmentRequestDTO;
import com.klu.services.exception.BadRequestException;
import com.klu.services.exception.GlobalExceptionHandler;
import com.klu.services.exception.ResourceNotFoundException;
import com.klu.services.model.Recruitment;
import com.klu.services.service.RecruitmentService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RecruitmentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RecruitmentService recruitmentService;

    @InjectMocks
    private RecruitmentController recruitmentController;

    private Recruitment sampleRecruitment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recruitmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        sampleRecruitment = new Recruitment(
                1L,
                10L,
                "2026-11-15T10:00:00",
                "Dr. Ramesh",
                "https://meet.talentsync.com/room-1",
                "Preliminary round",
                "SCHEDULED",
                "PENDING"
        );
    }

    @Test
    @DisplayName("POST /recruitments - Should return 201 Created with valid payload")
    void testCreateRecruitment_Valid() throws Exception {
        RecruitmentRequestDTO request = new RecruitmentRequestDTO(
                10L,
                "2026-11-15T10:00:00",
                "Dr. Ramesh",
                "https://meet.talentsync.com/room-1",
                "Preliminary round"
        );
        when(recruitmentService.createRecruitment(any(Recruitment.class))).thenReturn(sampleRecruitment);

        mockMvc.perform(post("/recruitments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recruitmentId", is(1)))
                .andExpect(jsonPath("$.applicationId", is(10)))
                .andExpect(jsonPath("$.interviewerName", is("Dr. Ramesh")))
                .andExpect(jsonPath("$.interviewStatus", is("SCHEDULED")));
    }

    @Test
    @DisplayName("POST /recruitments - Should return 400 Bad Request when required fields are blank")
    void testCreateRecruitment_ValidationFailure() throws Exception {
        RecruitmentRequestDTO request = new RecruitmentRequestDTO(null, "", "", null, null);

        mockMvc.perform(post("/recruitments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Validation failed")))
                .andExpect(jsonPath("$.details", hasSize(3)));
    }

    @Test
    @DisplayName("GET /recruitments/{id} - Should return 200 OK when record exists")
    void testGetRecruitmentById_Found() throws Exception {
        when(recruitmentService.getRecruitmentOrThrow(1L)).thenReturn(sampleRecruitment);

        mockMvc.perform(get("/recruitments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recruitmentId", is(1)))
                .andExpect(jsonPath("$.applicationId", is(10)));
    }

    @Test
    @DisplayName("GET /recruitments/{id} - Should return 404 Not Found when record does not exist")
    void testGetRecruitmentById_NotFound() throws Exception {
        when(recruitmentService.getRecruitmentOrThrow(99L))
                .thenThrow(new ResourceNotFoundException("Recruitment record not found for ID: 99"));

        mockMvc.perform(get("/recruitments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Recruitment record not found for ID: 99")));
    }

    @Test
    @DisplayName("PUT /recruitments/{id}/interview - Should return 200 OK on valid status update")
    void testUpdateInterviewStatus_Valid() throws Exception {
        InterviewUpdateDTO updateDTO = new InterviewUpdateDTO("COMPLETED", "Cleared round 1");
        Recruitment updated = new Recruitment(
                1L,
                10L,
                "2026-11-15T10:00:00",
                "Dr. Ramesh",
                "https://meet.talentsync.com/room-1",
                "Cleared round 1",
                "COMPLETED",
                "PENDING"
        );
        when(recruitmentService.updateInterviewStatus(eq(1L), eq("COMPLETED"), eq("Cleared round 1"))).thenReturn(updated);

        mockMvc.perform(put("/recruitments/1/interview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interviewStatus", is("COMPLETED")))
                .andExpect(jsonPath("$.notes", is("Cleared round 1")));
    }

    @Test
    @DisplayName("PUT /recruitments/{id}/interview - Should return 400 Bad Request on invalid status pattern")
    void testUpdateInterviewStatus_InvalidPattern() throws Exception {
        InterviewUpdateDTO updateDTO = new InterviewUpdateDTO("POSTPONED", "Notes");

        mockMvc.perform(put("/recruitments/1/interview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", hasItem(containsString("SCHEDULED, COMPLETED, or CANCELLED"))));
    }

    @Test
    @DisplayName("PUT /recruitments/{id}/final-status - Should return 200 OK on final decision")
    void testUpdateFinalStatus_Valid() throws Exception {
        FinalStatusDTO finalStatusDTO = new FinalStatusDTO("SELECTED");
        Recruitment updated = new Recruitment(
                1L,
                10L,
                "2026-11-15T10:00:00",
                "Dr. Ramesh",
                "https://meet.talentsync.com/room-1",
                "Cleared round 1",
                "COMPLETED",
                "SELECTED"
        );
        when(recruitmentService.updateFinalStatus(eq(1L), eq("SELECTED"))).thenReturn(updated);

        mockMvc.perform(put("/recruitments/1/final-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finalStatusDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalStatus", is("SELECTED")));
    }

    @Test
    @DisplayName("PUT /recruitments/{id}/final-status - Should return 400 Bad Request when interview not completed")
    void testUpdateFinalStatus_InterviewNotCompleted() throws Exception {
        FinalStatusDTO finalStatusDTO = new FinalStatusDTO("SELECTED");
        when(recruitmentService.updateFinalStatus(eq(1L), eq("SELECTED")))
                .thenThrow(new BadRequestException("Cannot record final decision until interview status is COMPLETED"));

        mockMvc.perform(put("/recruitments/1/final-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finalStatusDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Cannot record final decision until interview status is COMPLETED")));
    }
}
