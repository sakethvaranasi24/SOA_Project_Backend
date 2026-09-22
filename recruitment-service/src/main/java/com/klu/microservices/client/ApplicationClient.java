package com.klu.microservices.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "APPLICATION-SERVICE")
public interface ApplicationClient {

    @PutMapping("/applications/{id}/status")
    void updateApplicationStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> statusBody);
}
