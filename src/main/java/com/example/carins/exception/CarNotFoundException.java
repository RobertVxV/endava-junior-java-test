package com.example.carins.exception;

public class CarNotFoundException extends RuntimeException {
    private final Long carId;

    public CarNotFoundException(Long carId) {
        super("Car not found with id: " + carId);
        this.carId = carId;
    }

    public Long getCarId() {
        return carId;
    }
}
