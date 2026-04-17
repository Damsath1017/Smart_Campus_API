package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery endpoint — GET /api/v1
 * Returns API metadata, version info, admin contact, and resource links (HATEOAS).
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name",         "Smart Campus API");
        info.put("version",      "1.0");
        info.put("description",  "RESTful API for managing campus rooms and sensors.");
        info.put("adminContact", "admin@smartcampus.westminster.ac.uk");

        // HATEOAS-style resource links
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms",   "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        info.put("resources", resources);

        return Response.ok(info).build();
    }
}
