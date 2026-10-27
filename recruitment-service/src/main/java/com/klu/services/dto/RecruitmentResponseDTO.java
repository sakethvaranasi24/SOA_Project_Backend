package com.klu.services.dto;

import com.klu.services.model.Recruitment;

public class RecruitmentResponseDTO {

    private Long recruitmentId;
    private Long applicationId;
    private String interviewDate;
    private String interviewerName;
    private String meetingLink;
    private String notes;
    private String interviewStatus;
    private String finalStatus;

    public RecruitmentResponseDTO() {
    }

    public RecruitmentResponseDTO(Long recruitmentId, Long applicationId, String interviewDate, String interviewerName, String meetingLink, String notes, String interviewStatus, String finalStatus) {
        this.recruitmentId = recruitmentId;
        this.applicationId = applicationId;
        this.interviewDate = interviewDate;
        this.interviewerName = interviewerName;
        this.meetingLink = meetingLink;
        this.notes = notes;
        this.interviewStatus = interviewStatus;
        this.finalStatus = finalStatus;
    }

    public static RecruitmentResponseDTO fromEntity(Recruitment r) {
        if (r == null) return null;
        return new RecruitmentResponseDTO(
                r.getRecruitmentId(),
                r.getApplicationId(),
                r.getInterviewDate(),
                r.getInterviewerName(),
                r.getMeetingLink(),
                r.getNotes(),
                r.getInterviewStatus(),
                r.getFinalStatus()
        );
    }

    public Long getRecruitmentId() {
        return recruitmentId;
    }

    public void setRecruitmentId(Long recruitmentId) {
        this.recruitmentId = recruitmentId;
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

    public String getInterviewStatus() {
        return interviewStatus;
    }

    public void setInterviewStatus(String interviewStatus) {
        this.interviewStatus = interviewStatus;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }
}
