package com.benhession.imagepicker.controller;

import com.benhession.imagepicker.config.ImageConfigProperties;
import com.benhession.imagepicker.dto.ObjectUploadForm;
import com.benhession.imagepicker.exception.BadRequestException;
import com.benhession.imagepicker.model.ImageMetadata;
import com.benhession.imagepicker.service.ImageCreationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.jboss.resteasy.reactive.common.util.RestMediaType.APPLICATION_HAL_JSON;

@ApplicationScoped
@Path("/image")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImageController {
    private final ImageConfigProperties imageConfigProperties;
    private final ImageCreationService imageCreationService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON, APPLICATION_HAL_JSON})
    public Response addImage(@Valid @BeanParam ObjectUploadForm objectUploadForm) {

        validateMimeType(objectUploadForm.getMimetype());

        ImageMetadata metadata = imageCreationService.createNewImages(objectUploadForm);

        // TODO:
        //  build response with all sizes
        return Response.accepted().build();
    }

    private void validateMimeType(String mimeType) {
        if (!imageConfigProperties.acceptedMimeTypes().contains(mimeType)) {
            var errorMessage = BadRequestException.ErrorMessage.builder()
                    .path("/image")
                    .message("Mime type must be one of the following " + imageConfigProperties.acceptedMimeTypes())
                    .build();
            throw new BadRequestException(List.of(errorMessage));
        }
    }
}
