package com.ramirucompany.cosc022w.smart.campus.project.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical room in the campus.
 */
public class Room {

    private Long id;
    private String name;
    private Integer capacity;
    private List<String> sensorIds;

    public Room() {
        this.sensorIds = new ArrayList<>();
    }

    public Room(Long id, String name, Integer capacity, List<String> sensorIds) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        if (sensorIds == null) {
            this.sensorIds = new ArrayList<>();
        } else {
            this.sensorIds = new ArrayList<>(sensorIds);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        if (sensorIds == null) {
            this.sensorIds = new ArrayList<>();
        } else {
            this.sensorIds = new ArrayList<>(sensorIds);
        }
    }
}