package com.benhession.imagepicker.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@Provider
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    private final Logger log;

    @Override
    public Response toResponse(BadRequestException e) {
        String errorId = UUID.randomUUID().toString();
        log.error("errorId[{}]", errorId, e);

        List<ErrorResponse.ErrorMessage> errorMessages = e.getErrorMessages()
                .stream()
                .map(error -> ErrorResponse.ErrorMessage.builder()
                        .message(error.message())
                        .path(error.path())
                        .build())
                .toList();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorId(errorId)
                .errors(errorMessages)
                .build();

        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }
}
