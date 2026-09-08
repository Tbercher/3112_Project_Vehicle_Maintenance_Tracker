package com.vehiclelog;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;
    private final FileStorageService fileStorageService;
    private final VehicleService vehicleService;

    public MaintenanceController(MaintenanceService maintenanceService, FileStorageService fileStorageService,
                                  VehicleService vehicleService) {
        this.maintenanceService = maintenanceService;
        this.fileStorageService = fileStorageService;
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public List<MaintenanceRecord> all() {
        return maintenanceService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceRecord create(@Valid @RequestBody MaintenanceRequest request) {
        return maintenanceService.create(vehicleService.findById(request.vehicleId()), request.serviceDate(),
            request.mileage(), request.description(), request.documentPath());
    }

    @PutMapping("/{id}")
    public MaintenanceRecord update(@PathVariable Long id, @Valid @RequestBody MaintenanceRequest request) {
        return maintenanceService.update(id, vehicleService.findById(request.vehicleId()), request.serviceDate(),
            request.mileage(), request.description(), request.documentPath());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        maintenanceService.delete(id);
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        return fileStorageService.store(file);
    }
}