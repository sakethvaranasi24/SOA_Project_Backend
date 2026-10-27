package com.klu.services.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String role;

    private String location;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String status = "OPEN";

    public Job() {
        this.status = "OPEN";
    }

    public Job(Long jobId, String companyName, String role, String location, String description, String status) {
        this.jobId = jobId;
        this.companyName = companyName;
        this.role = role;
        this.location = location;
        this.description = description;
        this.status = (status != null && !status.trim().isEmpty()) ? status : "OPEN";
    }

    public static JobBuilder builder() {
        return new JobBuilder();
    }

    public static class JobBuilder {
        private Long jobId;
        private String companyName;
        private String role;
        private String location;
        private String description;
        private String status = "OPEN";

        public JobBuilder jobId(Long jobId) {
            this.jobId = jobId;
            return this;
        }

        public JobBuilder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public JobBuilder role(String role) {
            this.role = role;
            return this;
        }

        public JobBuilder location(String location) {
            this.location = location;
            return this;
        }

        public JobBuilder description(String description) {
            this.description = description;
            return this;
        }

        public JobBuilder status(String status) {
            if (status != null && !status.trim().isEmpty()) {
                this.status = status;
            }
            return this;
        }

        public Job build() {
            return new Job(jobId, companyName, role, location, description, status);
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
