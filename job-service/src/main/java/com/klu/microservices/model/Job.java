package com.klu.microservices.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Builder.Default
    private String status = "OPEN";
}
