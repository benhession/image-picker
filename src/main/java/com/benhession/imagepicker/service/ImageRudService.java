package com.benhession.imagepicker.service;

import com.benhession.imagepicker.model.ImageMetadata;
import com.benhession.imagepicker.repository.ImageMetadataRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImageRudService {

    private final ImageMetadataRepository imageMetadataRepository;

    public Optional<ImageMetadata> getImageMetaData(ObjectId objectId) {
        return imageMetadataRepository.findByIdOptional(objectId);
    }
}
