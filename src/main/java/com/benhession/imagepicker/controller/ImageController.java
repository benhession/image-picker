package com.benhession.imagepicker.controller;

import com.benhession.imagepicker.config.ImageConfigProperties;
import com.benhession.imagepicker.dto.ObjectUploadForm;
import com.benhession.imagepicker.exception.BadRequestException;
import com.benhession.imagepicker.mapper.ImageResponseMapper;
import com.benhession.imagepicker.model.ImageMetadata;
import com.benhession.imagepicker.service.ImageCreationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@Path("/image")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImageController {
    private final ImageConfigProperties imageConfigProperties;
    private final ImageCreationService imageCreationService;
    private final ImageResponseMapper imageResponseMapper;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed({"blog-admin"})
    public Response addImage(@Valid @BeanParam ObjectUploadForm objectUploadForm) {

        validateMimeType(objectUploadForm.getMimetype());

        ImageMetadata metadata = imageCreationService.createNewImages(objectUploadForm);

        return Response.accepted()
          .entity(imageResponseMapper.toDto(metadata))
          .build();
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
