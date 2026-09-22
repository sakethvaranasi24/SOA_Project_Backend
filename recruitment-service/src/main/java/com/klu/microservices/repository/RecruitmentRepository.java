package com.klu.microservices.repository;

import com.klu.microservices.model.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
    Optional<Recruitment> findByApplicationId(Long applicationId);
}
