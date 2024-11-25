package com.benhession.imagepicker.data.repository;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;

import com.benhession.imagepicker.data.model.ImageMetadata;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ImageMetaDataRepository implements PanacheMongoRepository<ImageMetadata> {
    public Optional<ImageMetadata> findByParentKey(String key) {
        return find("parentKey", key)
          .firstResultOptional();
    }

    public List<ImageMetadata> findProcessedImages(int page, int size) {
        return find("status.stage", PROCESSING_COMPLETE)
          .page(Page.of(page, size))
          .list();
    }

    public long countNumberOfProcessedImages() {
        return count("status.stage", PROCESSING_COMPLETE);
    }
}
