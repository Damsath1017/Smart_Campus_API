package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store for all campus data.
 * Uses ConcurrentHashMap to handle concurrent requests safely.
 * Pre-populated with sample data for testing.
 */
public class DataStore {

    // Thread-safe in-memory storage
    private static final Map<String, Room>              rooms    = new ConcurrentHashMap<>();
    private static final Map<String, Sensor>            sensors  = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Static initializer — pre-populate with sample data
    static {
        // --- Sample Rooms ---
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Lab", 30);
        Room r3 = new Room("HALL-A", "Main Hall", 200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // --- Sample Sensors ---
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE", 410.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0.0, "HALL-A");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // Link sensors to their rooms
        r1.getSensorIds().add("TEMP-001");
        r2.getSensorIds().add("CO2-001");
        r3.getSensorIds().add("OCC-001");

        // Initialize empty reading histories for each sensor
        readings.put("TEMP-001", new ArrayList<>());
        readings.put("CO2-001",  new ArrayList<>());
        readings.put("OCC-001",  new ArrayList<>());
    }

    // Prevent instantiation — this is a static utility class
    private DataStore() {}

    public static Map<String, Room> getRooms()    { return rooms; }
    public static Map<String, Sensor> getSensors() { return sensors; }
    public static Map<String, List<SensorReading>> getReadings() { return readings; }
}
