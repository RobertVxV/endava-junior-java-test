package com.example.carins.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InsuranceClaimDto(
        Long carId,
        String vin,
        LocalDate claimDate,
        String description,
        BigDecimal amount
) {
}
