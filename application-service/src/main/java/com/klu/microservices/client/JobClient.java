package com.klu.microservices.client;

import com.klu.microservices.dto.JobDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "JOB-SERVICE")
public interface JobClient {

    @GetMapping("/jobs/{id}")
    JobDTO getJobById(@PathVariable("id") Long id);
}
