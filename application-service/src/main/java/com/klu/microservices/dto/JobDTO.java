package com.klu.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private Long jobId;
    private String companyName;
    private String role;
    private String location;
    private String description;
    private String status;
}
