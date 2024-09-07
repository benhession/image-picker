package com.benhession.imagepicker.service;

import com.benhession.imagepicker.model.ImageMetadata;
import com.benhession.imagepicker.model.PageInfo;
import com.benhession.imagepicker.repository.ImageMetadataRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImageMetaDataService {

    private final ImageMetadataRepository imageMetadataRepository;

    public Optional<ImageMetadata> getImageMetaData(ObjectId objectId) {
        return imageMetadataRepository.findByIdOptional(objectId);
    }

    public List<ImageMetadata> getImageMetaDataList(int page, int size) {
        return imageMetadataRepository.findPage(page, size);
    }

    public PageInfo getPageInfo(int page, int size) {
        long numberOfItems = imageMetadataRepository.count();
        int numberOfPages = (int) Math.ceil((double) numberOfItems / size);
        int lastPage = numberOfPages - 1;
        int currentPage = Math.min(page, lastPage);

        return new PageInfo(numberOfItems, currentPage, lastPage, size);
    }
}
