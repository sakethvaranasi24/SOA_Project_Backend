package com.klu.services.client;

import com.klu.services.dto.JobDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "JOB-SERVICE")
public interface JobClient {

    @GetMapping("/jobs/{id}")
    JobDTO getJobById(@PathVariable("id") Long id);
}
