package com.example.carins.exception;

public class DuplicateVinException extends RuntimeException {
    private final String vin;

    public DuplicateVinException(String vin) {
        super("VIN already exists: " + vin);
        this.vin = vin;
    }

    public String getVin() {
        return vin;
    }
}