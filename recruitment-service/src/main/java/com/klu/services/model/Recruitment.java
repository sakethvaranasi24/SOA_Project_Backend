package com.klu.services.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recruitments")
public class Recruitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recruitmentId;

    @Column(nullable = false)
    private Long applicationId;

    private String interviewDate;

    private String interviewerName;

    private String meetingLink;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private String interviewStatus = "SCHEDULED";

    @Column(nullable = false)
    private String finalStatus = "PENDING";

    public Recruitment() {
        this.interviewStatus = "SCHEDULED";
        this.finalStatus = "PENDING";
    }

    public Recruitment(Long recruitmentId, Long applicationId, String interviewDate, String interviewerName, String meetingLink, String notes, String interviewStatus, String finalStatus) {
        this.recruitmentId = recruitmentId;
        this.applicationId = applicationId;
        this.interviewDate = interviewDate;
        this.interviewerName = interviewerName;
        this.meetingLink = meetingLink;
        this.notes = notes;
        this.interviewStatus = (interviewStatus != null && !interviewStatus.trim().isEmpty()) ? interviewStatus : "SCHEDULED";
        this.finalStatus = (finalStatus != null && !finalStatus.trim().isEmpty()) ? finalStatus : "PENDING";
    }

    public static RecruitmentBuilder builder() {
        return new RecruitmentBuilder();
    }

    public static class RecruitmentBuilder {
        private Long recruitmentId;
        private Long applicationId;
        private String interviewDate;
        private String interviewerName;
        private String meetingLink;
        private String notes;
        private String interviewStatus = "SCHEDULED";
        private String finalStatus = "PENDING";

        public RecruitmentBuilder recruitmentId(Long recruitmentId) {
            this.recruitmentId = recruitmentId;
            return this;
        }

        public RecruitmentBuilder applicationId(Long applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public RecruitmentBuilder interviewDate(String interviewDate) {
            this.interviewDate = interviewDate;
            return this;
        }

        public RecruitmentBuilder interviewerName(String interviewerName) {
            this.interviewerName = interviewerName;
            return this;
        }

        public RecruitmentBuilder meetingLink(String meetingLink) {
            this.meetingLink = meetingLink;
            return this;
        }

        public RecruitmentBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public RecruitmentBuilder interviewStatus(String interviewStatus) {
            if (interviewStatus != null && !interviewStatus.trim().isEmpty()) {
                this.interviewStatus = interviewStatus;
            }
            return this;
        }

        public RecruitmentBuilder finalStatus(String finalStatus) {
            if (finalStatus != null && !finalStatus.trim().isEmpty()) {
                this.finalStatus = finalStatus;
            }
            return this;
        }

        public Recruitment build() {
            return new Recruitment(recruitmentId, applicationId, interviewDate, interviewerName, meetingLink, notes, interviewStatus, finalStatus);
        }
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
