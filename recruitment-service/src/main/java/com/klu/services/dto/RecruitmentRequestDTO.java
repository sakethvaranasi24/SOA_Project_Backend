package com.klu.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RecruitmentRequestDTO {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotBlank(message = "Interview date is required")
    private String interviewDate;

    @NotBlank(message = "Interviewer name is required")
    private String interviewerName;

    private String meetingLink;
    private String notes;

    public RecruitmentRequestDTO() {
    }

    public RecruitmentRequestDTO(Long applicationId, String interviewDate, String interviewerName, String meetingLink, String notes) {
        this.applicationId = applicationId;
        this.interviewDate = interviewDate;
        this.interviewerName = interviewerName;
        this.meetingLink = meetingLink;
        this.notes = notes;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
