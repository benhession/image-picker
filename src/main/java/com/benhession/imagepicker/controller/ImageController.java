package com.benhession.imagepicker.controller;

import com.benhession.imagepicker.config.ImageConfigProperties;
import com.benhession.imagepicker.dto.ImageResponseDto;
import com.benhession.imagepicker.dto.ObjectUploadForm;
import com.benhession.imagepicker.exception.AbstractMultipleErrorApplicationException;
import com.benhession.imagepicker.exception.BadRequestException;
import com.benhession.imagepicker.exception.NotFoundException;
import com.benhession.imagepicker.mapper.ImageResponseMapper;
import com.benhession.imagepicker.model.ImageMetadata;
import com.benhession.imagepicker.service.ImageCreationService;
import com.benhession.imagepicker.service.ImageRudService;
import io.quarkus.resteasy.reactive.links.InjectRestLinks;
import io.quarkus.resteasy.reactive.links.RestLink;
import io.quarkus.resteasy.reactive.links.RestLinkType;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Path("/image")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImageController {
    private final ImageConfigProperties imageConfigProperties;
    private final ImageCreationService imageCreationService;
    private final ImageResponseMapper imageResponseMapper;
    private final ImageRudService imageRudService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed({"blog-admin"})
    @InjectRestLinks(RestLinkType.INSTANCE)
    public RestResponse<ImageResponseDto> addImage(@Valid @BeanParam ObjectUploadForm objectUploadForm) {

        validateMimeType(objectUploadForm.getMimetype());
        ImageMetadata metadata = imageCreationService.createNewImages(objectUploadForm);

        return RestResponse.accepted(imageResponseMapper.toDto(metadata));
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @PermitAll
    @RestLink(rel = "self")
    @InjectRestLinks(RestLinkType.INSTANCE)
    public RestResponse<ImageResponseDto> getImage(@PathParam("id") ObjectId id) {
        Optional<ImageMetadata> metadataOptional = imageRudService.getImageMetaData(id);

        return metadataOptional
          .map(metadata -> RestResponse
            .ok(imageResponseMapper.toDto(metadata)))
          .orElseThrow(() -> new NotFoundException(List.of(
              AbstractMultipleErrorApplicationException.ErrorMessage.builder()
                .path("/image/" + id.toString())
                .message("Unable to find image with id: " + id)
                .build())));
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
