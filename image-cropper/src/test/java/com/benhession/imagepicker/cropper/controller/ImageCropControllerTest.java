package com.benhession.imagepicker.cropper.controller;

import static com.benhession.imagepicker.common.model.ImageType.RECTANGULAR;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.SENT_TO_CROP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.exception.MetaDataRetrievalException;
import com.benhession.imagepicker.common.model.Coordinate;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.model.ImageCropProperties;
import com.benhession.imagepicker.common.sqs.ImageCropMessage;
import com.benhession.imagepicker.cropper.service.ImageCropService;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
@RequiredArgsConstructor
public class ImageCropControllerTest {

    private final ImageCropController imageCropController;

    @InjectMock
    ImageCropService imageCropService;
    @InjectMock
    ImageMetaDataService imageMetaDataService;
    @InjectMock
    ObjectStorageService objectStorageService;

    private ArgumentCaptor<ImageMetadata> imageMetadataArgumentCaptor;
    private ArgumentCaptor<FileData> fileDataArgumentCaptor;

    @BeforeEach
    public void init() {
        imageMetadataArgumentCaptor = ArgumentCaptor.forClass(ImageMetadata.class);
        fileDataArgumentCaptor = ArgumentCaptor.forClass(FileData.class);
    }

    @Test
    public void When_HandleCropRequest_With_MetaDataNotFound_Expect_MetadataRetrievalException() {

        // arrange
        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.empty());
        var imageCropMessage = getGenericImageCropMessage();

        // act + assert
        assertThatThrownBy(() -> imageCropController.handleCropQueueRequest(imageCropMessage))
            .isInstanceOf(MetaDataRetrievalException.class)
            .hasMessageContaining("Unable to find metadata for id: " + imageCropMessage.getMetaDataId());

        verify(imageMetaDataService, times(0)).setImageProcessingStage(any(), any());
    }

    @Test
    public void When_HandleCropQueueRequest_With_InvalidProcessingStatus_Expect_ImageProcessing_Exception() {
        // arrange
        var imageCropMessage = getGenericImageCropMessage();
        var imageMetadata = getImageMetadata(PROCESSING_COMPLETE);

        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.of(imageMetadata));

        // act
        imageCropController.handleCropQueueRequest(imageCropMessage);

        // assert
        verify(imageMetaDataService, times(1))
            .setImageProcessingStage(imageMetadataArgumentCaptor.capture(), eq(PROCESSING_FAILED));

        var capturedMetadata = imageMetadataArgumentCaptor.getValue();
        assertThat(capturedMetadata).isEqualTo(imageMetadata);
    }

    @Test
    public void When_HandleCropQueueRequest_With_ObjectStorageImageProcessingException_Expect_ProcessFailed() {
        // arrange
        var imageCropMessage = getGenericImageCropMessage();
        var imageMetadata = getImageMetadata(SENT_TO_CROP);

        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.of(imageMetadata));
        when(objectStorageService.getOriginalFileData(eq(imageMetadata.getParentKey())))
            .thenThrow(new ImageProcessingException("test exception"));

        // act
        imageCropController.handleCropQueueRequest(imageCropMessage);

        // assert
        verify(imageMetaDataService, times(1))
            .setImageProcessingStage(imageMetadataArgumentCaptor.capture(), eq(PROCESSING_FAILED));

        var capturedMetadata = imageMetadataArgumentCaptor.getValue();
        assertThat(capturedMetadata).isEqualTo(imageMetadata);
    }

    @Test
    public void When_HandleCropRequest_With_ValidRequest_Expect_Success() {
        // arrange
        var imageCropMessage = getGenericImageCropMessage();
        var imageMetadata = getImageMetadata(SENT_TO_CROP);
        var fileData = FileData.builder()
            .filename(UUID.randomUUID().toString())
            .build();

        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.of(imageMetadata));
        when(objectStorageService.getOriginalFileData(eq(imageMetadata.getParentKey())))
            .thenReturn(fileData);

        // act
        imageCropController.handleCropQueueRequest(imageCropMessage);

        // assert
        verify(imageMetaDataService, times(0))
            .setImageProcessingStage(any(), eq(PROCESSING_FAILED));
        verify(imageCropService, times(1))
            .cropOriginalImage(fileDataArgumentCaptor.capture(), eq(imageMetadata),
                eq(imageCropMessage.getImageCropProperties()));

        var capturedFileData = fileDataArgumentCaptor.getValue();
        assertThat(capturedFileData).isEqualTo(fileData);
    }

    public ImageCropMessage getGenericImageCropMessage() {
        return ImageCropMessage.builder()
            .metaDataId(ObjectId.get().toString())
            .fileDataKey(UUID.randomUUID().toString())
            .imageCropProperties(ImageCropProperties.builder()
                .baseCoordinate(new Coordinate(0, 0))
                .width(400)
                .imageType(RECTANGULAR)
                .build())
            .build();
    }

    public ImageMetadata getImageMetadata(ImageProcessingStage imageProcessingStage) {
        return ImageMetadata.builder()
            .id(new ObjectId())
            .parentKey(UUID.randomUUID().toString())
            .type(RECTANGULAR)
            .filename("filename")
            .status(ImageProcessingStatus.of(imageProcessingStage))
            .build();
    }
}
