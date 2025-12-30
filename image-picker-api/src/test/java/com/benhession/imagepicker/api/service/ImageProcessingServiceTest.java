package com.benhession.imagepicker.api.service;

import static com.benhession.imagepicker.common.model.ImageOrientation.LANDSCAPE;
import static com.benhession.imagepicker.common.model.ImageType.RECTANGULAR;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.INITIALISED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benhession.imagepicker.api.sqs.ImageProcessingQueueService;
import com.benhession.imagepicker.common.exception.BadRequestException;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import com.benhession.imagepicker.testutil.TestFileLoader;
import io.quarkus.panache.common.exception.PanacheQueryException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
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
    ImageValidationService imageValidationService;

    private ArgumentCaptor<ImageCreationMessage> creationMessageCaptor;
    private ArgumentCaptor<ImageMetadata> imageMetadataCaptor;

    private ImageMetadata inputImageMetadata;
    private FileData testFileData;
    private String testParentKey;
    private ObjectId testMetadataId;

    @BeforeEach
    public void init() throws IOException {
        testFileData = FileData.builder()
            .data(testFileLoader.loadTestFileBytes(TEST_FILENAME))
            .mimeType("image/jpeg")
            .filename(TEST_FILENAME)
            .build();

        testMetadataId = ObjectId.get();
        testParentKey = UUID.randomUUID() + "_" + TEST_FILENAME;
        imageMetadataCaptor = ArgumentCaptor.forClass(ImageMetadata.class);
        creationMessageCaptor = ArgumentCaptor.forClass(ImageCreationMessage.class);

        inputImageMetadata = ImageMetadata.builder()
            .id(testMetadataId)
            .filename(TEST_FILENAME)
            .status(ImageProcessingStatus.of(INITIALISED))
            .parentKey(testParentKey)
            .tags(List.of("test-tag"))
            .build();
    }

    @Test
    public void When_ValidateAndProcessUploadedImage_With_NoErrors_Expect_ProcessingStarted() {
        // arrange
        var expectedMetadata = inputImageMetadata.toBuilder()
            .type(RECTANGULAR)
            .status(ImageProcessingStatus.of(ORIGINAL_UPLOADED))
            .build();

        when(objectStorageService.getOriginalFileData(testParentKey)).thenReturn(testFileData);
        when(imageMetaDataService.findByParentKey(testParentKey))
            .thenReturn(Optional.of(expectedMetadata));

        // act
        var outputMetadata =
            imageProcessingService.validateAndProcessUploadedImage(RECTANGULAR, LANDSCAPE, inputImageMetadata);

        // assert
        assertThat(outputMetadata).isNotNull();
        assertThat(outputMetadata).isEqualTo(expectedMetadata);

        verify(imageMetaDataService, times(1)).persist(imageMetadataCaptor.capture());

        var capturedMetadata = imageMetadataCaptor.getValue();
        assertThat(capturedMetadata).isNotNull();
        assertThat(capturedMetadata.getId()).isEqualTo(testMetadataId);
        assertThat(capturedMetadata.getStatus().stage()).isEqualTo(ORIGINAL_UPLOADED);
        assertThat(capturedMetadata.getParentKey()).isEqualTo(testParentKey);
        assertThat(capturedMetadata.getTags()).isEqualTo(expectedMetadata.getTags());
        assertThat(capturedMetadata.getFilename()).isEqualTo(TEST_FILENAME);

        verify(imageProcessingQueueService, times(1))
            .sendMessage(creationMessageCaptor.capture());

        var imageCreationMessage = creationMessageCaptor.getValue();
        assertThat(imageCreationMessage.getFileDataKey()).isEqualTo(testParentKey);
        assertThat(imageCreationMessage.getMetaDataId()).isEqualTo(testMetadataId.toString());
    }

    @Test
    public void When_ValidateAndProcessUploadedImage_With_S3ImageProcessingException_Expect_StatusPersisted() {
        // arrange
        doThrow(ImageProcessingException.class)
            .when(objectStorageService).getOriginalFileData(testParentKey);

        var mockMetaData = ImageMetadata.builder()
            .parentKey(testParentKey)
            .status(ImageProcessingStatus.of(PROCESSING_FAILED))
            .id(new ObjectId())
            .build();

        when(imageMetaDataService.findByParentKey(testParentKey))
            .thenReturn(Optional.of(mockMetaData));

        // act
        var returnedMetaData =
            imageProcessingService.validateAndProcessUploadedImage(RECTANGULAR, LANDSCAPE, inputImageMetadata);
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
    public void When_ValidateAndProcessUploadedImage_With_SendMessageImageProcessingException_Expect_StatusPersisted() {
        // arrange
        doThrow(ImageProcessingException.class)
            .when(imageProcessingQueueService).sendMessage(any());

        var mockMetaData = ImageMetadata.builder()
            .parentKey(testParentKey)
            .status(ImageProcessingStatus.of(PROCESSING_FAILED))
            .id(new ObjectId())
            .type(RECTANGULAR)
            .build();
        when(imageMetaDataService.findByParentKey(testParentKey)).thenReturn(Optional.of(mockMetaData));
        when(objectStorageService.getOriginalFileData(testParentKey)).thenReturn(testFileData);

        // act
        var returnedMetaData =
            imageProcessingService.validateAndProcessUploadedImage(RECTANGULAR, LANDSCAPE, inputImageMetadata);

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
    public void When_ValidateAndProcessUploadedImage_With_PersistThrows_Expect_ExceptionThrown() {
        // arrange
        when(objectStorageService.getOriginalFileData(testParentKey)).thenReturn(testFileData);
        doThrow(PanacheQueryException.class)
            .when(imageMetaDataService).persist(any(ImageMetadata.class));

        // act + assert
        assertThatThrownBy(
            () -> imageProcessingService.validateAndProcessUploadedImage(RECTANGULAR, LANDSCAPE, inputImageMetadata))
            .isInstanceOf(PanacheQueryException.class);

    }

    @Test
    public void When_ValidateAndProcessUploadedImage_With_MetaDataNotSaved_Expect_ImageProcessingException() {
        // arrange
        when(objectStorageService.getOriginalFileData(testParentKey)).thenReturn(testFileData);
        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(
            () -> imageProcessingService.validateAndProcessUploadedImage(RECTANGULAR, LANDSCAPE, inputImageMetadata))
            .isInstanceOf(ImageProcessingException.class)
            .hasMessageContaining("Unable to save image metadata: parentKey = " + testParentKey);

    }

    @Test
    public void When_ValidateAndProcessUploadedImage_With_ValidationError_Expect_BadRequestException() {
        // arrange
        when(objectStorageService.getOriginalFileData(testParentKey)).thenReturn(testFileData);
        doThrow(BadRequestException.class).when(imageValidationService).validateInputImage(any(), any(), any());

        // act
        assertThatThrownBy(() ->
            imageProcessingService.validateAndProcessUploadedImage(RECTANGULAR, LANDSCAPE, inputImageMetadata))
            .isInstanceOf(BadRequestException.class);

    }
}
