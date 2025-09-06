package com.example.carins.repo;

import com.example.carins.model.Car;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    // TODO: enforce unique VIN at DB and via validation (exercise)
    @EntityGraph(attributePaths = {"owner"})
    List<Car> findAll();
    Optional<Car> findByVin(String vin);
    Optional<Car> findById(Long id);
}