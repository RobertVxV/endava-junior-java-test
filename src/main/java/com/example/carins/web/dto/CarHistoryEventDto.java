package com.example.carins.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CarHistoryEventDto(
        String eventType,        // "INSURANCE_POLICY_START", "INSURANCE_POLICY_END", "INSURANCE_CLAIM"
        LocalDate date,
        String description,
        BigDecimal amount,
        String provider
) {}