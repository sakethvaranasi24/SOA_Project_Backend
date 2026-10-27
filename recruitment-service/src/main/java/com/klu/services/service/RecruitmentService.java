package com.klu.services.service;

import com.klu.services.client.ApplicationClient;
import com.klu.services.exception.BadRequestException;
import com.klu.services.exception.ResourceNotFoundException;
import com.klu.services.model.Recruitment;
import com.klu.services.repo.RecruitmentRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class RecruitmentService {

    @Autowired
    private RecruitmentRepository recruitmentRepository;

    @Autowired(required = false)
    private ApplicationClient applicationClient;

    public Recruitment createRecruitment(Recruitment recruitment) {
        if (recruitment.getInterviewStatus() == null || recruitment.getInterviewStatus().trim().isEmpty()) {
            recruitment.setInterviewStatus("SCHEDULED");
        } else {
            recruitment.setInterviewStatus(recruitment.getInterviewStatus().trim().toUpperCase());
        }

        if (recruitment.getFinalStatus() == null || recruitment.getFinalStatus().trim().isEmpty()) {
            recruitment.setFinalStatus("PENDING");
        } else {
            recruitment.setFinalStatus(recruitment.getFinalStatus().trim().toUpperCase());
        }

        Recruitment saved = recruitmentRepository.save(recruitment);

        if (applicationClient != null) {
            try {
                applicationClient.updateApplicationStatus(recruitment.getApplicationId(), Collections.singletonMap("status", "INTERVIEW"));
            } catch (FeignException.NotFound e) {
                throw new ResourceNotFoundException("Application not found with ID: " + recruitment.getApplicationId());
            } catch (Exception e) {
                // Log and continue or handle
            }
        }
        return saved;
    }

    public Optional<Recruitment> getRecruitmentById(Long id) {
        return recruitmentRepository.findById(id);
    }

    public Recruitment getRecruitmentOrThrow(Long id) {
        return recruitmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recruitment record not found for ID: " + id));
    }

    public Recruitment updateInterviewStatus(Long id, String interviewStatus, String notes) {
        if (interviewStatus == null || interviewStatus.trim().isEmpty()) {
            throw new BadRequestException("Interview status is required");
        }

        String statusUpper = interviewStatus.trim().toUpperCase();
        if (!"SCHEDULED".equals(statusUpper) && !"COMPLETED".equals(statusUpper) && !"CANCELLED".equals(statusUpper)) {
            throw new BadRequestException("Invalid interview status: " + interviewStatus + ". Must be SCHEDULED, COMPLETED, or CANCELLED");
        }

        Recruitment recruitment = getRecruitmentOrThrow(id);
        recruitment.setInterviewStatus(statusUpper);
        if (notes != null && !notes.trim().isEmpty()) {
            recruitment.setNotes(notes.trim());
        }

        return recruitmentRepository.save(recruitment);
    }

    public Recruitment updateFinalStatus(Long id, String finalStatus) {
        if (finalStatus == null || finalStatus.trim().isEmpty()) {
            throw new BadRequestException("Final status is required");
        }

        String finalUpper = finalStatus.trim().toUpperCase();
        if (!"SELECTED".equals(finalUpper) && !"REJECTED".equals(finalUpper)) {
            throw new BadRequestException("Invalid final status: " + finalStatus + ". Must be SELECTED or REJECTED");
        }

        Recruitment recruitment = getRecruitmentOrThrow(id);

        if (!"COMPLETED".equalsIgnoreCase(recruitment.getInterviewStatus())) {
            throw new BadRequestException("Cannot record final decision until interview status is COMPLETED. Current interview status: " + recruitment.getInterviewStatus());
        }

        recruitment.setFinalStatus(finalUpper);
        Recruitment saved = recruitmentRepository.save(recruitment);

        if (applicationClient != null) {
            try {
                applicationClient.updateApplicationStatus(recruitment.getApplicationId(), Collections.singletonMap("status", finalUpper));
            } catch (Exception e) {
                // Log and continue
            }
        }
        return saved;
    }
}
