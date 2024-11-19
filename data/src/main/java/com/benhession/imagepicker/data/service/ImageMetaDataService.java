package com.benhession.imagepicker.data.service;

import com.benhession.imagepicker.common.model.PageInfo;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.repository.ImageMetaDataRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageMetaDataService {
    private final ImageMetaDataRepository imageMetadataRepository;

    public Optional<ImageMetadata> getImageMetaData(ObjectId objectId) {
        return imageMetadataRepository.findByIdOptional(objectId);
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
        return imageMetadataRepository.findByParentKey(parentKey);
    }

    public ImageMetadata setImageProcessingStage(ImageMetadata imageMetadata,
        ImageProcessingStage imageProcessingStage) {

        imageMetadata.setStatus(ImageProcessingStatus.of(imageProcessingStage));
        persist(imageMetadata);
        return imageMetadata;
    }


}
