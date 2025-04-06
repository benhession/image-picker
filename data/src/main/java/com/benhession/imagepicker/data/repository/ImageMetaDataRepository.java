package com.benhession.imagepicker.data.repository;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;

import com.benhession.imagepicker.data.model.ImageMetaDataSearchResult;
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

    public List<ImageMetaDataSearchResult> searchByFilenameAndTags(String searchTerm, int page, int size) {

        Document searchQuery = new Document("$search",
            new Document("index", "fileMetadataSearchIndex")
                .append("compound", getSearchByFilenameAndTagsCompoundQuery(searchTerm)));

        Document addFieldStage = new Document("$addFields",
            new Document("pointOfReference",
                new Document("$meta", "searchSequenceToken")));

        Document skipStage = new Document("$skip", page * size);
        Document limitStage = new Document("$limit", size);

        List<ImageMetaDataSearchResult> results = new ArrayList<>();
        mongoDatabase().getCollection("images", ImageMetaDataSearchResult.class)
            .aggregate(List.of(searchQuery, addFieldStage, skipStage, limitStage))
            .forEach(results::add);

        return results;
    }

    public List<ImageMetaDataSearchResult> searchByFilenameAndTagsAfter(String searchTerm, int size,
        String searchAfter) {

        Document searchQuery = new Document("$search",
            new Document("index", "fileMetadataSearchIndex")
                .append("compound", getSearchByFilenameAndTagsCompoundQuery(searchTerm))
                .append("searchAfter", searchAfter));

        return searchWithSize(searchQuery, size);
    }

    public List<ImageMetaDataSearchResult> searchByFilenameAndTagsBefore(String searchTerm, int size,
        String searchBefore) {

        Document searchQuery = new Document("$search",
            new Document("index", "fileMetadataSearchIndex")
                .append("compound", getSearchByFilenameAndTagsCompoundQuery(searchTerm))
                .append("searchBefore", searchBefore));

        return searchWithSize(searchQuery, size);
    }

    public long countItemsForSearchByFilenameAndTags(String searchTerm) {
        Document searchQuery = new Document("$searchMeta",
            new Document("index", "fileMetadataSearchIndex")
                .append("compound", getSearchByFilenameAndTagsCompoundQuery(searchTerm))
                .append("count", new Document("type", "total")));

        List<Document> results = new ArrayList<>();

        mongoDatabase().getCollection("images")
            .aggregate(List.of(searchQuery))
            .forEach(results::add);

        if (results.isEmpty()) {
            return 0;
        }

        return results.getFirst()
            .get("count", Document.class)
            .getLong("total");
    }

    private Document getSearchByFilenameAndTagsCompoundQuery(String searchTerm) {
        return
            new Document("must", List.of(
                new Document("text",
                    new Document("query", searchTerm)
                        .append("path", List.of("filename", "tags", "aiTags")))))
                .append("filter", List.of(
                    new Document("equals",
                        new Document("value", PROCESSING_COMPLETE)
                            .append("path", "status.stage"))));
    }

    private List<ImageMetaDataSearchResult> searchWithSize(Document searchStage, int size) {
        Document addFieldStage = new Document("$addFields",
            new Document("pointOfReference",
                new Document("$meta", "searchSequenceToken")));

        Document limitStage = new Document("$limit", size);

        List<ImageMetaDataSearchResult> results = new ArrayList<>();
        mongoDatabase().getCollection("images", ImageMetaDataSearchResult.class)
            .aggregate(List.of(searchStage, addFieldStage, limitStage))
            .forEach(results::add);

        return results;
    }
}
