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
public class ImageProcessingExceptionMapper implements ExceptionMapper<ImageProcessingException> {
    private final Logger log;

    @Override
    public Response toResponse(ImageProcessingException e) {
        String errorId = UUID.randomUUID().toString();
        log.error("errorId[{}]", errorId, e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorId(errorId)
                .errors(List.of(ErrorResponse.ErrorMessage.builder()
                        .message(e.getMessage())
                        .build()))
                .build();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
    }
}
