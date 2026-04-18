package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sensor resource — manages /api/v1/sensors
 *
 * GET  /api/v1/sensors            → list all sensors (optional ?type= filter)
 * POST /api/v1/sensors            → register a new sensor (validates roomId exists)
 * GET  /api/v1/sensors/{sensorId} → get a specific sensor
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors  — list all sensors, optional ?type= filter
    // -------------------------------------------------------------------------
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(DataStore.getSensors().values());

        // If ?type= query param is provided, filter by sensor type (case-insensitive)
        if (type != null && !type.isBlank()) {
            sensorList = sensorList.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensorList).build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/sensors  — register a new sensor
    // Validates that the referenced roomId actually exists before saving
    // -------------------------------------------------------------------------
    @POST
    public Response createSensor(Sensor sensor) {
        // Basic field validation
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody(400, "Bad Request", "Sensor 'id' is required."))
                    .build();
        }
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody(400, "Bad Request", "Sensor 'type' is required."))
                    .build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody(400, "Bad Request", "Sensor 'roomId' is required."))
                    .build();
        }

        Map<String, Sensor> sensors = DataStore.getSensors();
        Map<String, Room>   rooms   = DataStore.getRooms();

        // Check for duplicate sensor ID
        if (sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody(409, "Conflict", "Sensor with id '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // Integrity check: roomId must reference an existing room
        if (!rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }

        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        // Save sensor and link it to the room
        sensors.put(sensor.getId(), sensor);
        rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Initialise an empty reading history for this sensor
        DataStore.getReadings().put(sensor.getId(), new ArrayList<>());

        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}  — get a specific sensor by ID
    // -------------------------------------------------------------------------
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensors().get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Not Found", "Sensor '" + sensorId + "' does not exist."))
                    .build();
        }

        return Response.ok(sensor).build();
    }

    // -------------------------------------------------------------------------
    // Sub-resource locator — delegates {sensorId}/readings to SensorReadingResource
    // (implemented in Day 4)
    // -------------------------------------------------------------------------
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    // -------------------------------------------------------------------------
    // Helper — consistent JSON error body
    // -------------------------------------------------------------------------
    private Map<String, Object> errorBody(int status, String error, String message) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
