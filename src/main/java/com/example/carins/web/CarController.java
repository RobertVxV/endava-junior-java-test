package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.InsuranceClaimDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    public CarController(CarService service) {
        this.service = service;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId, @RequestParam String date) {
        // TODO: validate date format and handle errors consistently
        try{
            LocalDate d = LocalDate.parse(date);
            boolean valid = service.isInsuranceValid(carId, d);
            return ResponseEntity.ok(new InsuranceValidityResponse(carId, d.toString(), valid));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use YYYY-MM-DD");
        }
    }

    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<?> claimCar(@PathVariable Long carId, @RequestParam String claimDate, @RequestParam String description, @RequestParam String amount) {
        LocalDate parsedClaimDate;
        try {
            parsedClaimDate = LocalDate.parse(claimDate);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use YYYY-MM-DD");
        }
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

        InsuranceClaim claim = service.registerClaim(carId, parsedClaimDate, description.trim(), parsedAmount);

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

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
    public record InsuranceClaimResponse(Long carId, LocalDate claimDate, String description, BigDecimal amount) {}
}
