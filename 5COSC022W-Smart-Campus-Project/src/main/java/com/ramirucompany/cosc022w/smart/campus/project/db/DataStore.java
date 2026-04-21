package com.ramirucompany.cosc022w.smart.campus.project.db;

import com.ramirucompany.cosc022w.smart.campus.project.models.Room;
import com.ramirucompany.cosc022w.smart.campus.project.models.Sensor;
import com.ramirucompany.cosc022w.smart.campus.project.models.SensorReading;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory store used by all request-scoped resources.
 */
public final class DataStore {

    public enum DeleteRoomResult {
        DELETED,
        NOT_FOUND,
        HAS_ATTACHED_SENSORS
    }

    private static final Map<Long, Room> ROOMS = new ConcurrentHashMap<Long, Room>();
    private static final Map<Long, Sensor> SENSORS = new ConcurrentHashMap<Long, Sensor>();
    private static final Map<Long, List<SensorReading>> READINGS_BY_SENSOR = new ConcurrentHashMap<Long, List<SensorReading>>();

    private static final AtomicLong ROOM_IDS = new AtomicLong(0);
    private static final AtomicLong SENSOR_IDS = new AtomicLong(0);
    private static final AtomicLong READING_IDS = new AtomicLong(0);

    private DataStore() {
    }

    public static List<Room> listRooms() {
        return ROOMS.values()
                .stream()
                .map(DataStore::copyRoom)
                .sorted(Comparator.comparing(Room::getId))
                .collect(Collectors.toList());
    }

    public static Room getRoom(long roomId) {
        Room room = ROOMS.get(roomId);
        return room == null ? null : copyRoom(room);
    }

    public static boolean roomExists(long roomId) {
        return ROOMS.containsKey(roomId);
    }

    public static synchronized Room createRoom(Room request) {
        Room room = new Room();
        room.setId(ROOM_IDS.incrementAndGet());
        room.setName(request.getName().trim());
        room.setCapacity(request.getCapacity());
        room.setSensorIds(new CopyOnWriteArrayList<String>());

        ROOMS.put(room.getId(), room);
        return copyRoom(room);
    }

    public static synchronized DeleteRoomResult deleteRoom(long roomId) {
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

    public static Sensor getSensor(long sensorId) {
        Sensor sensor = SENSORS.get(sensorId);
        return sensor == null ? null : copySensor(sensor);
    }

    public static boolean sensorExists(long sensorId) {
        return SENSORS.containsKey(sensorId);
    }

    public static synchronized Sensor createSensor(Sensor request) {
        Sensor sensor = new Sensor();
        sensor.setId(SENSOR_IDS.incrementAndGet());
        sensor.setType(request.getType().trim());
        sensor.setRoomId(request.getRoomId());
        sensor.setCurrentValue(request.getCurrentValue());
        sensor.setStatus(normalizeStatus(request.getStatus()));

        SENSORS.put(sensor.getId(), sensor);
        READINGS_BY_SENSOR.put(sensor.getId(), new CopyOnWriteArrayList<SensorReading>());

        Room room = ROOMS.get(sensor.getRoomId());
        if (room != null) {
            String sensorIdText = String.valueOf(sensor.getId());
            if (!room.getSensorIds().contains(sensorIdText)) {
                room.getSensorIds().add(sensorIdText);
            }
        }

        return copySensor(sensor);
    }

    public static List<SensorReading> listReadings(long sensorId) {
        List<SensorReading> readings = READINGS_BY_SENSOR.get(sensorId);
        if (readings == null) {
            return new ArrayList<SensorReading>();
        }

        return readings.stream()
                .map(DataStore::copyReading)
                .sorted(Comparator.comparing(SensorReading::getId))
                .collect(Collectors.toList());
    }

    public static synchronized SensorReading createReading(long sensorId, SensorReading request) {
        Sensor sensor = SENSORS.get(sensorId);
        if (sensor == null) {
            return null;
        }

        SensorReading reading = new SensorReading();
        reading.setId(READING_IDS.incrementAndGet());
        reading.setValue(request.getValue());

        if (request.getTimestamp() == null || request.getTimestamp().trim().isEmpty()) {
            reading.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC).toString());
        } else {
            reading.setTimestamp(request.getTimestamp().trim());
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