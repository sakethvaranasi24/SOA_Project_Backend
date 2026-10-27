package com.klu.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class JobRequestDTO {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Location is required")
    private String location;

    private String description;

    @Pattern(regexp = "^(OPEN|CLOSED)?$", message = "Status must be either OPEN or CLOSED")
    private String status;

    public JobRequestDTO() {
    }

    public JobRequestDTO(String companyName, String role, String location, String description, String status) {
        this.companyName = companyName;
        this.role = role;
        this.location = location;
        this.description = description;
        this.status = status;
    }

    public static JobRequestDTOBuilder builder() {
        return new JobRequestDTOBuilder();
    }

    public static class JobRequestDTOBuilder {
        private String companyName;
        private String role;
        private String location;
        private String description;
        private String status;

        public JobRequestDTOBuilder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public JobRequestDTOBuilder role(String role) {
            this.role = role;
            return this;
        }

        public JobRequestDTOBuilder location(String location) {
            this.location = location;
            return this;
        }

        public JobRequestDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public JobRequestDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public JobRequestDTO build() {
            return new JobRequestDTO(companyName, role, location, description, status);
        }
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
