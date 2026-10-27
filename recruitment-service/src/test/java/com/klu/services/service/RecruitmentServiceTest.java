package com.klu.services.service;

import com.klu.services.client.ApplicationClient;
import com.klu.services.exception.BadRequestException;
import com.klu.services.exception.ResourceNotFoundException;
import com.klu.services.model.Recruitment;
import com.klu.services.repo.RecruitmentRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecruitmentServiceTest {

    @Mock
    private RecruitmentRepository recruitmentRepository;

    @Mock
    private ApplicationClient applicationClient;

    @InjectMocks
    private RecruitmentService recruitmentService;

    private Recruitment sampleRecruitment;

    @BeforeEach
    void setUp() {
        sampleRecruitment = Recruitment.builder()
                .recruitmentId(1L)
                .applicationId(10L)
                .interviewDate("2026-11-15T10:00:00")
                .interviewerName("Dr. Ramesh")
                .meetingLink("https://meet.talentsync.com/room-1")
                .notes("Preliminary round")
                .interviewStatus("SCHEDULED")
                .finalStatus("PENDING")
                .build();
    }

    @Test
    @DisplayName("createRecruitment should succeed and trigger Feign to update application status to INTERVIEW")
    void testCreateRecruitment_Success() {
        when(recruitmentRepository.save(any(Recruitment.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(applicationClient).updateApplicationStatus(eq(10L), any());

        Recruitment created = recruitmentService.createRecruitment(sampleRecruitment);

        assertNotNull(created);
        assertEquals("SCHEDULED", created.getInterviewStatus());
        assertEquals("PENDING", created.getFinalStatus());
        verify(recruitmentRepository, times(1)).save(sampleRecruitment);
        verify(applicationClient, times(1)).updateApplicationStatus(eq(10L), eq(Collections.singletonMap("status", "INTERVIEW")));
    }

    @Test
    @DisplayName("createRecruitment should throw ResourceNotFoundException when application does not exist")
    void testCreateRecruitment_ApplicationNotFound() {
        when(recruitmentRepository.save(any(Recruitment.class))).thenAnswer(i -> i.getArgument(0));
        Request request = Request.create(Request.HttpMethod.PUT, "/applications/10/status", Collections.emptyMap(), null, new RequestTemplate());
        doThrow(new FeignException.NotFound("Not Found", request, null, null))
                .when(applicationClient).updateApplicationStatus(eq(10L), any());

        assertThrows(ResourceNotFoundException.class, () -> recruitmentService.createRecruitment(sampleRecruitment));
    }

    @Test
    @DisplayName("getRecruitmentOrThrow should return record when found")
    void testGetRecruitmentOrThrow_Success() {
        when(recruitmentRepository.findById(1L)).thenReturn(Optional.of(sampleRecruitment));

        Recruitment found = recruitmentService.getRecruitmentOrThrow(1L);

        assertNotNull(found);
        assertEquals(1L, found.getRecruitmentId());
        assertEquals("Dr. Ramesh", found.getInterviewerName());
    }

    @Test
    @DisplayName("getRecruitmentOrThrow should throw ResourceNotFoundException when record missing")
    void testGetRecruitmentOrThrow_NotFound() {
        when(recruitmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recruitmentService.getRecruitmentOrThrow(99L));
    }

    @Test
    @DisplayName("updateInterviewStatus should update status to COMPLETED with notes")
    void testUpdateInterviewStatus_Success() {
        when(recruitmentRepository.findById(1L)).thenReturn(Optional.of(sampleRecruitment));
        when(recruitmentRepository.save(any(Recruitment.class))).thenAnswer(i -> i.getArgument(0));

        Recruitment updated = recruitmentService.updateInterviewStatus(1L, "COMPLETED", "Candidate showed great expertise");

        assertEquals("COMPLETED", updated.getInterviewStatus());
        assertEquals("Candidate showed great expertise", updated.getNotes());
        verify(recruitmentRepository, times(1)).save(sampleRecruitment);
    }

    @Test
    @DisplayName("updateInterviewStatus should throw BadRequestException for invalid status")
    void testUpdateInterviewStatus_InvalidStatus() {
        assertThrows(BadRequestException.class, () -> recruitmentService.updateInterviewStatus(1L, "POSTPONED", "Notes"));
        verify(recruitmentRepository, never()).save(any(Recruitment.class));
    }

    @Test
    @DisplayName("updateFinalStatus should succeed when interview is COMPLETED and trigger Feign update")
    void testUpdateFinalStatus_Success() {
        sampleRecruitment.setInterviewStatus("COMPLETED");
        when(recruitmentRepository.findById(1L)).thenReturn(Optional.of(sampleRecruitment));
        when(recruitmentRepository.save(any(Recruitment.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(applicationClient).updateApplicationStatus(eq(10L), any());

        Recruitment updated = recruitmentService.updateFinalStatus(1L, "SELECTED");

        assertEquals("SELECTED", updated.getFinalStatus());
        verify(applicationClient, times(1)).updateApplicationStatus(eq(10L), eq(Collections.singletonMap("status", "SELECTED")));
    }

    @Test
    @DisplayName("updateFinalStatus should throw BadRequestException if interview is NOT COMPLETED")
    void testUpdateFinalStatus_InterviewNotCompleted_ThrowsBadRequest() {
        sampleRecruitment.setInterviewStatus("SCHEDULED");
        when(recruitmentRepository.findById(1L)).thenReturn(Optional.of(sampleRecruitment));

        assertThrows(BadRequestException.class, () -> recruitmentService.updateFinalStatus(1L, "SELECTED"));
        verify(recruitmentRepository, never()).save(any(Recruitment.class));
    }

    @Test
    @DisplayName("updateFinalStatus should throw BadRequestException if status is not SELECTED or REJECTED")
    void testUpdateFinalStatus_InvalidStatus_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> recruitmentService.updateFinalStatus(1L, "HIRED"));
        verify(recruitmentRepository, never()).save(any(Recruitment.class));
    }
}
