package com.benhession.imagepicker.api.exception;

import com.benhession.imagepicker.common.exception.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

@Provider
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    private final ErrorMessageService errorMessageService;

    @Override
    public Response toResponse(BadRequestException e) {
        ErrorResponse errorResponse = errorMessageService.mapApplicationExceptionList(e);
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }
}