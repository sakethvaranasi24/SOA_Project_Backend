package com.klu.microservices.model;

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
}
