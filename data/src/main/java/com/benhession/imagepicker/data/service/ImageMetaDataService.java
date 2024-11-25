package com.benhession.imagepicker.data.service;

import com.benhession.imagepicker.common.config.ImageProcessingProperties;
import com.benhession.imagepicker.common.model.PageInfo;
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
        long numberOfItems = imageMetadataRepository.countNumberOfProcessedImages();
        int numberOfPages = (int) Math.ceil((double) numberOfItems / size);
        int lastPage = numberOfPages - 1;
        int currentPage = Math.min(page, lastPage);

        return new PageInfo(numberOfItems, currentPage, lastPage, size);
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

    private ImageMetadata checkForTimeout(ImageMetadata imageMetadata) {
        var status = imageMetadata.getStatus();
        Instant timeoutInstant = status.statusChangedAt().plus(imageProcessingProperties.timeout());

        if (ImageProcessingStage.isInProgress(status.stage()) && timeoutInstant.isBefore(Instant.now())) {
            var updatedMetaData = setImageProcessingStage(imageMetadata, ImageProcessingStage.PROCESSING_TIMEOUT);
            objectStorageService.deleteImagesByParentKey(imageMetadata.getParentKey());
            return updatedMetaData;
        }

        return imageMetadata;
    }
}
