package com.benhession.imagepicker.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.benhession.imagepicker.data.repository.ImageMetaDataRepository;
import com.benhession.imagepicker.testutil.resources.MongoMigrationResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MongoMigrationResource.class)
@RequiredArgsConstructor
public class MigrationTest {

    private final ImageMetaDataRepository imageMetaDataRepository;

    @Test
    public void testMigration() {
        // arrange
        List<Document> indexesDocuments = new ArrayList<>();

        // act
        try (var cursor = imageMetaDataRepository.mongoCollection()
            .listIndexes()
            .cursor()) {
            while (cursor.hasNext()) {
                indexesDocuments.add(cursor.next());
            }
        }

        // assert
        assertThat(indexesDocuments).hasSize(2);
        Document document1 = indexesDocuments.getFirst();
        String index1Name = (String) document1.get("name");
        assertThat(index1Name).isEqualTo("_id_");
        Document document2 = indexesDocuments.getLast();
        String index2Name = (String) document2.get("name");
        assertThat(index2Name).isEqualTo("parentKey_1");
    }

}
