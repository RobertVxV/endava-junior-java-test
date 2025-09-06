package com.example.carins.service;

import com.example.carins.exception.CarNotFoundException;
import com.example.carins.exception.DuplicateVinException;
import com.example.carins.exception.InvalidInsuranceException;
import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.CarHistoryEventDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
        if (carRepository.findById(carId).isEmpty()) {
            throw new CarNotFoundException(carId);
        }
        return policyRepository.existsActiveOnDate(carId, date);
    }

    public Car saveCar(Car car) {
        if (carRepository.findByVin(car.getVin()).isPresent()) {
            throw new DuplicateVinException(car.getVin());
        }
        return carRepository.save(car);
    }

    public InsuranceClaim registerClaim(Long carId, LocalDate claimDate, String description, BigDecimal amount) {

        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) {
            throw new CarNotFoundException(carId);
        }

        boolean valid = this.isInsuranceValid(carId, claimDate);
        if (!valid) {
            throw new InvalidInsuranceException(carId, claimDate);
        }

        Car car = carOpt.get();
        InsuranceClaim claim = new InsuranceClaim(car, claimDate, description, amount);
        return claimRepository.save(claim);
    }

    public List<CarHistoryEventDto> getCarHistory(Long carId) {
        if (carRepository.findById(carId).isEmpty()) {
            throw new CarNotFoundException(carId);
        }

        List<CarHistoryEventDto> events = new ArrayList<>();

        List<InsurancePolicy> policies = policyRepository.findByCarId(carId);

        for (InsurancePolicy policy : policies) {
            events.add(new CarHistoryEventDto(
                    "INSURANCE_POLICY_START",
                    policy.getStartDate(),
                    String.format("Insurance policy started with %s (valid until %s)",
                            policy.getProvider(), policy.getEndDate()),
                    null,
                    policy.getProvider()
            ));

            if (policy.getEndDate().isBefore(LocalDate.now())) {
                events.add(new CarHistoryEventDto(
                        "INSURANCE_POLICY_END",
                        policy.getEndDate(),
                        String.format("Insurance policy with %s expired", policy.getProvider()),
                        null,
                        policy.getProvider()
                ));
            }
        }

        List<InsuranceClaim> claims = claimRepository.findByCarIdOrderByClaimDateAsc(carId);

        for (InsuranceClaim claim : claims) {
            String providerName = "Unknown";
            for (InsurancePolicy policy : policies) {
                if (!claim.getClaimDate().isBefore(policy.getStartDate()) &&
                        !claim.getClaimDate().isAfter(policy.getEndDate())) {
                    providerName = policy.getProvider();
                    break;
                }
            }

            events.add(new CarHistoryEventDto(
                    "INSURANCE_CLAIM",
                    claim.getClaimDate(),
                    claim.getDescription(),
                    claim.getAmount(),
                    providerName
            ));
        }

        events.sort(Comparator.comparing(CarHistoryEventDto::date));

        return events;
    }
}
