package com.benhession.imagepicker.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.ResourceBundle;
import java.util.UUID;

@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    @Inject
    Logger log;

    @Override
    public Response toResponse(Throwable e) {
        String errorId = UUID.randomUUID().toString();
        log.error("errorId[{}]", errorId, e);
        String defaultErrorMessage = ResourceBundle.getBundle("ValidationMessages").getString("System.error");
        ErrorResponse.ErrorMessage errorMessage = new ErrorResponse.ErrorMessage(defaultErrorMessage);
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
    }

}
