package com.benhession.imagepicker.controller;

import com.benhession.imagepicker.config.ImageConfigProperties;
import com.benhession.imagepicker.model.ObjectUploadForm;
import com.benhession.imagepicker.service.ImageCreationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

@ApplicationScoped
@Path("/image")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImageController {
    private final ImageConfigProperties imageConfigProperties;
    private final ImageCreationService imageCreationService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addImage(@Valid @MultipartForm ObjectUploadForm objectUploadForm) {

        if (!imageConfigProperties.acceptedMimeTypes().contains(objectUploadForm.getMimetype())) {
            throw new BadRequestException("Mime type must be one of the following " + imageConfigProperties.acceptedMimeTypes());
        }

        // TODO: get image id from imageCreationService
        imageCreationService.createNewImagesFrom(objectUploadForm.getData());

        // TODO: fetch meta data using id
        //  build hateoas response and add to body
        return Response.accepted().build();
    }
}
