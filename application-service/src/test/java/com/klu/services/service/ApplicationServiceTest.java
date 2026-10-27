package com.klu.services.service;

import com.klu.services.client.JobClient;
import com.klu.services.dto.JobDTO;
import com.klu.services.exception.BadRequestException;
import com.klu.services.exception.DuplicateApplicationException;
import com.klu.services.exception.ResourceNotFoundException;
import com.klu.services.exception.ServiceUnavailableException;
import com.klu.services.model.Application;
import com.klu.services.repo.ApplicationRepository;
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

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobClient jobClient;

    @InjectMocks
    private ApplicationService applicationService;

    private Application sampleApp;
    private JobDTO sampleOpenJob;

    @BeforeEach
    void setUp() {
        sampleApp = Application.builder()
                .applicationId(1L)
                .jobId(10L)
                .candidateId(100L)
                .resumeLink("https://storage.talentsync.com/resumes/c100.pdf")
                .status("APPLIED")
                .build();

        sampleOpenJob = new JobDTO(10L, "Google", "SWE", "Hyderabad", "Backend dev", "OPEN");
    }

    @Test
    @DisplayName("applyForJob should succeed when job is OPEN and no duplicate exists")
    void testApplyForJob_Success() {
        when(applicationRepository.existsByCandidateIdAndJobId(100L, 10L)).thenReturn(false);
        when(jobClient.getJobById(10L)).thenReturn(sampleOpenJob);
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        Application created = applicationService.applyForJob(sampleApp);

        assertNotNull(created);
        assertEquals("APPLIED", created.getStatus());
        verify(applicationRepository, times(1)).save(sampleApp);
    }

    @Test
    @DisplayName("applyForJob should throw DuplicateApplicationException when candidate already applied")
    void testApplyForJob_Duplicate_ThrowsDuplicateException() {
        when(applicationRepository.existsByCandidateIdAndJobId(100L, 10L)).thenReturn(true);

        assertThrows(DuplicateApplicationException.class, () -> applicationService.applyForJob(sampleApp));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("applyForJob should throw BadRequestException when job status is CLOSED")
    void testApplyForJob_ClosedJob_ThrowsBadRequest() {
        when(applicationRepository.existsByCandidateIdAndJobId(100L, 10L)).thenReturn(false);
        JobDTO closedJob = new JobDTO(10L, "Google", "SWE", "Hyderabad", "Backend dev", "CLOSED");
        when(jobClient.getJobById(10L)).thenReturn(closedJob);

        assertThrows(BadRequestException.class, () -> applicationService.applyForJob(sampleApp));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("applyForJob should throw ResourceNotFoundException when job does not exist in Job Service")
    void testApplyForJob_JobNotFound_ThrowsResourceNotFound() {
        when(applicationRepository.existsByCandidateIdAndJobId(100L, 10L)).thenReturn(false);
        Request request = Request.create(Request.HttpMethod.GET, "/jobs/10", Collections.emptyMap(), null, new RequestTemplate());
        when(jobClient.getJobById(10L)).thenThrow(new FeignException.NotFound("Not Found", request, null, null));

        assertThrows(ResourceNotFoundException.class, () -> applicationService.applyForJob(sampleApp));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("applyForJob should throw ServiceUnavailableException when Job Service is down")
    void testApplyForJob_ServiceDown_ThrowsServiceUnavailable() {
        when(applicationRepository.existsByCandidateIdAndJobId(100L, 10L)).thenReturn(false);
        when(jobClient.getJobById(10L)).thenThrow(new RuntimeException("Connection refused"));

        assertThrows(ServiceUnavailableException.class, () -> applicationService.applyForJob(sampleApp));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("getApplicationOrThrow should return application when found")
    void testGetApplicationOrThrow_Success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));

        Application found = applicationService.getApplicationOrThrow(1L);

        assertNotNull(found);
        assertEquals(1L, found.getApplicationId());
    }

    @Test
    @DisplayName("getApplicationOrThrow should throw ResourceNotFoundException when id missing")
    void testGetApplicationOrThrow_NotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> applicationService.getApplicationOrThrow(99L));
    }

    @Test
    @DisplayName("getApplicationsByCandidateId should return candidate applications")
    void testGetApplicationsByCandidateId() {
        when(applicationRepository.findByCandidateId(100L)).thenReturn(Collections.singletonList(sampleApp));

        List<Application> list = applicationService.getApplicationsByCandidateId(100L);

        assertEquals(1, list.size());
        assertEquals(100L, list.get(0).getCandidateId());
    }

    @Test
    @DisplayName("getApplicationsByJobId should return job applications")
    void testGetApplicationsByJobId() {
        when(applicationRepository.findByJobId(10L)).thenReturn(Collections.singletonList(sampleApp));

        List<Application> list = applicationService.getApplicationsByJobId(10L);

        assertEquals(1, list.size());
        assertEquals(10L, list.get(0).getJobId());
    }

    @Test
    @DisplayName("updateApplicationStatus should successfully transition from APPLIED to SHORTLISTED")
    void testUpdateStatus_ValidTransition_Success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        Application updated = applicationService.updateApplicationStatus(1L, "SHORTLISTED");

        assertEquals("SHORTLISTED", updated.getStatus());
    }

    @Test
    @DisplayName("updateApplicationStatus should reject invalid transition like REJECTED to SELECTED")
    void testUpdateStatus_InvalidTransition_ThrowsBadRequest() {
        sampleApp.setStatus("REJECTED");
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));

        assertThrows(BadRequestException.class, () -> applicationService.updateApplicationStatus(1L, "SELECTED"));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("updateApplicationStatus should allow idempotent update to same status")
    void testUpdateStatus_SameStatus_Idempotent() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));

        Application result = applicationService.updateApplicationStatus(1L, "APPLIED");

        assertEquals("APPLIED", result.getStatus());
    }
}
