package com.klu.services.controller;

import com.klu.services.dto.ApplicationRequestDTO;
import com.klu.services.dto.ApplicationResponseDTO;
import com.klu.services.dto.StatusUpdateDTO;
import com.klu.services.model.Application;
import com.klu.services.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> applyForJob(@Valid @RequestBody ApplicationRequestDTO requestDTO) {
        Application application = Application.builder()
                .jobId(requestDTO.getJobId())
                .candidateId(requestDTO.getCandidateId())
                .resumeLink(requestDTO.getResumeLink())
                .status("APPLIED")
                .build();

        Application createdApp = applicationService.applyForJob(application);
        return new ResponseEntity<>(ApplicationResponseDTO.fromEntity(createdApp), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(@PathVariable Long id) {
        Application application = applicationService.getApplicationOrThrow(id);
        return ResponseEntity.ok(ApplicationResponseDTO.fromEntity(application));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByCandidate(@PathVariable Long candidateId) {
        List<ApplicationResponseDTO> list = applicationService.getApplicationsByCandidateId(candidateId)
                .stream()
                .map(ApplicationResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByJob(@PathVariable Long jobId) {
        List<ApplicationResponseDTO> list = applicationService.getApplicationsByJobId(jobId)
                .stream()
                .map(ApplicationResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponseDTO> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO statusDTO) {
        Application updated = applicationService.updateApplicationStatus(id, statusDTO.getStatus());
        return ResponseEntity.ok(ApplicationResponseDTO.fromEntity(updated));
    }
}
