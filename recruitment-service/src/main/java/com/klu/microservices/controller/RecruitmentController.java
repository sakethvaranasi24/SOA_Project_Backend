package com.klu.microservices.controller;

import com.klu.microservices.model.Recruitment;
import com.klu.microservices.service.RecruitmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/recruitments")
public class RecruitmentController {

    @Autowired
    private RecruitmentService recruitmentService;

    @PostMapping
    public ResponseEntity<Recruitment> createRecruitment(@RequestBody Recruitment recruitment) {
        Recruitment created = recruitmentService.createRecruitment(recruitment);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recruitment> getRecruitmentById(@PathVariable Long id) {
        return recruitmentService.getRecruitmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/interview")
    public ResponseEntity<Recruitment> updateInterviewStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("interviewStatus");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Recruitment updated = recruitmentService.updateInterviewStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/final-status")
    public ResponseEntity<Recruitment> updateFinalStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("finalStatus");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Recruitment updated = recruitmentService.updateFinalStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
