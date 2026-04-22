package com.ramirucompany.cosc022w.smart.campus.project.errors.mappers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.ramirucompany.cosc022w.smart.campus.project.errors.ApiError;
import com.ramirucompany.cosc022w.smart.campus.project.errors.SensorUnavailableException;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ApiError error = new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC).toString(),
                Response.Status.FORBIDDEN.getStatusCode(),
                Response.Status.FORBIDDEN.getReasonPhrase(),
                exception.getMessage(),
                getPath()
        );

        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }

    private String getPath() {
        return uriInfo == null || uriInfo.getRequestUri() == null
                ? ""
                : uriInfo.getRequestUri().getPath();
    }
}
