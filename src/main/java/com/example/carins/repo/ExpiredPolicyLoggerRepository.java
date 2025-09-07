package com.example.carins.repo;

import com.example.carins.model.ExpiredPolicyLogger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpiredPolicyLoggerRepository extends JpaRepository<ExpiredPolicyLogger, Long> {
    boolean existsByPolicyId(Long policyId);
}