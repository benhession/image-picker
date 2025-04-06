package com.benhession.imagepicker.data.service;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.INITIALISED;

import com.benhession.imagepicker.common.config.ImageProcessingProperties;
import com.benhession.imagepicker.common.model.PageInfo;
import com.benhession.imagepicker.common.util.FilenameUtil;
import com.benhession.imagepicker.data.model.ImageMetaDataSearchResult;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.repository.ImageMetaDataRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageMetaDataService {

    private final ImageMetaDataRepository imageMetadataRepository;
    private final ImageProcessingProperties imageProcessingProperties;
    private final ObjectStorageService objectStorageService;
    private final FilenameUtil filenameUtil;

    public Optional<ImageMetadata> getImageMetaData(ObjectId objectId) {
        var metaDataOptional = imageMetadataRepository.findByIdOptional(objectId);

        if (metaDataOptional.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(checkForTimeout(metaDataOptional.orElseThrow()));
    }

    public List<ImageMetadata> findProcessedImages(int page, int size) {
        return imageMetadataRepository.findProcessedImages(page, size);
    }

    public PageInfo findProcessedImagesPageInfo(int page, int size) {
        return calculatePageInfo(page, size, imageMetadataRepository.countNumberOfProcessedImages());
    }

    public void persist(ImageMetadata imageMetadata) {
        imageMetadataRepository.persistOrUpdate(imageMetadata);
    }

    public Optional<ImageMetadata> findByParentKey(String parentKey) {
        var metaDataOptional = imageMetadataRepository.findByParentKey(parentKey);

        if (metaDataOptional.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(checkForTimeout(metaDataOptional.orElseThrow()));
    }

    public ImageMetadata setImageProcessingStage(ImageMetadata imageMetadata,
        ImageProcessingStage imageProcessingStage) {

        imageMetadata.setStatus(ImageProcessingStatus.of(imageProcessingStage));
        persist(imageMetadata);
        return imageMetadata;
    }

    public ImageMetadata newImageMetaData(String filename, List<String> tags) {
        ImageMetadata imageMetadata = ImageMetadata.builder()
            .tags(tags)
            .filename(filename)
            .parentKey(filenameUtil.generateParentKey(filename))
            .status(ImageProcessingStatus.of(INITIALISED))
            .build();

        persist(imageMetadata);
        return imageMetadata;
    }

    public List<ImageMetaDataSearchResult> searchByFilenameAndTags(String searchTerm, int page, int size,
        String searchBefore, String searchAfter) {
        if (searchBefore != null && !searchBefore.isBlank() && page > 0) {
            return imageMetadataRepository.searchByFilenameAndTagsBefore(searchTerm, size, searchBefore);
        }

        if (searchAfter != null && !searchAfter.isBlank()) {
            return imageMetadataRepository.searchByFilenameAndTagsAfter(searchTerm, size, searchAfter);
        }

        return imageMetadataRepository.searchByFilenameAndTags(searchTerm, page, size);
    }

    public PageInfo searchByFilenameAndTagsPageInfo(int page, int size, String searchTerm) {
        return calculatePageInfo(page, size, imageMetadataRepository.countItemsForSearchByFilenameAndTags(searchTerm));
    }

    private PageInfo calculatePageInfo(int page, int size, long numberOfItems) {
        int numberOfPages = (int) Math.ceil((double) numberOfItems / size);
        int lastPage = numberOfPages - 1;
        int currentPage = Math.min(page, lastPage);
        return new PageInfo(numberOfItems, currentPage, lastPage, size);
    }

    private ImageMetadata checkForTimeout(ImageMetadata imageMetadata) {
        var status = imageMetadata.getStatus();
        Instant timeoutInstant = status.statusChangedAt().plus(imageProcessingProperties.timeout());

        if (status.stage().isInProgress() && timeoutInstant.isBefore(Instant.now())) {
            var updatedMetaData = setImageProcessingStage(imageMetadata, ImageProcessingStage.PROCESSING_TIMEOUT);
            objectStorageService.deleteImagesByParentKey(imageMetadata.getParentKey());
            return updatedMetaData;
        }

        return imageMetadata;
    }
}
