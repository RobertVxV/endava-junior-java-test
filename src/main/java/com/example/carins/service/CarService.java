package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final InsuranceClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, InsuranceClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (carId == null || date == null) return false;
        // TODO: optionally throw NotFound if car does not exist
        if (carRepository.findById(carId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found with id: " + carId);
        }
        return policyRepository.existsActiveOnDate(carId, date);
    }

    public Car saveCar(Car car) {
        if (carRepository.findByVin(car.getVin()).isPresent()) {
            throw new IllegalArgumentException("VIN already exists: " + car.getVin());
        }
        return carRepository.save(car);
    }

    public InsuranceClaim registerClaim(Long carId, LocalDate claimDate, String description, BigDecimal amount) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found with id: " + carId);
        }

        boolean valid = this.isInsuranceValid(carId, claimDate);
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No active insurance policy found for car on claim date: " + claimDate);
        }

        Car car = carOpt.get();
        InsuranceClaim claim = new InsuranceClaim(car, claimDate, description, amount);
        return claimRepository.save(claim);
    }
}
