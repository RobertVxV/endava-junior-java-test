package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.ExpiredPolicyLogger;
import com.example.carins.repo.ExpiredPolicyLoggerRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpiredPolicyScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ExpiredPolicyScheduler.class);

    private final InsurancePolicyRepository policyRepository;
    private final ExpiredPolicyLoggerRepository loggedPolicyRepository;

    public ExpiredPolicyScheduler(InsurancePolicyRepository policyRepository,
                                  ExpiredPolicyLoggerRepository loggedPolicyRepository) {
        this.policyRepository = policyRepository;
        this.loggedPolicyRepository = loggedPolicyRepository;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes in milliseconds
    @Transactional
    public void logExpiredPolicies() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<InsurancePolicy> expiredPolicies = policyRepository.findPoliciesExpiringOnDate(yesterday);

        for (InsurancePolicy policy : expiredPolicies) {
            if (!loggedPolicyRepository.existsByPolicyId(policy.getId())) {
                logger.info("Policy {} for car {} expired on {}",
                        policy.getId(),
                        policy.getCar().getId(),
                        policy.getEndDate());

                loggedPolicyRepository.save(new ExpiredPolicyLogger(policy.getId()));
            }
        }
    }
}