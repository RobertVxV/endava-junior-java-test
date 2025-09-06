package com.example.carins.exception;

import java.time.LocalDate;

public class InvalidInsuranceException extends RuntimeException {
    private final Long carId;
    private final LocalDate claimDate;

    public InvalidInsuranceException(Long carId, LocalDate claimDate) {
        super("No active insurance policy found for car " + carId + " on date: " + claimDate);
        this.carId = carId;
        this.claimDate = claimDate;
    }

    public Long getCarId() {
        return carId;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }
}
