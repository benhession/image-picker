package com.benhession.imagepicker.api.controller;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.jboss.resteasy.reactive.RestResponse.Status.OK;

import com.benhession.imagepicker.api.dto.ImageResponseDto;
import com.benhession.imagepicker.api.dto.ObjectUploadForm;
import com.benhession.imagepicker.api.mapper.ImageResponseMapper;
import com.benhession.imagepicker.api.service.ImageCreationService;
import com.benhession.imagepicker.api.service.ImageValidationService;
import com.benhession.imagepicker.api.service.PaginationLinksService;
import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException;
import com.benhession.imagepicker.common.exception.BadRequestException;
import com.benhession.imagepicker.common.exception.NotFoundException;
import com.benhession.imagepicker.common.model.PageInfo;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import io.quarkus.resteasy.reactive.links.InjectRestLinks;
import io.quarkus.resteasy.reactive.links.RestLink;
import io.quarkus.resteasy.reactive.links.RestLinkType;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Path("/image")
@RequiredArgsConstructor
public class ImageController {
    private final ImageCreationService imageCreationService;
    private final ImageResponseMapper imageResponseMapper;
    private final ImageMetaDataService imageMetaDataService;
    private final PaginationLinksService paginationLinksService;
    private final ImageValidationService imageValidationService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({APPLICATION_JSON})
    @RolesAllowed({"blog-admin"})
    @InjectRestLinks(RestLinkType.INSTANCE)
    public RestResponse<ImageResponseDto> addImage(@Valid @BeanParam ObjectUploadForm objectUploadForm) {
        imageValidationService.validateInputImage(objectUploadForm);
        ImageMetadata metadata = imageCreationService.createNewImages(objectUploadForm);

        return RestResponse.accepted(imageResponseMapper.toDto(metadata));
    }

    @GET
    @Path("/{id}")
    @Produces({APPLICATION_JSON})
    @PermitAll
    @RestLink(rel = "self")
    @InjectRestLinks(RestLinkType.INSTANCE)
    public RestResponse<ImageResponseDto> getImage(@PathParam("id") ObjectId id) {
        Optional<ImageMetadata> metadataOptional = imageMetaDataService.getImageMetaData(id);

        return metadataOptional
          .map(metadata -> RestResponse
            .ok(imageResponseMapper.toDto(metadata)))
          .orElseThrow(() -> new NotFoundException(List.of(
            AbstractMultipleErrorApplicationException.ErrorMessage.builder()
              .path("/image/" + id.toString())
              .message("Unable to find image with id: " + id)
              .build())));
    }

    @GET
    @Produces({APPLICATION_JSON})
    @RolesAllowed({"blog-admin"})
    @RestLink(rel = "list")
    public RestResponse<List<ImageResponseDto>> getImages(@QueryParam("page") String pageString,
                                                          @QueryParam("size") String sizeString,
                                                          @Context UriInfo uriInfo) {

        List<AbstractMultipleErrorApplicationException.ErrorMessage> errorMessages = new ArrayList<>();
        int page = parseIntegerQueryParameter(pageString, "page", errorMessages);
        int size = parseIntegerQueryParameter(sizeString, "size", errorMessages);

        if (size <= 0) {
            errorMessages.add(AbstractMultipleErrorApplicationException.ErrorMessage.builder()
              .path("/image")
              .message("'size' must be greater than 0")
              .build());
        }
        if (page < 0) {
            errorMessages.add(AbstractMultipleErrorApplicationException.ErrorMessage.builder()
              .path("/image")
              .message("'page' must be non-negative")
              .build());
        }
        if (!errorMessages.isEmpty()) {
            throw new BadRequestException(errorMessages);
        }

        PageInfo pageInfo = imageMetaDataService.getPageInfo(page, size);
        if (pageInfo.numberItems() == 0) {
            return RestResponse.noContent();
        }

        List<ImageMetadata> imageMetadataList =
          imageMetaDataService.getImageMetaDataList(pageInfo.page(), pageInfo.size());

        return RestResponse.ResponseBuilder
          .create(OK, imageMetadataList.stream()
            .map(imageResponseMapper::toDto)
            .toList())
          .links(paginationLinksService.getPaginationLinks(pageInfo, uriInfo))
          .build();
    }

    private int parseIntegerQueryParameter(String paramString, String paramName,
                                           List<AbstractMultipleErrorApplicationException.ErrorMessage> errorMessages) {

        if (paramString != null) {
            try {
                return Integer.parseInt(paramString);
            } catch (NumberFormatException e) {
                errorMessages.add(AbstractMultipleErrorApplicationException.ErrorMessage.builder()
                  .path("/image")
                  .message("query parameter '" + paramName + "' must be an integer")
                  .build());
            }
        } else {
            errorMessages.add(AbstractMultipleErrorApplicationException.ErrorMessage.builder()
              .path("/image")
              .message("query parameter '" + paramName + "' is required")
              .build());
        }

        return 0;
    }
}
