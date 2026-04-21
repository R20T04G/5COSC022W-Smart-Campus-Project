package com.ramirucompany.cosc022w.smart.campus.project.models;

/**
 * Represents a historical reading from a sensor.
 */
public class SensorReading {

    private Long id;
    private String timestamp;
    private Double value;

    public SensorReading() {
    }

    public SensorReading(Long id, String timestamp, Double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}