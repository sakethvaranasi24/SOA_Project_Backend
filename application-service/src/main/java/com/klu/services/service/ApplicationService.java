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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired(required = false)
    private JobClient jobClient;

    // Allowed status transitions mapping
    private static final Map<String, Set<String>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put("APPLIED", new HashSet<>(Arrays.asList("SHORTLISTED", "REJECTED", "INTERVIEW")));
        VALID_TRANSITIONS.put("SHORTLISTED", new HashSet<>(Arrays.asList("INTERVIEW", "REJECTED")));
        VALID_TRANSITIONS.put("INTERVIEW", new HashSet<>(Arrays.asList("SELECTED", "REJECTED")));
        VALID_TRANSITIONS.put("SELECTED", Collections.emptySet());
        VALID_TRANSITIONS.put("REJECTED", Collections.emptySet());
    }

    public Application applyForJob(Application application) {
        if (applicationRepository.existsByCandidateIdAndJobId(application.getCandidateId(), application.getJobId())) {
            throw new DuplicateApplicationException("Candidate " + application.getCandidateId() + " has already applied for Job " + application.getJobId());
        }

        if (jobClient != null) {
            JobDTO job;
            try {
                job = jobClient.getJobById(application.getJobId());
            } catch (FeignException.NotFound e) {
                throw new ResourceNotFoundException("Referenced job not found with ID: " + application.getJobId());
            } catch (Exception e) {
                throw new ServiceUnavailableException("Job Service is currently unreachable. Please try again later.");
            }

            if (job == null) {
                throw new ResourceNotFoundException("Referenced job not found with ID: " + application.getJobId());
            }

            if ("CLOSED".equalsIgnoreCase(job.getStatus())) {
                throw new BadRequestException("Cannot apply to closed job with ID: " + application.getJobId());
            }
        }

        if (application.getStatus() == null || application.getStatus().trim().isEmpty()) {
            application.setStatus("APPLIED");
        } else {
            application.setStatus(application.getStatus().trim().toUpperCase());
        }

        return applicationRepository.save(application);
    }

    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }

    public Application getApplicationOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));
    }

    public List<Application> getApplicationsByCandidateId(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    public List<Application> getApplicationsByJobId(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public Application updateApplicationStatus(Long id, String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Status value is required");
        }

        Application application = getApplicationOrThrow(id);
        String currentStatus = application.getStatus().toUpperCase();
        String targetStatus = status.trim().toUpperCase();

        if (currentStatus.equals(targetStatus)) {
            return application;
        }

        Set<String> allowedNext = VALID_TRANSITIONS.get(currentStatus);
        if (allowedNext == null || !allowedNext.contains(targetStatus)) {
            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + targetStatus);
        }

        application.setStatus(targetStatus);
        return applicationRepository.save(application);
    }
}
