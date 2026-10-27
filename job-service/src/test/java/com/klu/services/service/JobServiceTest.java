package com.klu.services.service;

import com.klu.services.exception.BadRequestException;
import com.klu.services.exception.ResourceNotFoundException;
import com.klu.services.model.Job;
import com.klu.services.repo.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    private Job sampleJob;

    @BeforeEach
    void setUp() {
        sampleJob = Job.builder()
                .jobId(1L)
                .companyName("TechCorp")
                .role("Backend Engineer")
                .location("Hyderabad")
                .description("Build microservices")
                .status("OPEN")
                .build();
    }

    @Test
    @DisplayName("createJob should default status to OPEN when status is null or blank")
    void testCreateJob_DefaultStatus() {
        Job newJob = Job.builder()
                .companyName("TechCorp")
                .role("Backend Engineer")
                .location("Hyderabad")
                .status(null)
                .build();

        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job created = jobService.createJob(newJob);

        assertNotNull(created);
        assertEquals("OPEN", created.getStatus());
        verify(jobRepository, times(1)).save(newJob);
    }

    @Test
    @DisplayName("createJob should preserve valid status like CLOSED")
    void testCreateJob_ValidCustomStatus() {
        Job newJob = Job.builder()
                .companyName("TechCorp")
                .role("Backend Engineer")
                .location("Hyderabad")
                .status("CLOSED")
                .build();

        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job created = jobService.createJob(newJob);

        assertNotNull(created);
        assertEquals("CLOSED", created.getStatus());
    }

    @Test
    @DisplayName("createJob should throw BadRequestException for invalid status value")
    void testCreateJob_InvalidStatus_ThrowsBadRequest() {
        Job invalidJob = Job.builder()
                .companyName("TechCorp")
                .role("Backend Engineer")
                .location("Hyderabad")
                .status("SUSPENDED")
                .build();

        assertThrows(BadRequestException.class, () -> jobService.createJob(invalidJob));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("getAllJobs should return all jobs from repository")
    void testGetAllJobs() {
        Job job2 = Job.builder().jobId(2L).companyName("InnoSoft").role("DevOps").location("Remote").status("OPEN").build();
        when(jobRepository.findAll()).thenReturn(Arrays.asList(sampleJob, job2));

        List<Job> jobs = jobService.getAllJobs();

        assertEquals(2, jobs.size());
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getJobOrThrow should return job when found by id")
    void testGetJobOrThrow_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));

        Job found = jobService.getJobOrThrow(1L);

        assertNotNull(found);
        assertEquals(1L, found.getJobId());
        assertEquals("TechCorp", found.getCompanyName());
    }

    @Test
    @DisplayName("getJobOrThrow should throw ResourceNotFoundException when id does not exist")
    void testGetJobOrThrow_NotFound() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.getJobOrThrow(99L));
    }

    @Test
    @DisplayName("updateJob should update fields and save successfully")
    void testUpdateJob_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job updateDetails = Job.builder()
                .companyName("TechCorp Solutions")
                .role("Lead Engineer")
                .location("Bangalore")
                .description("Lead the team")
                .status("CLOSED")
                .build();

        Job updated = jobService.updateJob(1L, updateDetails);

        assertEquals("TechCorp Solutions", updated.getCompanyName());
        assertEquals("Lead Engineer", updated.getRole());
        assertEquals("Bangalore", updated.getLocation());
        assertEquals("CLOSED", updated.getStatus());
    }

    @Test
    @DisplayName("updateJob should throw BadRequestException for invalid status")
    void testUpdateJob_InvalidStatus_ThrowsBadRequest() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));

        Job updateDetails = Job.builder()
                .status("DRAFT")
                .build();

        assertThrows(BadRequestException.class, () -> jobService.updateJob(1L, updateDetails));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("updateJob should throw ResourceNotFoundException when id does not exist")
    void testUpdateJob_NotFound() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        Job updateDetails = Job.builder().companyName("ABC").build();

        assertThrows(ResourceNotFoundException.class, () -> jobService.updateJob(99L, updateDetails));
    }

    @Test
    @DisplayName("deleteJob should soft-delete job by setting status to CLOSED")
    void testDeleteJob_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        jobService.deleteJob(1L);

        assertEquals("CLOSED", sampleJob.getStatus());
        verify(jobRepository, times(1)).save(sampleJob);
    }

    @Test
    @DisplayName("deleteJob should throw ResourceNotFoundException when id does not exist")
    void testDeleteJob_NotFound() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.deleteJob(99L));
        verify(jobRepository, never()).save(any(Job.class));
    }
}
