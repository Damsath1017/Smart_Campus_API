package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Room resource — manages /api/v1/rooms
 *
 * GET  /api/v1/rooms          → list all rooms
 * POST /api/v1/rooms          → create a new room
 * GET  /api/v1/rooms/{roomId} → get a specific room
 * DELETE /api/v1/rooms/{roomId} → delete room (blocked if sensors still assigned)
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // -------------------------------------------------------------------------
    // GET /api/v1/rooms  — return all rooms
    // -------------------------------------------------------------------------
    @GET
    public Response getAllRooms() {
        Collection<Room> rooms = DataStore.getRooms().values();
        List<Room> roomList = new ArrayList<>(rooms);
        return Response.ok(roomList).build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/rooms  — create a new room
    // -------------------------------------------------------------------------
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody(400, "Bad Request", "Room 'id' is required."))
                    .build();
        }

        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody(400, "Bad Request", "Room 'name' is required."))
                    .build();
        }

        Map<String, Room> rooms = DataStore.getRooms();

        if (rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody(409, "Conflict", "Room with id '" + room.getId() + "' already exists."))
                    .build();
        }

        // Ensure sensorIds list starts empty on creation
        room.setSensorIds(new ArrayList<>());
        rooms.put(room.getId(), room);

        URI location = URI.create("/api/v1/rooms/" + room.getId());
        return Response.created(location).entity(room).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/rooms/{roomId}  — get a specific room by ID
    // -------------------------------------------------------------------------
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRooms().get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Not Found", "Room '" + roomId + "' does not exist."))
                    .build();
        }

        return Response.ok(room).build();
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/rooms/{roomId}  — delete room (blocked if sensors assigned)
    // -------------------------------------------------------------------------
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Map<String, Room> rooms = DataStore.getRooms();
        Room room = rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Not Found", "Room '" + roomId + "' does not exist."))
                    .build();
        }

        // Safety check: cannot delete a room that still has sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }

        rooms.remove(roomId);
        return Response.noContent().build(); // 204 No Content
    }

    // -------------------------------------------------------------------------
    // Helper — consistent JSON error body
    // -------------------------------------------------------------------------
    private java.util.Map<String, Object> errorBody(int status, String error, String message) {
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
