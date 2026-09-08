package com.vehiclelog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MaintenanceService {
    private final MaintenanceRecordRepository repository;

    public MaintenanceService(MaintenanceRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecord> findAll() {
        return repository.findAllByOrderByServiceDateDesc();
    }

    public List<MaintenanceRecord> findAll(Vehicle vehicle) {
        return repository.findAllByVehicleIdOrderByServiceDateDesc(vehicle.getId());
    }

    public MaintenanceRecord create(Vehicle vehicle, LocalDate date, Integer mileage, String description, String documentPath) {
        validate(vehicle, date, mileage, description);
        return repository.save(new MaintenanceRecord(vehicle, date, mileage, description.trim(), documentPath));
    }

    public MaintenanceRecord update(Long id, Vehicle vehicle, LocalDate date, Integer mileage, String description, String documentPath) {
        validate(vehicle, date, mileage, description);
        MaintenanceRecord record = repository.findById(id).orElseThrow();
        record.update(vehicle, date, mileage, description.trim(), documentPath);
        return repository.save(record);
    }

    private void validate(Vehicle vehicle, LocalDate date, Integer mileage, String description) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Select a vehicle first.");
        }
        if (date == null || mileage == null || mileage < 0 || description == null || description.isBlank()) {
            throw new IllegalArgumentException("Date, mileage, and service description are required.");
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
