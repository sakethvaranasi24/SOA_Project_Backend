package com.klu.microservices.service;

import com.klu.microservices.model.Job;
import com.klu.microservices.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public Job createJob(Job job) {
        if (job.getStatus() == null || job.getStatus().isEmpty()) {
            job.setStatus("OPEN");
        }
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public Job updateJob(Long id, Job updatedJob) {
        return jobRepository.findById(id).map(existingJob -> {
            if (updatedJob.getCompanyName() != null) existingJob.setCompanyName(updatedJob.getCompanyName());
            if (updatedJob.getRole() != null) existingJob.setRole(updatedJob.getRole());
            if (updatedJob.getLocation() != null) existingJob.setLocation(updatedJob.getLocation());
            if (updatedJob.getDescription() != null) existingJob.setDescription(updatedJob.getDescription());
            if (updatedJob.getStatus() != null) existingJob.setStatus(updatedJob.getStatus());
            return jobRepository.save(existingJob);
        }).orElseThrow(() -> new RuntimeException("Job not found with ID: " + id));
    }

    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + id));
        job.setStatus("CLOSED");
        jobRepository.save(job);
    }
}
