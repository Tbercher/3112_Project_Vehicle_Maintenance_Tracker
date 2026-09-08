package com.vehiclelog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VehicleService {
    private final VehicleRepository repository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public VehicleService(VehicleRepository repository, MaintenanceRecordRepository maintenanceRecordRepository) {
        this.repository = repository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findAll() {
        return repository.findAll().stream()
                .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                .toList();
    }

    public Vehicle create(String name) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Enter a name for the vehicle.");
        }
        if (repository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("That vehicle is already in your garage.");
        }
        return repository.save(new Vehicle(normalizedName));
    }

    @Transactional(readOnly = true)
    public Vehicle findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("The selected vehicle was not found."));
    }

    public void delete(Long id) {
        findById(id);
        maintenanceRecordRepository.deleteByVehicleId(id);
        repository.deleteById(id);
    }
}
