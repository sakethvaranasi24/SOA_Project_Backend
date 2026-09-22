package com.klu.microservices.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Builder.Default
    private String status = "APPLIED";
}
