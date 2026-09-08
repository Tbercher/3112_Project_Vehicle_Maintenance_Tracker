package com.vehiclelog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MaintenanceRequest(
        @NotNull Long vehicleId,
        @NotNull LocalDate serviceDate,
        @NotNull @Min(0) Integer mileage,
        @NotBlank String description,
        String documentPath) {
}
