package com.klu.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class InterviewUpdateDTO {

    @NotBlank(message = "Interview status is required")
    @Pattern(regexp = "^(SCHEDULED|COMPLETED|CANCELLED)$", message = "Interview status must be SCHEDULED, COMPLETED, or CANCELLED")
    private String interviewStatus;

    private String notes;

    public InterviewUpdateDTO() {
    }

    public InterviewUpdateDTO(String interviewStatus, String notes) {
        this.interviewStatus = interviewStatus;
        this.notes = notes;
    }

    public String getInterviewStatus() {
        return interviewStatus;
    }

    public void setInterviewStatus(String interviewStatus) {
        this.interviewStatus = interviewStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
