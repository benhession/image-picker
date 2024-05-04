package com.benhession.imagepicker.controller;

import com.benhession.imagepicker.config.ImageConfigProperties;
import com.benhession.imagepicker.exception.BadRequestException;
import com.benhession.imagepicker.model.ObjectUploadForm;
import com.benhession.imagepicker.service.ImageCreationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.util.List;

@ApplicationScoped
@Path("/image")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImageController {
    private final ImageConfigProperties imageConfigProperties;
    private final ImageCreationService imageCreationService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addImage(@Valid @MultipartForm ObjectUploadForm objectUploadForm) {

        validateMimeType(objectUploadForm.getMimetype());
        // TODO: check image size

        // TODO: get image id from imageCreationService
        imageCreationService.createNewImagesFrom(objectUploadForm.getData());

        // TODO: fetch meta data using id
        //  build hateoas response and add to body
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
