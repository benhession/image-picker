package com.benhession.imagepicker.data.repository;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;

import com.benhession.imagepicker.data.model.ImageMetadata;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.Document;

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

    public List<ImageMetadata> searchImagesByFilenameAndTags(String searchTerm) {
        List<ImageMetadata> results = new ArrayList<>();

        Document searchQuery = new Document("$search",
            new Document("index", "fileMetadataSearchIndex")
                .append("text",
                    new Document("query", searchTerm)
                        .append("path", List.of("filename", "tags", "aiTags")))
                .append("text", new Document("query", PROCESSING_COMPLETE)
                    .append("path", List.of("status.stage")))
        );

        mongoCollection().aggregate(List.of(searchQuery))
            .forEach(results::add);

        return results;
    }
}
