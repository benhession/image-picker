package com.benhession.imagepicker.api.controller;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.CROPPED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.INITIALISED;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.jboss.resteasy.reactive.RestResponse.Status.OK;

import com.benhession.imagepicker.api.dto.CropPropertiesDto;
import com.benhession.imagepicker.api.dto.GetUploadUrlDto;
import com.benhession.imagepicker.api.dto.ImageResponseDto;
import com.benhession.imagepicker.api.dto.ProcessImageDto;
import com.benhession.imagepicker.api.dto.UploadUrlResponseDto;
import com.benhession.imagepicker.api.mapper.CropPropertiesMapper;
import com.benhession.imagepicker.api.mapper.ImageResponseMapper;
import com.benhession.imagepicker.api.service.ImageProcessingService;
import com.benhession.imagepicker.api.service.ImageValidationService;
import com.benhession.imagepicker.api.service.PaginationLinksService;
import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException;
import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException.ErrorMessage;
import com.benhession.imagepicker.common.exception.BadRequestException;
import com.benhession.imagepicker.common.exception.DownStreamServerException;
import com.benhession.imagepicker.common.exception.DownStreamServerTimeoutException;
import com.benhession.imagepicker.common.exception.NotFoundException;
import com.benhession.imagepicker.common.model.ImageCropProperties;
import com.benhession.imagepicker.common.model.ImageType;
import com.benhession.imagepicker.common.model.PageInfo;
import com.benhession.imagepicker.data.dto.ImageUploadDto;
import com.benhession.imagepicker.data.dto.PreSignedUploadDto;
import com.benhession.imagepicker.data.model.ImageMetaDataSearchResult;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import io.quarkus.resteasy.reactive.links.InjectRestLinks;
import io.quarkus.resteasy.reactive.links.RestLink;
import io.quarkus.resteasy.reactive.links.RestLinkType;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Path("/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageResponseMapper imageResponseMapper;
    private final ImageMetaDataService imageMetaDataService;
    private final PaginationLinksService paginationLinksService;
    private final ImageValidationService imageValidationService;
    private final ImageProcessingService imageProcessingService;
    private final ObjectStorageService objectStorageService;
    private final CropPropertiesMapper cropPropertiesMapper;

    @POST
    @Path("/pre-signed")
    @Consumes(APPLICATION_JSON)
    @RolesAllowed({"admin"})
    public RestResponse<UploadUrlResponseDto> getUploadUrl(@Valid GetUploadUrlDto getUploadUrlDto) {
        imageValidationService.validateMimeType(getUploadUrlDto.getMimetype(), "/image/pre-signed");

        var imageMetadata =
            imageMetaDataService.newImageMetaData(getUploadUrlDto.getFilename(), getUploadUrlDto.getTags());

        var imageUploadDto = ImageUploadDto.builder()
            .filename(imageMetadata.getFilename())
            .mimetype(getUploadUrlDto.getMimetype())
            .build();

        PreSignedUploadDto uploadUrlDto =
            objectStorageService.getPreSignedUrl(imageUploadDto, imageMetadata.getParentKey());

        return RestResponse.ok(UploadUrlResponseDto.builder()
            .imageId(imageMetadata.getId().toString())
            .uploadUrl(uploadUrlDto.getUrl())
            .headers(uploadUrlDto.getHeaders())
            .build());
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path(("/{id}/process"))
    @RolesAllowed({"admin"})
    @InjectRestLinks(RestLinkType.INSTANCE)
    public RestResponse<ImageResponseDto> processImage(@PathParam("id") ObjectId imageId,
        @Valid ProcessImageDto processImageDto, @Context UriInfo uriInfo) {

        var imageMetadata = imageMetaDataService.getImageMetaData(imageId)
            .orElseThrow(() -> new NotFoundException(List.of(ErrorMessage.builder()
                .message("Unable to find metadata for image id " + imageId)
                .path(uriInfo.getPath())
                .build())));

        List<ImageProcessingStage> validStages = List.of(INITIALISED, CROPPED);

        if (!validStages.contains(imageMetadata.getStatus().stage())) {
            throw new BadRequestException(List.of(ErrorMessage.builder()
                .message(String.format("Expected image processing stage to be one of %s but was %s",
                    validStages, imageMetadata.getStatus().stage()))
                .path(uriInfo.getPath())
                .build()));
        }

        imageMetadata = imageProcessingService
            .validateAndProcessUploadedImage(ImageType.valueOf(processImageDto.getImageType()), imageMetadata);

        return RestResponse.accepted(imageResponseMapper.toDtoWithoutImages(imageMetadata));
    }

    @GET
    @Path("/{id}")
    @Produces({APPLICATION_JSON})
    @PermitAll
    @RestLink(rel = "self")
    @InjectRestLinks(RestLinkType.INSTANCE)
    public RestResponse<ImageResponseDto> getImage(@PathParam("id") ObjectId id) {
        ImageMetadata metadata = imageMetaDataService.getImageMetaData(id)
            .orElseThrow(() -> new NotFoundException(List.of(
                AbstractMultipleErrorApplicationException.ErrorMessage.builder()
                    .path("/image/" + id.toString())
                    .message("Unable to find image with id: " + id)
                    .build())));

        return switch (metadata.getStatus().stage()) {
            case PROCESSING_FAILED -> throw new DownStreamServerException(
                String.format(
                    "The image could not be processed successfully. id: %s status: %s", id, metadata.getStatus()));
            case PROCESSING_TIMEOUT -> throw new DownStreamServerTimeoutException(
                String.format(
                    "The image processing timed out. id: %s status: %s", id, metadata.getStatus()));
            case INITIALISED, ORIGINAL_UPLOADED, PROCESSING, SENT_TO_CROP, CROPPING, CROPPED ->
                RestResponse.ok(imageResponseMapper.toDtoWithoutImages(metadata));
            case PROCESSING_COMPLETE -> RestResponse.ok(imageResponseMapper.toDto(metadata));
            case null -> throw new IllegalStateException("Processing status not found for image id: " + id);
        };
    }

    @GET
    @Produces({APPLICATION_JSON})
    @RolesAllowed({"admin"})
    @RestLink(rel = "list")
    public RestResponse<List<ImageResponseDto>> getImages(@QueryParam("page") String pageString,
        @QueryParam("size") String sizeString,
        @Context UriInfo uriInfo) {

        List<AbstractMultipleErrorApplicationException.ErrorMessage> errorMessages = new ArrayList<>();
        int page = parseIntegerQueryParameter(pageString, "page", errorMessages);
        int size = parseIntegerQueryParameter(sizeString, "size", errorMessages);

        checkPaginationValuesAreValid(page, size, uriInfo.getPath(), errorMessages);

        if (!errorMessages.isEmpty()) {
            throw new BadRequestException(errorMessages);
        }

        PageInfo pageInfo = imageMetaDataService.findProcessedImagesPageInfo(page, size);
        if (pageInfo.numberItems() == 0) {
            return RestResponse.noContent();
        }

        List<ImageMetadata> imageMetadataList =
            imageMetaDataService.findProcessedImages(pageInfo.page(), pageInfo.size());

        return RestResponse.ResponseBuilder
            .create(OK, imageMetadataList.stream()
                .map(imageResponseMapper::toDto)
                .toList())
            .links(paginationLinksService.getPaginationLinks(pageInfo, uriInfo, Map.of()))
            .build();
    }

    @POST
    @Path("/{id}/crop")
    @Consumes(APPLICATION_JSON)
    @RolesAllowed({"admin"})
    @InjectRestLinks(RestLinkType.INSTANCE)
    public RestResponse<ImageResponseDto> cropImage(@PathParam("id") ObjectId id,
        @Valid CropPropertiesDto cropPropertiesDto, @Context UriInfo uriInfo) {

        ImageCropProperties cropProperties = cropPropertiesMapper.toModel(cropPropertiesDto);
        ImageMetadata imageMetadata = imageMetaDataService.getImageMetaData(id)
            .orElseThrow(() -> new NotFoundException(List.of(ErrorMessage.builder()
                .message("Unable to find image metadata with id: " + id)
                .path(uriInfo.getPath())
                .build())));

        ImageProcessingStage stage = imageMetadata.getStatus().stage();
        if (!stage.equals(INITIALISED)) {
            throw new BadRequestException(List.of(ErrorMessage.builder()
                .message(String.format("Expected image processing stage to be %s but was %s", INITIALISED, stage))
                .build()));
        }

        imageMetadata = imageProcessingService.validateAndCropOriginalImage(imageMetadata, cropProperties);
        return RestResponse.accepted(imageResponseMapper.toDtoWithoutImages(imageMetadata));
    }

    @GET
    @Path("/search")
    @Consumes(APPLICATION_JSON)
    @RolesAllowed({"admin"})
    @RestLink(rel = "search")
    public RestResponse<List<ImageResponseDto>> searchImages(@QueryParam("page") String pageString,
        @QueryParam("size") String sizeString, @QueryParam("searchTerm") String searchTerm,
        @QueryParam("searchAfter") String searchAfter, @QueryParam("searchBefore") String searchBefore,
        @Context UriInfo uriInfo) {

        List<AbstractMultipleErrorApplicationException.ErrorMessage> errorMessages = new ArrayList<>();
        int page = parseIntegerQueryParameter(pageString, "page", errorMessages);
        int size = parseIntegerQueryParameter(sizeString, "size", errorMessages);
        checkPaginationValuesAreValid(page, size, uriInfo.getPath(), errorMessages);

        if (searchTerm.isBlank()) {
            errorMessages.add(ErrorMessage.builder()
                .message("SearchTerm parameter is required and cannot be blank")
                .path(uriInfo.getPath())
                .build());
        }

        if (!errorMessages.isEmpty()) {
            throw new BadRequestException(errorMessages);
        }

        PageInfo pageInfo = imageMetaDataService.searchByFilenameAndTagsPageInfo(page, size, searchTerm);
        if (pageInfo.numberItems() == 0) {
            return RestResponse.noContent();
        }

        List<ImageMetaDataSearchResult> results =
            imageMetaDataService.searchByFilenameAndTags(searchTerm, page, size, searchBefore, searchAfter);

        return RestResponse.ResponseBuilder.create(OK, results.stream().map(imageResponseMapper::toDto).toList())
            .links(paginationLinksService.getPaginationLinksForSearchResults(pageInfo, uriInfo, results,
                Map.of("searchTerm", searchTerm)))
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

    private void checkPaginationValuesAreValid(int page, int size, String path, List<ErrorMessage> errorMessages) {
        if (size <= 0) {
            errorMessages.add(AbstractMultipleErrorApplicationException.ErrorMessage.builder()
                .path(path)
                .message("'size' must be greater than 0")
                .build());
        }
        if (page < 0) {
            errorMessages.add(AbstractMultipleErrorApplicationException.ErrorMessage.builder()
                .path(path)
                .message("'page' must be non-negative")
                .build());
        }
    }
}
