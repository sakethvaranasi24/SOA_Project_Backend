package com.klu.services.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recruitments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recruitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recruitmentId;

    @Column(nullable = false)
    private Long applicationId;

    @Builder.Default
    private String interviewStatus = "SCHEDULED";

    @Builder.Default
    private String finalStatus = "PENDING";

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

    public static RecruitmentBuilder builder() {
        return new RecruitmentBuilder();
    }

    public static class RecruitmentBuilder {
        private Long recruitmentId;
        private Long applicationId;
        private String interviewStatus = "SCHEDULED";
        private String finalStatus = "PENDING";

        public RecruitmentBuilder recruitmentId(Long recruitmentId) { this.recruitmentId = recruitmentId; return this; }
        public RecruitmentBuilder applicationId(Long applicationId) { this.applicationId = applicationId; return this; }
        public RecruitmentBuilder interviewStatus(String interviewStatus) { this.interviewStatus = interviewStatus; return this; }
        public RecruitmentBuilder finalStatus(String finalStatus) { this.finalStatus = finalStatus; return this; }

        public Recruitment build() {
            Recruitment r = new Recruitment();
            r.recruitmentId = this.recruitmentId;
            r.applicationId = this.applicationId;
            r.interviewStatus = this.interviewStatus;
            r.finalStatus = this.finalStatus;
            return r;
        }
    }
}
