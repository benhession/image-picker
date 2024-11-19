package com.benhession.imagepicker.api.exception;

import com.benhession.imagepicker.common.exception.DownStreamServerException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

@Provider
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class DownStreamSeverExceptionMapper implements ExceptionMapper<DownStreamServerException> {
    private final ErrorMessageService errorMessageService;

    @Override
    public Response toResponse(DownStreamServerException e) {
        return Response.status(Status.BAD_GATEWAY)
            .entity(errorMessageService.mapSingleMessageError(e))
            .build();
    }
}
