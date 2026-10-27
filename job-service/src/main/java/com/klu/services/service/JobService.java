package com.klu.services.service;

import com.klu.services.exception.BadRequestException;
import com.klu.services.exception.ResourceNotFoundException;
import com.klu.services.model.Job;
import com.klu.services.repo.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public Job createJob(Job job) {
        if (job.getStatus() == null || job.getStatus().trim().isEmpty()) {
            job.setStatus("OPEN");
        } else {
            String upperStatus = job.getStatus().trim().toUpperCase();
            if (!"OPEN".equals(upperStatus) && !"CLOSED".equals(upperStatus)) {
                throw new BadRequestException("Status must be either OPEN or CLOSED");
            }
            job.setStatus(upperStatus);
        }
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public Job getJobOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));
    }

    public Job updateJob(Long id, Job updatedJob) {
        Job existingJob = getJobOrThrow(id);

        if (updatedJob.getCompanyName() != null && !updatedJob.getCompanyName().trim().isEmpty()) {
            existingJob.setCompanyName(updatedJob.getCompanyName().trim());
        }
        if (updatedJob.getRole() != null && !updatedJob.getRole().trim().isEmpty()) {
            existingJob.setRole(updatedJob.getRole().trim());
        }
        if (updatedJob.getLocation() != null && !updatedJob.getLocation().trim().isEmpty()) {
            existingJob.setLocation(updatedJob.getLocation().trim());
        }
        if (updatedJob.getDescription() != null) {
            existingJob.setDescription(updatedJob.getDescription().trim());
        }
        if (updatedJob.getStatus() != null && !updatedJob.getStatus().trim().isEmpty()) {
            String upperStatus = updatedJob.getStatus().trim().toUpperCase();
            if (!"OPEN".equals(upperStatus) && !"CLOSED".equals(upperStatus)) {
                throw new BadRequestException("Invalid job status: " + updatedJob.getStatus() + ". Allowed values are OPEN, CLOSED");
            }
            existingJob.setStatus(upperStatus);
        }

        return jobRepository.save(existingJob);
    }

    public void deleteJob(Long id) {
        Job job = getJobOrThrow(id);
        job.setStatus("CLOSED");
        jobRepository.save(job);
    }
}
