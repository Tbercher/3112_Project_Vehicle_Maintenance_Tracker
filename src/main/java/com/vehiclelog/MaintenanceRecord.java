package com.vehiclelog;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private LocalDate serviceDate;
    private Integer mileage;
    private String description;
    private String documentPath;

    protected MaintenanceRecord() {
    }

    public MaintenanceRecord(Vehicle vehicle, LocalDate serviceDate, Integer mileage, String description, String documentPath) {
        this.vehicle = vehicle;
        this.serviceDate = serviceDate;
        this.mileage = mileage;
        this.description = description;
        this.documentPath = documentPath;
    }

    public Long getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public LocalDate getServiceDate() { return serviceDate; }
    public Integer getMileage() { return mileage; }
    public String getDescription() { return description; }
    public String getDocumentPath() { return documentPath; }

    public void update(Vehicle vehicle, LocalDate serviceDate, Integer mileage, String description, String documentPath) {
        this.vehicle = vehicle;
        this.serviceDate = serviceDate;
        this.mileage = mileage;
        this.description = description;
        this.documentPath = documentPath;
    }
}
