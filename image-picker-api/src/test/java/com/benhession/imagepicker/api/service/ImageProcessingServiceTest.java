package com.benhession.imagepicker.api.service;

import static com.benhession.imagepicker.common.model.ImageType.RECTANGULAR;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benhession.imagepicker.api.sqs.ImageProcessingQueueService;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.common.util.FilenameUtil;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import com.benhession.imagepicker.testutil.TestFileLoader;
import io.quarkus.panache.common.exception.PanacheQueryException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
@RequiredArgsConstructor
public class ImageProcessingServiceTest {

    private static final String TEST_FILENAME = "test-image.jpg";

    private final TestFileLoader testFileLoader;
    private final ImageProcessingService imageProcessingService;

    @InjectMock
    ImageProcessingQueueService imageProcessingQueueService;
    @InjectMock
    ImageMetaDataService imageMetaDataService;
    @InjectMock
    ObjectStorageService objectStorageService;
    @InjectMock
    FilenameUtil filenameUtil;
    private ArgumentCaptor<ImageCreationMessage> creationMessageCaptor;
    private ArgumentCaptor<ImageMetadata> imageMetadataCaptor;

    private FileData testFileData;
    private String testParentKey;

    @BeforeEach
    public void init() {
        testFileData = FileData.builder()
            .data(testFileLoader.loadTestFile(TEST_FILENAME))
            .imageType(RECTANGULAR.toString())
            .mimeType("image/jpeg")
            .filename(TEST_FILENAME)
            .build();

        testParentKey = UUID.randomUUID() + "_" + TEST_FILENAME;
        imageMetadataCaptor = ArgumentCaptor.forClass(ImageMetadata.class);
        creationMessageCaptor = ArgumentCaptor.forClass(ImageCreationMessage.class);
    }

    @Test
    public void When_ProcessImage_Expect_ProcessingPrepDone() {
        // arrange
        when(filenameUtil.generateParentKey(eq(TEST_FILENAME)))
            .thenReturn(testParentKey);
        var mockMetaData = ImageMetadata.builder()
            .parentKey(testParentKey)
            .id(new ObjectId())
            .build();

        when(imageMetaDataService.findByParentKey(testParentKey))
            .thenReturn(Optional.of(mockMetaData));

        // act
        var returnedMetaData = imageProcessingService.processImage(testFileData);

        // assert
        assertThat(returnedMetaData).isEqualTo(mockMetaData);

        verify(objectStorageService, times(1))
            .uploadOriginalFileData(eq(testFileData), eq(testParentKey));

        verify(imageMetaDataService, times(1))
            .persist(imageMetadataCaptor.capture());

        var capturedMetadata = imageMetadataCaptor.getValue();
        assertThat(capturedMetadata.getParentKey())
            .isEqualTo(testParentKey);
        assertThat(capturedMetadata.getStatus().stage())
            .isEqualTo(ORIGINAL_UPLOADED);
        assertThat(capturedMetadata.getFilename())
            .isEqualTo(TEST_FILENAME);
        assertThat(capturedMetadata.getType())
            .isEqualTo(RECTANGULAR);

        verify(imageProcessingQueueService, times(1))
            .sendMessage(creationMessageCaptor.capture());

        var actualMessage = creationMessageCaptor.getValue();
        assertThat(actualMessage.getFileDataKey()).isEqualTo(testParentKey);

    }

    @Test
    public void When_ProcessImage_With_S3ImageProcessingException_Expect_StatusPersisted() {
        // arrange
        doThrow(ImageProcessingException.class)
            .when(objectStorageService).uploadOriginalFileData(eq(testFileData), eq(testParentKey));

        when(filenameUtil.generateParentKey(eq(TEST_FILENAME)))
            .thenReturn(testParentKey);

        var mockMetaData = ImageMetadata.builder()
            .parentKey(testParentKey)
            .status(ImageProcessingStatus.of(PROCESSING_FAILED))
            .id(new ObjectId())
            .build();

        when(imageMetaDataService.findByParentKey(testParentKey))
            .thenReturn(Optional.of(mockMetaData));

        // act
        var returnedMetaData = imageProcessingService.processImage(testFileData);
        assertThat(returnedMetaData).isEqualTo(mockMetaData);

        // assert
        verify(imageMetaDataService, times(1))
            .persist(imageMetadataCaptor.capture());

        var capturedMetadata = imageMetadataCaptor.getValue();
        assertThat(capturedMetadata.getParentKey())
            .isEqualTo(testParentKey);
        assertThat(capturedMetadata.getStatus().stage())
            .isEqualTo(PROCESSING_FAILED);
    }

    @Test
    public void When_ProcessImage_With_SendMessageImageProcessingException_Expect_StatusPersisted() {
        // arrange
        when(filenameUtil.generateParentKey(eq(TEST_FILENAME)))
            .thenReturn(testParentKey);
        doThrow(ImageProcessingException.class)
            .when(imageProcessingQueueService).sendMessage(any());

        var mockMetaData = ImageMetadata.builder()
            .parentKey(testParentKey)
            .status(ImageProcessingStatus.of(PROCESSING_FAILED))
            .id(new ObjectId())
            .build();
        when(imageMetaDataService.findByParentKey(testParentKey))
            .thenReturn(Optional.of(mockMetaData));

        // act
        var returnedMetaData = imageProcessingService.processImage(testFileData);

        // assert
        assertThat(returnedMetaData).isEqualTo(mockMetaData);
        verify(imageMetaDataService, times(2)).persist(imageMetadataCaptor.capture());

        List<ImageMetadata> capturedMetadata = imageMetadataCaptor.getAllValues();
        assertThat(capturedMetadata.getFirst().getParentKey())
            .isEqualTo(testParentKey);
        assertThat(capturedMetadata.getFirst().getStatus().stage())
            .isEqualTo(ORIGINAL_UPLOADED);

        assertThat(capturedMetadata.getLast().getParentKey())
            .isEqualTo(testParentKey);
        assertThat(capturedMetadata.getLast().getStatus().stage())
            .isEqualTo(PROCESSING_FAILED);
    }

    @Test
    public void When_ProcessImage_With_PersistThrows_Expect_ExceptionThrown() {
        // arrange
        doThrow(PanacheQueryException.class)
            .when(imageMetaDataService).persist(any(ImageMetadata.class));

        // act + assert
        assertThatThrownBy(() -> imageProcessingService.processImage(testFileData))
            .isInstanceOf(PanacheQueryException.class);

    }

    @Test
    public void When_ProcessImage_With_MetaDataNotSaved_Expect_ImageProcessingException() {
        // arrange
        when(filenameUtil.generateParentKey(eq(TEST_FILENAME)))
            .thenReturn(testParentKey);
        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> imageProcessingService.processImage(testFileData))
            .isInstanceOf(ImageProcessingException.class)
            .hasMessageContaining("Unable to save image metadata: parentKey = " + testParentKey);

    }

}
