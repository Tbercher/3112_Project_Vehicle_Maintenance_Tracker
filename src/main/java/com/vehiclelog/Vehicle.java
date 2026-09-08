package com.vehiclelog;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected Vehicle() {
    }

    public Vehicle(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name;
    }
}
