package com.benhession.imagepicker.repository;

import com.benhession.imagepicker.model.ImageMetadata;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ImageMetadataRepository implements PanacheMongoRepository<ImageMetadata> {
    public Optional<ImageMetadata> findByParentKey(String key) {
        return find("parentKey", key)
                .firstResultOptional();
    }
}
