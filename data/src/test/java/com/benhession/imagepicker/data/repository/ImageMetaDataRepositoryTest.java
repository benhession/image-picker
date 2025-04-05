package com.benhession.imagepicker.data.repository;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.testutil.resources.MongoMigrationResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@QuarkusTestResource(MongoMigrationResource.class)
@RequiredArgsConstructor
public class ImageMetaDataRepositoryTest {

    private final ImageMetaDataRepository imageMetaDataRepository;

    @AfterEach
    public void tearDown() {
        imageMetaDataRepository.deleteAll();
    }

    @Test
    void When_FindByParentKey_Expect_Retrievable() {
        // arrange
        var parentKey = UUID.randomUUID().toString();

        ImageMetadata imageMetadata =
            ImageMetadata.builder().parentKey(parentKey).filename("Bill-and-Teds-bogus-journey.jpg")
                .tags(List.of("adventure")).aiTags(List.of("turnip"))
                .status(ImageProcessingStatus.of(PROCESSING_COMPLETE)).build();

        // act
        imageMetaDataRepository.persist(imageMetadata);

        // assert
        Optional<ImageMetadata> result = imageMetaDataRepository.findByParentKey(parentKey);
        assertThat(result).isPresent();
        assertThat(result.get().getParentKey()).isEqualTo(parentKey);
    }

    @ParameterizedTest
    @MethodSource("testSearchTerms")
    void When_SearchByFilenameAndTags_WithMatches_Expect_Results(String searchTerm) throws InterruptedException {
        // arrange
        List<ImageMetadata> imageMetadata = List.of(
            ImageMetadata.builder()
                .parentKey(UUID.randomUUID().toString())
                .filename("Bill-and-Teds-bogus-journey.jpg")
                .tags(List.of("adventure")).aiTags(List.of("turnip"))
                .status(ImageProcessingStatus.of(PROCESSING_COMPLETE))
                .build(),
            ImageMetadata.builder()
                .parentKey(UUID.randomUUID().toString())
                .filename("Negative-result.png")
                .status(ImageProcessingStatus.of(PROCESSING_COMPLETE))
                .build()
        );

        imageMetaDataRepository.persist(imageMetadata);
        // sleep to give the database chance to index the new document
        Thread.sleep(2000);

        // act
        var results = imageMetaDataRepository.searchByFilenameAndTags(searchTerm, 0, 10);

        // assert
        assertThat(results).hasSize(1);
        var result = results.getFirst();

        assertThat(result).isEqualTo(imageMetadata.getFirst())
            .hasFieldOrProperty("id")
            .hasFieldOrPropertyWithValue("tags", imageMetadata.getFirst().getTags())
            .hasFieldOrPropertyWithValue("aiTags", imageMetadata.getFirst().getAiTags());
        assertThat(result.getStatus().stage()).isEqualTo(PROCESSING_COMPLETE);
    }

    private static Stream<Arguments> testSearchTerms() {
        return Stream.of(Arguments.of("bill"),
            Arguments.of("turnip"),
            Arguments.of("aDVenTure"),
            Arguments.of("JPG"));
    }

    @ParameterizedTest
    @EnumSource(value = ImageProcessingStage.class, names = "PROCESSING_COMPLETE", mode = EXCLUDE)
    void When_SearchByFilenameAndTags_With_InvalidProcessingStage_Expect_Results(
        ImageProcessingStage processingStage) throws InterruptedException {
        // arrange
        var imageMetadata =
            ImageMetadata.builder().parentKey(UUID.randomUUID().toString())
                .filename("Bill-and-Teds-bogus-journey.jpg")
                .aiTags(List.of("adventure"))
                .tags(List.of("turnip"))
                .status(ImageProcessingStatus.of(processingStage))
                .build();

        imageMetaDataRepository.persist(imageMetadata);
        // sleep to give the database chance to index the new document
        Thread.sleep(2000);

        // act
        var result = imageMetaDataRepository.searchByFilenameAndTags("bill", 0, 10);

        // assert
        assertThat(result).isEmpty();
    }

    @Test
    void When_countItemsForSearchByFilenameAndTags_WithMatches_Expect_CorrectCount() throws InterruptedException {
        // arrange
        var imageMetadata = List.of(
            ImageMetadata.builder().parentKey(UUID.randomUUID().toString())
                .filename("Bill-and-Teds-bogus-journey.jpg")
                .aiTags(List.of("adventure"))
                .tags(List.of("turnip"))
                .status(ImageProcessingStatus.of(PROCESSING_COMPLETE))
                .build(),
            ImageMetadata.builder().parentKey(UUID.randomUUID().toString())
                .filename("Shawshank Redemption.png")
                .aiTags(List.of("adventure", "parsnip"))
                .status(ImageProcessingStatus.of(PROCESSING_COMPLETE))
                .build(),
            ImageMetadata.builder().parentKey(UUID.randomUUID().toString())
                .filename("Happy Gilmore.gif")
                .aiTags(List.of("adventure"))
                .status(ImageProcessingStatus.of(PROCESSING_TIMEOUT))
                .build()
        );

        imageMetaDataRepository.persist(imageMetadata);
        // sleep to give the database chance to index the new document
        Thread.sleep(2000);

        // act
        long result = imageMetaDataRepository.countItemsForSearchByFilenameAndTags("adventure");

        // assert
        assertThat(result).isEqualTo(2);
    }
}
