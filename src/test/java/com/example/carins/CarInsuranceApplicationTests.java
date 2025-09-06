package com.example.carins;

import com.example.carins.exception.CarNotFoundException;
import com.example.carins.service.CarService;
import com.example.carins.web.utilities.DateValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CarInsuranceApplicationTests {

    @Autowired
    CarService service;

    @Test
    void validInsurance() {
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2024-06-01")));
    }

    @Test
    void invalidInsurance() {
        assertFalse(service.isInsuranceValid(2L, LocalDate.parse("2025-02-01")));
    }

    @Test
    void carNotFound() {
        assertThrows(CarNotFoundException.class,
                () -> service.isInsuranceValid(999L, LocalDate.parse("2024-06-01")));
    }

    @Test
    void nullInputs() {
        assertFalse(service.isInsuranceValid(null, LocalDate.parse("2024-06-01")));
        assertFalse(service.isInsuranceValid(1L, null));
    }

    @Autowired
    DateValidator validator;

    @Test
    void validDate() {
        LocalDate result = validator.validateAndParse("2024-06-15");
        assertEquals(LocalDate.of(2024, 6, 15), result);
    }

    @Test
    void nullDate() {
        assertThrows(ResponseStatusException.class,
                () -> validator.validateAndParse(null));
    }

    @Test
    void invalidFormat() {
        assertThrows(ResponseStatusException.class,
                () -> validator.validateAndParse("2024/06/15"));
    }

    @Test
    void dateOutOfRange() {
        assertThrows(ResponseStatusException.class,
                () -> validator.validateAndParse("1800-01-01"));
        assertThrows(ResponseStatusException.class,
                () -> validator.validateAndParse("2100-01-01"));
    }
}
