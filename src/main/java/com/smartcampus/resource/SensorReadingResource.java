package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

/**
 * Stub for SensorReadingResource.
 * Will be fully implemented in Day 4.
 */
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        return Response.ok("Readings for sensor " + sensorId + " will be implemented in Day 4.").build();
    }
}
