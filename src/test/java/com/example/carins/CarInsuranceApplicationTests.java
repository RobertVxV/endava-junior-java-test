package com.example.carins;

import com.example.carins.exception.CarNotFoundException;
import com.example.carins.exception.InvalidInsuranceException;
import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarHistoryEventDto;
import com.example.carins.web.utilities.DateValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    @Test
    void listCarsReturnsNonNull() {
        List<Car> cars = service.listCars();
        assertNotNull(cars);
    }

    @Test
    void registerClaimWithValidInsurance() {
        LocalDate claimDate = LocalDate.parse("2024-06-01");
        String description = "Minor fender bender in parking lot";
        BigDecimal amount = new BigDecimal("1500.00");

        InsuranceClaim claim = service.registerClaim(1L, claimDate, description, amount);
        assertNotNull(claim);
        assertEquals(description, claim.getDescription());
        assertEquals(amount, claim.getAmount());
        assertEquals(claimDate, claim.getClaimDate());
    }

    @Test
    void registerClaimWithInvalidInsurance() {
        LocalDate claimDate = LocalDate.parse("2023-02-01");
        String description = "Test claim description";
        BigDecimal amount = new BigDecimal("1000.00");

        assertThrows(InvalidInsuranceException.class,
                () -> service.registerClaim(2L, claimDate, description, amount));
    }

    @Test
    void registerClaimForNonexistentCar() {
        LocalDate claimDate = LocalDate.parse("2024-06-01");
        String description = "Test claim description";
        BigDecimal amount = new BigDecimal("1000.00");

        assertThrows(CarNotFoundException.class,
                () -> service.registerClaim(999L, claimDate, description, amount));
    }

    @Test
    void getCarHistoryForExistingCar() {
        List<CarHistoryEventDto> history = service.getCarHistory(1L);
        assertNotNull(history);
    }

    @Test
    void getCarHistoryForNonexistentCar() {
        assertThrows(CarNotFoundException.class,
                () -> service.getCarHistory(999L));
    }

    @Test
    void carHistoryEventsAreSortedByDate() {
        List<CarHistoryEventDto> history = service.getCarHistory(1L);

        for (int i = 0; i < history.size() - 1; i++) {
            LocalDate currentDate = history.get(i).date();
            LocalDate previousDate = history.get(i + 1).date();
            assertTrue(currentDate.isAfter(previousDate) || currentDate.isEqual(previousDate),
                    "History events should be sorted by date");
        }
    }

    @Test
    void emptyStringDate() {
        assertThrows(ResponseStatusException.class,
                () -> validator.validateAndParse(""));
    }

    @Test
    void whitespaceOnlyDate() {
        assertThrows(ResponseStatusException.class,
                () -> validator.validateAndParse("   "));
    }

    @Test
    void validDateAtBoundary() {
        LocalDate minDate = validator.validateAndParse("1900-01-01");
        assertEquals(LocalDate.of(1900, 1, 1), minDate);

        LocalDate futureDate = LocalDate.now().plusYears(50);
        LocalDate maxDate = validator.validateAndParse(futureDate.toString());
        assertEquals(futureDate, maxDate);
    }

    @Test
    void invalidDateFormats() {
        assertThrows(ResponseStatusException.class,
                () -> validator.validateAndParse("06/15/2024"));

        assertThrows(ResponseStatusException.class,
                () -> validator.validateAndParse("2024-6-15"));

        assertThrows(ResponseStatusException.class,
                () -> validator.validateAndParse("15-06-2024"));

        assertThrows(ResponseStatusException.class,
                () -> validator.validateAndParse("not-a-date"));
    }
}

