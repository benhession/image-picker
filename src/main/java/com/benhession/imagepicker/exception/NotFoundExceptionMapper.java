package com.benhession.imagepicker.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

@Provider
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    private final ErrorMessageService errorMessageService;

    @Override
    public Response toResponse(NotFoundException e) {
        ErrorResponse errorResponse = errorMessageService.mapApplicationExceptionList(e);
        return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
    }
}
