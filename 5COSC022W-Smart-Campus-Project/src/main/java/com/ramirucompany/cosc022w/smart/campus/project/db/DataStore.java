package com.ramirucompany.cosc022w.smart.campus.project.db;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.ramirucompany.cosc022w.smart.campus.project.models.Room;
import com.ramirucompany.cosc022w.smart.campus.project.models.Sensor;
import com.ramirucompany.cosc022w.smart.campus.project.models.SensorReading;

/**
 * Thread-safe in-memory store used by all request-scoped resources.
 */
public final class DataStore {

    public enum DeleteRoomResult {
        DELETED,
        NOT_FOUND,
        HAS_ATTACHED_SENSORS
    }

    private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> SENSORS = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> READINGS_BY_SENSOR = new ConcurrentHashMap<>();

    private static final AtomicLong ROOM_IDS = new AtomicLong(0);
    private static final AtomicLong SENSOR_IDS = new AtomicLong(0);

    private DataStore() {
    }

    public static List<Room> listRooms() {
        return ROOMS.values()
                .stream()
                .map(DataStore::copyRoom)
                .sorted(Comparator.comparing(Room::getId))
                .collect(Collectors.toList());
    }

    public static Room getRoom(String roomId) {
        Room room = ROOMS.get(roomId);
        return room == null ? null : copyRoom(room);
    }

    public static boolean roomExists(String roomId) {
        return ROOMS.containsKey(roomId);
    }

    public static synchronized Room createRoom(Room request) {
        Room room = new Room();
        room.setId("ROOM-" + ROOM_IDS.incrementAndGet());
        room.setName(request.getName().trim());
        room.setCapacity(request.getCapacity());
        room.setSensorIds(new CopyOnWriteArrayList<>());

        ROOMS.put(room.getId(), room);
        return copyRoom(room);
    }

    public static synchronized DeleteRoomResult deleteRoom(String roomId) {
        Room room = ROOMS.get(roomId);
        if (room == null) {
            return DeleteRoomResult.NOT_FOUND;
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            return DeleteRoomResult.HAS_ATTACHED_SENSORS;
        }

        ROOMS.remove(roomId);
        return DeleteRoomResult.DELETED;
    }

    public static List<Sensor> listSensors(String typeFilter) {
        String normalizedFilter = normalize(typeFilter);

        return SENSORS.values()
                .stream()
                .filter(sensor -> matchesTypeFilter(sensor, normalizedFilter))
                .map(DataStore::copySensor)
                .sorted(Comparator.comparing(Sensor::getId))
                .collect(Collectors.toList());
    }

    public static Sensor getSensor(String sensorId) {
        Sensor sensor = SENSORS.get(sensorId);
        return sensor == null ? null : copySensor(sensor);
    }

    public static boolean sensorExists(String sensorId) {
        return SENSORS.containsKey(sensorId);
    }

    public static synchronized Sensor createSensor(Sensor request) {
        Sensor sensor = new Sensor();
        sensor.setId("SENSOR-" + SENSOR_IDS.incrementAndGet());
        sensor.setType(request.getType().trim());
        sensor.setRoomId(request.getRoomId());
        sensor.setCurrentValue(request.getCurrentValue());
        sensor.setStatus(normalizeStatus(request.getStatus()));

        SENSORS.put(sensor.getId(), sensor);
        READINGS_BY_SENSOR.put(sensor.getId(), new CopyOnWriteArrayList<>());

        Room room = ROOMS.get(sensor.getRoomId());
        if (room != null) {
            if (!room.getSensorIds().contains(sensor.getId())) {
                room.getSensorIds().add(sensor.getId());
            }
        }

        return copySensor(sensor);
    }

    public static List<SensorReading> listReadings(String sensorId) {
        List<SensorReading> readings = READINGS_BY_SENSOR.get(sensorId);
        if (readings == null) {
            return new ArrayList<>();
        }

        return readings.stream()
                .map(DataStore::copyReading)
                .sorted(Comparator.comparing(SensorReading::getTimestamp))
                .collect(Collectors.toList());
    }

    public static synchronized SensorReading createReading(String sensorId, SensorReading request) {
        Sensor sensor = SENSORS.get(sensorId);
        if (sensor == null) {
            return null;
        }

        SensorReading reading = new SensorReading();
        reading.setId(UUID.randomUUID().toString());
        reading.setValue(request.getValue());

        if (request.getTimestamp() <= 0) {
            reading.setTimestamp(Instant.now().toEpochMilli());
        } else {
            reading.setTimestamp(request.getTimestamp());
        }

        READINGS_BY_SENSOR
                .computeIfAbsent(sensorId, key -> new CopyOnWriteArrayList<SensorReading>())
                .add(reading);

        sensor.setCurrentValue(reading.getValue());

        return copyReading(reading);
    }

    private static boolean matchesTypeFilter(Sensor sensor, String normalizedFilter) {
        if (normalizedFilter == null || normalizedFilter.isEmpty()) {
            return true;
        }

        String sensorType = normalize(sensor.getType());
        return normalizedFilter.equals(sensorType);
    }

    private static String normalize(String input) {
        return input == null ? null : input.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeStatus(String inputStatus) {
        if (inputStatus == null || inputStatus.trim().isEmpty()) {
            return "active";
        }

        return inputStatus.trim().toLowerCase(Locale.ROOT);
    }

    private static Room copyRoom(Room source) {
        return new Room(
                source.getId(),
                source.getName(),
                source.getCapacity(),
                source.getSensorIds()
        );
    }

    private static Sensor copySensor(Sensor source) {
        return new Sensor(
                source.getId(),
                source.getType(),
                source.getStatus(),
                source.getCurrentValue(),
                source.getRoomId()
        );
    }

    private static SensorReading copyReading(SensorReading source) {
        return new SensorReading(
                source.getId(),
                source.getTimestamp(),
                source.getValue()
        );
    }
}