package com.benhession.imagepicker.api.exception;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

@Provider
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ImageProcessingExceptionMapper implements ExceptionMapper<ImageProcessingException> {
    private final ErrorMessageService errorMessageService;

    @Override
    public Response toResponse(ImageProcessingException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(errorMessageService.mapSingleMessageError(e))
            .build();
    }
}
