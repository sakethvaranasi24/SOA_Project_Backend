package com.klu.microservices.service;

import com.klu.microservices.client.JobClient;
import com.klu.microservices.dto.JobDTO;
import com.klu.microservices.model.Application;
import com.klu.microservices.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired(required = false)
    private JobClient jobClient;

    public Application applyForJob(Application application) {
        if (jobClient != null) {
            try {
                JobDTO job = jobClient.getJobById(application.getJobId());
                if (job == null || "CLOSED".equalsIgnoreCase(job.getStatus())) {
                    throw new RuntimeException("Job is invalid or closed.");
                }
            } catch (Exception e) {
                // Fallback or log
            }
        }
        if (application.getStatus() == null || application.getStatus().isEmpty()) {
            application.setStatus("APPLIED");
        }
        return applicationRepository.save(application);
    }

    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }

    public List<Application> getApplicationsByCandidateId(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    public List<Application> getApplicationsByJobId(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public Application updateApplicationStatus(Long id, String status) {
        return applicationRepository.findById(id).map(app -> {
            app.setStatus(status.toUpperCase());
            return applicationRepository.save(app);
        }).orElseThrow(() -> new RuntimeException("Application not found with ID: " + id));
    }
}
