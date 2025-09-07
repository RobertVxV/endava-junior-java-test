package com.example.carins.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "expired_policy_logger")
public class ExpiredPolicyLogger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long policyId;

    @Column(nullable = false)
    private LocalDateTime loggedAt;

    public ExpiredPolicyLogger(){}

    public ExpiredPolicyLogger(Long policyId) {
        this.policyId = policyId;
        this.loggedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }
}
