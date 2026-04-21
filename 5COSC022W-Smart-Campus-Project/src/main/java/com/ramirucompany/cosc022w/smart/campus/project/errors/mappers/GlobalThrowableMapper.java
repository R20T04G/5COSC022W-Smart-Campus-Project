package com.ramirucompany.cosc022w.smart.campus.project.errors.mappers;

import com.ramirucompany.cosc022w.smart.campus.project.errors.ApiError;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GlobalThrowableMapper implements ExceptionMapper<Throwable> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable throwable) {
        if (throwable instanceof WebApplicationException) {
            WebApplicationException webApplicationException = (WebApplicationException) throwable;
            Response existingResponse = webApplicationException.getResponse();

            int status = existingResponse == null ? 500 : existingResponse.getStatus();
            String reason = existingResponse == null || existingResponse.getStatusInfo() == null
                    ? "Internal Server Error"
                    : existingResponse.getStatusInfo().getReasonPhrase();

            String message = throwable.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = reason;
            }

            ApiError knownError = new ApiError(
                    OffsetDateTime.now(ZoneOffset.UTC).toString(),
                    status,
                    reason,
                    message,
                    getPath()
            );

            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(knownError)
                    .build();
        }

        ApiError unknownError = new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC).toString(),
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected server error occurred.",
                getPath()
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(unknownError)
                .build();
    }

    private String getPath() {
        return uriInfo == null || uriInfo.getRequestUri() == null
                ? ""
                : uriInfo.getRequestUri().getPath();
    }
}