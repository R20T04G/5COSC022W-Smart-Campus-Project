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
import com.ramirucompany.cosc022w.smart.campus.project.errors.ConflictException;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ConflictException exception) {
        ApiError error = new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC).toString(),
                Response.Status.CONFLICT.getStatusCode(),
                Response.Status.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                getPath()
        );

        return Response.status(Response.Status.CONFLICT)
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