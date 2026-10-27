package com.klu.services.controller;

import com.klu.services.dto.FinalStatusDTO;
import com.klu.services.dto.InterviewUpdateDTO;
import com.klu.services.dto.RecruitmentRequestDTO;
import com.klu.services.dto.RecruitmentResponseDTO;
import com.klu.services.model.Recruitment;
import com.klu.services.service.RecruitmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recruitments")
public class RecruitmentController {

    @Autowired
    private RecruitmentService recruitmentService;

    @PostMapping
    public ResponseEntity<RecruitmentResponseDTO> createRecruitment(@Valid @RequestBody RecruitmentRequestDTO requestDTO) {
        Recruitment recruitment = Recruitment.builder()
                .applicationId(requestDTO.getApplicationId())
                .interviewDate(requestDTO.getInterviewDate())
                .interviewerName(requestDTO.getInterviewerName())
                .meetingLink(requestDTO.getMeetingLink())
                .notes(requestDTO.getNotes())
                .interviewStatus("SCHEDULED")
                .finalStatus("PENDING")
                .build();

        Recruitment created = recruitmentService.createRecruitment(recruitment);
        return new ResponseEntity<>(RecruitmentResponseDTO.fromEntity(created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecruitmentResponseDTO> getRecruitmentById(@PathVariable Long id) {
        Recruitment recruitment = recruitmentService.getRecruitmentOrThrow(id);
        return ResponseEntity.ok(RecruitmentResponseDTO.fromEntity(recruitment));
    }

    @PutMapping("/{id}/interview")
    public ResponseEntity<RecruitmentResponseDTO> updateInterviewStatus(
            @PathVariable Long id,
            @Valid @RequestBody InterviewUpdateDTO updateDTO) {
        Recruitment updated = recruitmentService.updateInterviewStatus(id, updateDTO.getInterviewStatus(), updateDTO.getNotes());
        return ResponseEntity.ok(RecruitmentResponseDTO.fromEntity(updated));
    }

    @PutMapping("/{id}/final-status")
    public ResponseEntity<RecruitmentResponseDTO> updateFinalStatus(
            @PathVariable Long id,
            @Valid @RequestBody FinalStatusDTO finalStatusDTO) {
        Recruitment updated = recruitmentService.updateFinalStatus(id, finalStatusDTO.getFinalStatus());
        return ResponseEntity.ok(RecruitmentResponseDTO.fromEntity(updated));
    }
}
