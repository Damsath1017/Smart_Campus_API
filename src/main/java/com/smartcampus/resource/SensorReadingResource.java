package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sub-resource for managing readings of a specific sensor.
 * Path: /api/v1/sensors/{sensorId}/readings
 * Note: This class does NOT have a @Path annotation at the class level because 
 * it is instantiated dynamically via the SensorResource sub-resource locator.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}/readings  — get all readings history
    // -------------------------------------------------------------------------
    @GET
    public Response getReadings() {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Not Found", "Sensor '" + sensorId + "' does not exist."))
                    .build();
        }

        List<SensorReading> readings = DataStore.getReadings().get(sensorId);
        return Response.ok(readings).build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/sensors/{sensorId}/readings  — append a new reading
    // -------------------------------------------------------------------------
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Not Found", "Sensor '" + sensorId + "' does not exist."))
                    .build();
        }

        // Validate state: Cannot accept readings if in MAINTENANCE
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        // Enforce basic validation
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody(400, "Bad Request", "Reading body is missing."))
                    .build();
        }

        // Auto-generate metadata for the reading
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());

        // Append to the sensor's reading history
        DataStore.getReadings().get(sensorId).add(reading);

        // SIDE EFFECT: Update the parent sensor's current value for consistency
        sensor.setCurrentValue(reading.getValue());

        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId());
        return Response.created(location).entity(reading).build();
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
