package com.benhession.imagepicker.imageprocessor.service;

import static com.benhession.imagepicker.common.model.ImageType.RECTANGULAR;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;
import static org.assertj.core.api.Assertions.assertThat;

import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.model.ImageSize;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.imageprocessor.service.data.MongoDbHelper;
import com.benhession.imagepicker.imageprocessor.service.data.S3Helper;
import com.benhession.imagepicker.imageprocessor.service.security.AccessTokenHelper;
import com.benhession.imagepicker.imageprocessor.service.sqs.SqsTestHelper;
import io.quarkus.test.junit.QuarkusTest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProcessImageTest {

    private static final String TEST_FILENAME = "test-image.jpg";

    @Test
    public void testProcessImage() throws IOException, InterruptedException {
        // arrange

        // add original file to s3
        var parentKey = String.format("%s-%s", TEST_FILENAME, UUID.randomUUID());
        FileData fileData =
            new FileData(loadFile(TEST_FILENAME), TEST_FILENAME, "image/jpeg", "RECTANGULAR");
        S3Helper.uploadOriginalFile(fileData, parentKey);

        // add metadata to db
        var imageMetaData = ImageMetadata.builder()
            .status(ImageProcessingStatus.of(ORIGINAL_UPLOADED))
            .type(RECTANGULAR)
            .filename(TEST_FILENAME)
            .parentKey(parentKey)
            .build();
        var metaDataId = MongoDbHelper.addImageMetaData(imageMetaData);

        ImageCreationMessage message = ImageCreationMessage.builder()
            .metaDataId(metaDataId.toString())
            .fileDataKey(parentKey)
            .build();
        message.setAuthJwt(AccessTokenHelper.getToken());

        // act
        var sqsBatchResponse = SqsTestHelper.sendSqsEvent(message);

        // assert
        assertThat(sqsBatchResponse.getBatchItemFailures()).isEmpty();

        var updatedMetaData = MongoDbHelper.getImageMetaData(metaDataId);
        assertThat(updatedMetaData).isPresent();
        assertThat(updatedMetaData.orElseThrow().getStatus().stage()).isEqualTo(PROCESSING_COMPLETE);

        var s3Objects = S3Helper.listS3Objects(parentKey);
        assertThat(s3Objects.keyCount()).isEqualTo(ImageSize.values().length);
    }

    private byte[] loadFile(String filename) throws IOException {
        URL url = getClass().getClassLoader().getResource(filename);
        assert url != null;
        File file = new File(url.getFile());

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return fileInputStream.readAllBytes();
        }
    }
}
