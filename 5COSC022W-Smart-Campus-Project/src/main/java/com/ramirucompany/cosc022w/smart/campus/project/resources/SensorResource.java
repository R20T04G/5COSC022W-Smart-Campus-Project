package com.ramirucompany.cosc022w.smart.campus.project.resources;

import com.ramirucompany.cosc022w.smart.campus.project.db.DataStore;
import com.ramirucompany.cosc022w.smart.campus.project.errors.ForbiddenOperationException;
import com.ramirucompany.cosc022w.smart.campus.project.errors.UnprocessableEntityException;
import com.ramirucompany.cosc022w.smart.campus.project.models.Sensor;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private static final List<String> ALLOWED_STATUS_VALUES = Arrays.asList("active", "inactive", "offline");

    @GET
    public Response listSensors(@QueryParam("type") String type) {
        return Response.ok(DataStore.listSensors(type)).build();
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        validateSensorPayload(sensor);

        Sensor createdSensor = DataStore.createSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(createdSensor.getId()))
                .build();

        return Response.created(location)
                .entity(createdSensor)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getSensorById(@PathParam("id") long id) {
        Sensor sensor = DataStore.getSensor(id);
        if (sensor == null) {
            throw new NotFoundException("Sensor " + id + " was not found.");
        }

        return Response.ok(sensor).build();
    }

    @Path("/{id}/readings")
    public SensorReadingResource sensorReadingResource(@PathParam("id") long id) {
        if (!DataStore.sensorExists(id)) {
            throw new NotFoundException("Sensor " + id + " was not found.");
        }

        return new SensorReadingResource(id);
    }

    private void validateSensorPayload(Sensor sensor) {
        if (sensor == null) {
            throw new UnprocessableEntityException("Sensor payload is required.");
        }

        if (sensor.getRoomId() == null) {
            throw new UnprocessableEntityException("roomId is required for sensor registration.");
        }

        if (!DataStore.roomExists(sensor.getRoomId())) {
            throw new UnprocessableEntityException("roomId " + sensor.getRoomId() + " does not reference an existing room.");
        }

        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            throw new UnprocessableEntityException("Sensor type is required.");
        }

        if ("biometric".equalsIgnoreCase(sensor.getType().trim())) {
            throw new ForbiddenOperationException("Biometric sensors are not permitted through this API.");
        }

        if (sensor.getStatus() != null && sensor.getStatus().trim().isEmpty()) {
            throw new UnprocessableEntityException("Sensor status cannot be blank when provided.");
        }

        if (sensor.getStatus() != null) {
            String normalizedStatus = sensor.getStatus().trim().toLowerCase(Locale.ROOT);
            if (!ALLOWED_STATUS_VALUES.contains(normalizedStatus)) {
                throw new UnprocessableEntityException("Sensor status must be one of: active, inactive, offline.");
            }
        }
    }
}