package com.ramirucompany.cosc022w.smart.campus.project.resources;

import com.ramirucompany.cosc022w.smart.campus.project.db.DataStore;
import com.ramirucompany.cosc022w.smart.campus.project.errors.ConflictException;
import com.ramirucompany.cosc022w.smart.campus.project.errors.UnprocessableEntityException;
import com.ramirucompany.cosc022w.smart.campus.project.models.Room;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @GET
    public Response listRooms() {
        return Response.ok(DataStore.listRooms()).build();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        validateRoomPayload(room);

        Room createdRoom = DataStore.createRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(createdRoom.getId()))
                .build();

        return Response.created(location)
                .entity(createdRoom)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getRoomById(@PathParam("id") long id) {
        Room room = DataStore.getRoom(id);
        if (room == null) {
            throw new NotFoundException("Room " + id + " was not found.");
        }

        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") long id) {
        DataStore.DeleteRoomResult deleteResult = DataStore.deleteRoom(id);
        if (deleteResult == DataStore.DeleteRoomResult.HAS_ATTACHED_SENSORS) {
            throw new ConflictException("Room " + id + " cannot be deleted because it still has registered sensors.");
        }

        // Return 204 for both deleted and already-missing cases to keep DELETE idempotent.
        return Response.noContent().build();
    }

    private void validateRoomPayload(Room room) {
        if (room == null) {
            throw new UnprocessableEntityException("Room payload is required.");
        }

        if (room.getName() == null || room.getName().trim().isEmpty()) {
            throw new UnprocessableEntityException("Room name is required.");
        }

        if (room.getCapacity() == null) {
            throw new UnprocessableEntityException("Room capacity is required.");
        }

        if (room.getCapacity() < 0) {
            throw new UnprocessableEntityException("Room capacity must be zero or greater.");
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new UnprocessableEntityException("sensorIds are managed by the API and must not be provided when creating a room.");
        }
    }
}