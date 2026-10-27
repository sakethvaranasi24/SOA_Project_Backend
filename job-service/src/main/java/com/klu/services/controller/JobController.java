package com.klu.services.controller;

import com.klu.services.dto.JobRequestDTO;
import com.klu.services.dto.JobResponseDTO;
import com.klu.services.model.Job;
import com.klu.services.service.JobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponseDTO> createJob(@Valid @RequestBody JobRequestDTO requestDTO) {
        Job job = Job.builder()
                .companyName(requestDTO.getCompanyName())
                .role(requestDTO.getRole())
                .location(requestDTO.getLocation())
                .description(requestDTO.getDescription())
                .status(requestDTO.getStatus())
                .build();

        Job createdJob = jobService.createJob(job);
        return new ResponseEntity<>(JobResponseDTO.fromEntity(createdJob), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<JobResponseDTO>> getAllJobs() {
        List<JobResponseDTO> jobs = jobService.getAllJobs().stream()
                .map(JobResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobOrThrow(id);
        return ResponseEntity.ok(JobResponseDTO.fromEntity(job));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponseDTO> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequestDTO requestDTO) {
        Job jobDetails = Job.builder()
                .companyName(requestDTO.getCompanyName())
                .role(requestDTO.getRole())
                .location(requestDTO.getLocation())
                .description(requestDTO.getDescription())
                .status(requestDTO.getStatus())
                .build();

        Job updatedJob = jobService.updateJob(id, jobDetails);
        return ResponseEntity.ok(JobResponseDTO.fromEntity(updatedJob));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
