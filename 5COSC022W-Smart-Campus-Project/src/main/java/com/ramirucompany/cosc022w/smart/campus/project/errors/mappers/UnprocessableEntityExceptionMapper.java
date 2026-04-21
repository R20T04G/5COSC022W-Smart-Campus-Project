package com.ramirucompany.cosc022w.smart.campus.project.errors.mappers;

import com.ramirucompany.cosc022w.smart.campus.project.errors.ApiError;
import com.ramirucompany.cosc022w.smart.campus.project.errors.UnprocessableEntityException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnprocessableEntityExceptionMapper implements ExceptionMapper<UnprocessableEntityException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(UnprocessableEntityException exception) {
        ApiError error = new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC).toString(),
                422,
                "Unprocessable Entity",
                exception.getMessage(),
                getPath()
        );

        return Response.status(422)
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