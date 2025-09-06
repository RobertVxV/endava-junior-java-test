package com.example.carins.web;

import com.example.carins.exception.CarNotFoundException;
import com.example.carins.exception.InvalidInsuranceException;
import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.service.CarService;
import com.example.carins.web.utilities.DateValidator;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.CarHistoryEventDto;
import com.example.carins.web.dto.InsuranceClaimDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    private final DateValidator dateValidator;

    public CarController(CarService service, DateValidator dateValidator) {
        this.service = service;
        this.dateValidator = dateValidator;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId, @RequestParam String date) {
        LocalDate parsedDate = dateValidator.validateAndParse(date);
        boolean valid = false;
        try {
            valid = service.isInsuranceValid(carId, parsedDate);
        } catch (CarNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, parsedDate.toString(), valid));
    }

    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<?> claimCar(@PathVariable Long carId, @RequestParam String claimDate, @RequestParam String description, @RequestParam String amount) {
        LocalDate parsedClaimDate = dateValidator.validateAndParse(claimDate);
        if (description == null || description.trim().length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Description is required and must be at least 10 characters");
        }
        if (description.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Description must not exceed 1000 characters");
        }

        BigDecimal parsedAmount;
        try {
            parsedAmount = new BigDecimal(amount);
            if (parsedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Amount must be positive");
            }
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid amount format. Use decimal number (e.g., 123.45)");
        }

        InsuranceClaim claim;
        try {
            claim = service.registerClaim(carId, parsedClaimDate, description.trim(), parsedAmount);
        } catch (CarNotFoundException | InvalidInsuranceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        InsuranceClaimDto claimDto = toClaimDto(claim);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(claim.getId())
                .toUri();

        return ResponseEntity.created(location).body(claimDto);
    }

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    private InsuranceClaimDto toClaimDto(InsuranceClaim claim) {
        Car car = claim.getCar();
        return new InsuranceClaimDto(
                car.getId(),
                car.getVin(),
                claim.getClaimDate(),
                claim.getDescription(),
                claim.getAmount()
        );
    }

    @GetMapping("/cars/{carId}/history")
    public List<CarHistoryEventDto> getCarHistory(@PathVariable Long carId) {
        return service.getCarHistory(carId);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {
    }

    public record InsuranceClaimResponse(Long carId, LocalDate claimDate, String description, BigDecimal amount) {
    }
}
