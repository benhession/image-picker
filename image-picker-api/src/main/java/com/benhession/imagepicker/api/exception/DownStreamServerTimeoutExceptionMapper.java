package com.benhession.imagepicker.api.exception;

import com.benhession.imagepicker.common.exception.DownStreamServerTimeoutException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

@Provider
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class DownStreamServerTimeoutExceptionMapper implements ExceptionMapper<DownStreamServerTimeoutException> {
    private final ErrorMessageService errorMessageService;

    @Override
    public Response toResponse(DownStreamServerTimeoutException e) {
        return Response.status(Status.GATEWAY_TIMEOUT)
            .entity(errorMessageService.mapSingleMessageError(e))
            .build();
    }
}
