package com.ramirucompany.cosc022w.smart.campus.project.models;

/**
 * Represents a historical reading from a sensor.
 */
public class SensorReading {

    private String id;
    private long timestamp;
    private Double value;

    public SensorReading() {
    }

    public SensorReading(String id, long timestamp, Double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}