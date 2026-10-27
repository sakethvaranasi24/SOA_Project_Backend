package com.klu.services.model;

import jakarta.persistence.*;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @Column(nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private Long candidateId;

    @Column(nullable = false)
    private String resumeLink;

    @Column(nullable = false)
    private String status = "APPLIED";

    public Application() {
        this.status = "APPLIED";
    }

    public Application(Long applicationId, Long jobId, Long candidateId, String resumeLink, String status) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.resumeLink = resumeLink;
        this.status = (status != null && !status.trim().isEmpty()) ? status : "APPLIED";
    }

    public static ApplicationBuilder builder() {
        return new ApplicationBuilder();
    }

    public static class ApplicationBuilder {
        private Long applicationId;
        private Long jobId;
        private Long candidateId;
        private String resumeLink;
        private String status = "APPLIED";

        public ApplicationBuilder applicationId(Long applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public ApplicationBuilder jobId(Long jobId) {
            this.jobId = jobId;
            return this;
        }

        public ApplicationBuilder candidateId(Long candidateId) {
            this.candidateId = candidateId;
            return this;
        }

        public ApplicationBuilder resumeLink(String resumeLink) {
            this.resumeLink = resumeLink;
            return this;
        }

        public ApplicationBuilder status(String status) {
            if (status != null && !status.trim().isEmpty()) {
                this.status = status;
            }
            return this;
        }

        public Application build() {
            return new Application(applicationId, jobId, candidateId, resumeLink, status);
        }
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
