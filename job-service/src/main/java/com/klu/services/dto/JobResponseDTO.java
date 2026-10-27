package com.klu.services.dto;

import com.klu.services.model.Job;

public class JobResponseDTO {

    private Long jobId;
    private String companyName;
    private String role;
    private String location;
    private String description;
    private String status;

    public JobResponseDTO() {
    }

    public JobResponseDTO(Long jobId, String companyName, String role, String location, String description, String status) {
        this.jobId = jobId;
        this.companyName = companyName;
        this.role = role;
        this.location = location;
        this.description = description;
        this.status = status;
    }

    public static JobResponseDTO fromEntity(Job job) {
        if (job == null) return null;
        return new JobResponseDTO(
                job.getJobId(),
                job.getCompanyName(),
                job.getRole(),
                job.getLocation(),
                job.getDescription(),
                job.getStatus()
        );
    }

    public static JobResponseDTOBuilder builder() {
        return new JobResponseDTOBuilder();
    }

    public static class JobResponseDTOBuilder {
        private Long jobId;
        private String companyName;
        private String role;
        private String location;
        private String description;
        private String status;

        public JobResponseDTOBuilder jobId(Long jobId) {
            this.jobId = jobId;
            return this;
        }

        public JobResponseDTOBuilder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public JobResponseDTOBuilder role(String role) {
            this.role = role;
            return this;
        }

        public JobResponseDTOBuilder location(String location) {
            this.location = location;
            return this;
        }

        public JobResponseDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public JobResponseDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public JobResponseDTO build() {
            return new JobResponseDTO(jobId, companyName, role, location, description, status);
        }
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
