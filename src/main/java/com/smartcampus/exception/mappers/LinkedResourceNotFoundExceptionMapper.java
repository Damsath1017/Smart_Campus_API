package com.smartcampus.exception.mappers;

import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity.
 * Triggered when a sensor references a roomId that does not exist.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 422);
        body.put("error", "Unprocessable Entity");
        body.put("message", e.getMessage());

        return Response.status(422)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
