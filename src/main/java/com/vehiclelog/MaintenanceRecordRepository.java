package com.vehiclelog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
	List<MaintenanceRecord> findAllByOrderByServiceDateDesc();
	List<MaintenanceRecord> findAllByVehicleIdOrderByServiceDateDesc(Long vehicleId);
	long deleteByVehicleId(Long vehicleId);
}
