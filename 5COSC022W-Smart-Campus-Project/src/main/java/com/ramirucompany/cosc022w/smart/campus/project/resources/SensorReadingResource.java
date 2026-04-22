package com.ramirucompany.cosc022w.smart.campus.project.resources;

import com.ramirucompany.cosc022w.smart.campus.project.db.DataStore;
import com.ramirucompany.cosc022w.smart.campus.project.errors.LinkedResourceNotFoundException;
import com.ramirucompany.cosc022w.smart.campus.project.errors.SensorUnavailableException;
import com.ramirucompany.cosc022w.smart.campus.project.models.Sensor;
import com.ramirucompany.cosc022w.smart.campus.project.models.SensorReading;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final long sensorId;

    public SensorReadingResource(long sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response listReadings() {
        if (!DataStore.sensorExists(sensorId)) {
            throw new NotFoundException("Sensor " + sensorId + " was not found.");
        }

        return Response.ok(DataStore.listReadings(sensorId)).build();
    }

    @POST
    public Response createReading(SensorReading reading, @Context UriInfo uriInfo) {
        validateReadingPayload(reading);

        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor " + sensorId + " was not found.");
        }

        if ("maintenance".equalsIgnoreCase(sensor.getStatus()) || "offline".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is currently unavailable and cannot accept new readings.");
        }

        SensorReading createdReading = DataStore.createReading(sensorId, reading);
        if (createdReading == null) {
            throw new NotFoundException("Sensor " + sensorId + " was not found.");
        }

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(createdReading.getId()))
                .build();

        return Response.created(location)
                .entity(createdReading)
                .build();
    }

    private void validateReadingPayload(SensorReading reading) {
        if (reading == null) {
            throw new LinkedResourceNotFoundException("Sensor reading payload is required.");
        }

        if (reading.getValue() == null) {
            throw new LinkedResourceNotFoundException("Reading value is required.");
        }
    }
}