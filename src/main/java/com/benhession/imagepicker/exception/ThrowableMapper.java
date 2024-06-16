package com.benhession.imagepicker.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@Provider
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    private final Logger log;

    @ConfigProperty(name = "validation-messages.system.error")
    String systemErrorMessage;

    @Override
    public Response toResponse(Throwable e) {

        if (e instanceof ClientErrorException clientErrorException) {
            return clientErrorException.getResponse();
        }

        String errorId = UUID.randomUUID().toString();
        log.error("errorId[{}]", errorId, e);
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorId(errorId)
            .errors(List.of(ErrorResponse.ErrorMessage.builder()
                    .message(systemErrorMessage)
                    .build()))
            .build();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
    }

}
