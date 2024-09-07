package com.benhession.imagepicker.repository;

import com.benhession.imagepicker.model.ImageMetadata;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ImageMetadataRepository implements PanacheMongoRepository<ImageMetadata> {
    public Optional<ImageMetadata> findByParentKey(String key) {
        return find("parentKey", key)
                .firstResultOptional();
    }

    public List<ImageMetadata> findPage(int page, int size) {
        return findAll()
          .page(Page.of(page, size))
          .list();
    }
}
