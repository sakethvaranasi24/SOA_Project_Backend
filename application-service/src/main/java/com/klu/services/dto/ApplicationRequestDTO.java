package com.klu.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ApplicationRequestDTO {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    @NotBlank(message = "Resume link is required")
    private String resumeLink;

    public ApplicationRequestDTO() {
    }

    public ApplicationRequestDTO(Long jobId, Long candidateId, String resumeLink) {
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.resumeLink = resumeLink;
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
}
