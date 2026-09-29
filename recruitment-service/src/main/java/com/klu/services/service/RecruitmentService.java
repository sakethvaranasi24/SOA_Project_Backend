package com.klu.services.service;

import com.klu.services.client.ApplicationClient;
import com.klu.services.model.Recruitment;
import com.klu.services.repo.RecruitmentRepository;
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
        if (recruitment.getInterviewStatus() == null) recruitment.setInterviewStatus("SCHEDULED");
        if (recruitment.getFinalStatus() == null) recruitment.setFinalStatus("PENDING");
        
        Recruitment saved = recruitmentRepository.save(recruitment);
        
        if (applicationClient != null) {
            try {
                applicationClient.updateApplicationStatus(recruitment.getApplicationId(), Collections.singletonMap("status", "INTERVIEW"));
            } catch (Exception e) {
                // Feign fallback
            }
        }
        return saved;
    }

    public Optional<Recruitment> getRecruitmentById(Long id) {
        return recruitmentRepository.findById(id);
    }

    public Recruitment updateInterviewStatus(Long id, String interviewStatus) {
        return recruitmentRepository.findById(id).map(r -> {
            r.setInterviewStatus(interviewStatus.toUpperCase());
            return recruitmentRepository.save(r);
        }).orElseThrow(() -> new RuntimeException("Recruitment record not found for ID: " + id));
    }

    public Recruitment updateFinalStatus(Long id, String finalStatus) {
        return recruitmentRepository.findById(id).map(r -> {
            r.setFinalStatus(finalStatus.toUpperCase());
            Recruitment saved = recruitmentRepository.save(r);

            if (applicationClient != null) {
                try {
                    applicationClient.updateApplicationStatus(r.getApplicationId(), Collections.singletonMap("status", finalStatus.toUpperCase()));
                } catch (Exception e) {
                    // Feign fallback
                }
            }
            return saved;
        }).orElseThrow(() -> new RuntimeException("Recruitment record not found for ID: " + id));
    }
}
