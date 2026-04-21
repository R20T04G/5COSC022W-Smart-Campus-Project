package com.ramirucompany.cosc022w.smart.campus.project.resources;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author 
 */
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class JakartaEE8Resource {

    @GET
    public Response discovery(@Context UriInfo uriInfo) {
        String base = uriInfo.getAbsolutePath().toString();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", "Smart Campus Room Management API");
        payload.put("version", "v1");
        payload.put("status", "online");

        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("name", "Smart Campus API Team");
        contact.put("email", "api-support@smartcampus.local");
        payload.put("contact", contact);

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", base + "/rooms");
        resources.put("roomById", base + "/rooms/{id}");
        resources.put("sensors", base + "/sensors");
        resources.put("sensorById", base + "/sensors/{id}");
        resources.put("sensorReadings", base + "/sensors/{id}/readings");
        resources.put("diagnosticFailure", base + "/diagnostics/fail");
        payload.put("resources", resources);

        return Response.ok(payload).build();
    }

    @GET
    @Path("/diagnostics/fail")
    public Response triggerUnexpectedError() {
        throw new IllegalStateException("Intentional test exception.");
    }
}
