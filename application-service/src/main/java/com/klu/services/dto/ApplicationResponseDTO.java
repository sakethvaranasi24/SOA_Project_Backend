package com.klu.services.dto;

import com.klu.services.model.Application;

public class ApplicationResponseDTO {

    private Long applicationId;
    private Long jobId;
    private Long candidateId;
    private String resumeLink;
    private String status;

    public ApplicationResponseDTO() {
    }

    public ApplicationResponseDTO(Long applicationId, Long jobId, Long candidateId, String resumeLink, String status) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.resumeLink = resumeLink;
        this.status = status;
    }

    public static ApplicationResponseDTO fromEntity(Application app) {
        if (app == null) return null;
        return new ApplicationResponseDTO(
                app.getApplicationId(),
                app.getJobId(),
                app.getCandidateId(),
                app.getResumeLink(),
                app.getStatus()
        );
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getResumeLink() {
        return resumeLink;
    }

    public void setResumeLink(String resumeLink) {
        this.resumeLink = resumeLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
