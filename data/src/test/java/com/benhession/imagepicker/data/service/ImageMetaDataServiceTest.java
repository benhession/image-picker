package com.benhession.imagepicker.data.service;

import static com.benhession.imagepicker.common.model.ImageType.RECTANGULAR;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;
import static org.assertj.core.api.Assertions.assertThat;

import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.repository.ImageMetaDataRepository;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
public class ImageMetaDataServiceTest {
    private final ImageMetaDataService imageMetaDataService;
    private final ImageMetaDataRepository imageMetaDataRepository;

    @AfterEach
    public void tearDown() {
        imageMetaDataRepository.deleteAll();
    }

    @Test
    public void When_FindProcessedImages_With_SomeUnProcessed_Expect_OnlyProcessedReturned() {
        // arrange
        Arrays.stream(ImageProcessingStage.values())
            .forEach(this::addImageMetaDataWithStage);

        // act
        List<ImageMetadata> response = imageMetaDataService.findProcessedImages(0, 10);

        // assert
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getStatus().stage()).isEqualTo(PROCESSING_COMPLETE);
    }

    public void addImageMetaDataWithStage(ImageProcessingStage stage) {
        var mockMetaData = ImageMetadata.builder()
            .type(RECTANGULAR)
            .parentKey(UUID.randomUUID().toString())
            .status(ImageProcessingStatus.of(stage))
            .filename("test-file.png")
            .build();

        imageMetaDataRepository.persist(mockMetaData);
    }
}
