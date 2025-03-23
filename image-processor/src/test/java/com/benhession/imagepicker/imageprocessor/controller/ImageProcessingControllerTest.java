package com.benhession.imagepicker.imageprocessor.controller;

import com.benhession.imagepicker.common.exception.MetaDataRetrievalException;
import com.benhession.imagepicker.common.model.FileData;
import static com.benhession.imagepicker.common.model.ImageType.RECTANGULAR;
import com.benhession.imagepicker.common.security.UserInfo;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.data.model.ImageMetadata;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.imageprocessor.service.FileDataRetrievalService;
import com.benhession.imagepicker.imageprocessor.service.ImageClassifierService;
import com.benhession.imagepicker.imageprocessor.service.ImageCreationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@RequiredArgsConstructor
public class ImageProcessingControllerTest {

    @InjectMock
    ImageMetaDataService imageMetaDataService;
    @InjectMock
    UserInfo userInfo;
    @InjectMock
    ImageClassifierService imageClassifierService;
    @InjectMock
    FileDataRetrievalService fileDataRetrievalService;
    @InjectMock
    @SuppressWarnings("unused")
    ImageCreationService imageCreationService;
    private final ImageProcessingController imageProcessingController;

    @Test
    void When_HandleProcessingRequest_With_MetaDataNotFound_Expect_MetaDataRetrievalException() {
        // arrange
        String metaDataId = ObjectId.get().toString();
        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.empty());
        ImageCreationMessage imageCreationMessage = ImageCreationMessage.builder()
            .metaDataId(metaDataId)
            .build();

        // act + assert
        assertThatThrownBy(() -> imageProcessingController.handleProcessingRequest(imageCreationMessage))
            .isInstanceOf(MetaDataRetrievalException.class)
            .hasMessage("Image metadata not found for imageId: %s", metaDataId);
    }

    @Test
    void When_HandleProcessingRequest_With_NonEditorUser_Expect_ProcessingFailed() {
        // arrange
        var imageMetadataStub = mock(ImageMetadata.class);
        var imageCreationMessageStub = ImageCreationMessage.builder()
            .metaDataId(ObjectId.get().toString())
            .build();
        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.of(imageMetadataStub));
        when(userInfo.isEditor()).thenReturn(false);

        // act
        imageProcessingController.handleProcessingRequest(imageCreationMessageStub);

        // assert
        verify(imageMetaDataService, times(1))
            .setImageProcessingStage(eq(imageMetadataStub), eq(PROCESSING_FAILED));
    }

    @Test
    void When_HandleProcessingRequest_With_InvalidProcessingState_Expect_ProcessingFailed() {
        // arrange
        ObjectId metadataId = ObjectId.get();
        var imageMetadataStub = ImageMetadata.builder()
            .id(metadataId)
            .status(ImageProcessingStatus.of(PROCESSING_COMPLETE))
            .build();
        var imageCreationMessageStub = ImageCreationMessage.builder()
            .metaDataId(metadataId.toString())
            .build();
        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.of(imageMetadataStub));
        when(userInfo.isEditor()).thenReturn(true);

        // act
        imageProcessingController.handleProcessingRequest(imageCreationMessageStub);

        // assert
        verify(imageMetaDataService, times(1))
            .setImageProcessingStage(eq(imageMetadataStub), eq(PROCESSING_FAILED));
    }

    @Test
    void When_HandleProcessingRequest_With_FiledataNotFound_Expect_ProcessingFailed() {
        // arrange
        ObjectId metadataId = ObjectId.get();
        var imageMetadataStub = ImageMetadata.builder()
            .id(metadataId)
            .status(ImageProcessingStatus.of(ORIGINAL_UPLOADED))
            .build();

        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.of(imageMetadataStub));
        when(userInfo.isEditor()).thenReturn(true);
        when(fileDataRetrievalService.retrieve(any())).thenReturn(Optional.empty());
        when(imageMetaDataService.setImageProcessingStage(eq(imageMetadataStub), eq(PROCESSING)))
            .thenReturn(imageMetadataStub);

        var imageCreationMessageStub = ImageCreationMessage.builder()
            .metaDataId(metadataId.toString())
            .build();

        // act
        imageProcessingController.handleProcessingRequest(imageCreationMessageStub);

        // assert
        verify(imageMetaDataService)
            .setImageProcessingStage(imageMetadataStub, PROCESSING);
        verify(imageMetaDataService)
            .setImageProcessingStage(imageMetadataStub, PROCESSING_FAILED);
        verify(imageMetaDataService, times(2))
            .setImageProcessingStage(any(), any());
    }

    @Test
    void When_HandleProcessingRequest_With_ClassifierExecutionException_Expect_ProcessingComplete()
        throws ExecutionException, InterruptedException {
        // arrange
        ObjectId metadataId = ObjectId.get();
        var imageMetadataStub = mock(ImageMetadata.class);
        @SuppressWarnings("unchecked")
        Future<List<String>> mockFuture = mock(Future.class);
        var fileDataMock = mock(FileData.class);

        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.of(imageMetadataStub));
        when(userInfo.isEditor()).thenReturn(true);
        when(fileDataRetrievalService.retrieve(any())).thenReturn(Optional.of(fileDataMock));
        when(imageMetaDataService.setImageProcessingStage(eq(imageMetadataStub), eq(PROCESSING)))
            .thenReturn(imageMetadataStub);
        when(imageMetadataStub.getId()).thenReturn(metadataId);
        when(imageMetadataStub.getStatus())
            .thenReturn(ImageProcessingStatus.of(ORIGINAL_UPLOADED))
            .thenReturn(ImageProcessingStatus.of(PROCESSING));
        when(imageMetadataStub.getParentKey()).thenReturn("parentKey");
        when(imageMetadataStub.getType()).thenReturn(RECTANGULAR);
        when(imageClassifierService.findClassifiersAsync("originalFileData/testKey"))
            .thenReturn(mockFuture);
        when(mockFuture.get()).thenThrow(ExecutionException.class);

        var imageCreationMessageStub = ImageCreationMessage.builder()
            .metaDataId(metadataId.toString())
            .fileDataKey("testKey")
            .build();

        // act
        imageProcessingController.handleProcessingRequest(imageCreationMessageStub);

        // assert
        verify(imageMetaDataService)
            .setImageProcessingStage(imageMetadataStub, PROCESSING);
        verify(imageMetaDataService)
            .setImageProcessingStage(imageMetadataStub, PROCESSING_COMPLETE);
        verify(imageMetaDataService, times(2))
            .setImageProcessingStage(any(), any());
    }

    @Test
    void When_HandleProcessingRequest_With_NoError_Expect_ProcessingComplete()
        throws ExecutionException, InterruptedException {
        // arrange
        ObjectId metadataId = ObjectId.get();
        var imageMetadataStub = mock(ImageMetadata.class);
        @SuppressWarnings("unchecked")
        Future<List<String>> mockFuture = mock(Future.class);
        var fileDataMock = mock(FileData.class);

        when(imageMetaDataService.getImageMetaData(any())).thenReturn(Optional.of(imageMetadataStub));
        when(userInfo.isEditor()).thenReturn(true);
        when(fileDataRetrievalService.retrieve(any())).thenReturn(Optional.of(fileDataMock));
        when(imageMetaDataService.setImageProcessingStage(eq(imageMetadataStub), eq(PROCESSING)))
            .thenReturn(imageMetadataStub);
        when(imageMetadataStub.getId()).thenReturn(metadataId);
        when(imageMetadataStub.getStatus())
            .thenReturn(ImageProcessingStatus.of(ORIGINAL_UPLOADED))
            .thenReturn(ImageProcessingStatus.of(PROCESSING));
        when(imageMetadataStub.getType()).thenReturn(RECTANGULAR);
        when(imageClassifierService.findClassifiersAsync("originalFileData/testKey"))
            .thenReturn(mockFuture);
        when(mockFuture.get()).thenReturn(List.of());

        var imageCreationMessageStub = ImageCreationMessage.builder()
            .metaDataId(metadataId.toString())
            .fileDataKey("testKey")
            .build();

        // act
        imageProcessingController.handleProcessingRequest(imageCreationMessageStub);

        // assert
        verify(imageMetaDataService)
            .setImageProcessingStage(imageMetadataStub, PROCESSING);
        verify(imageMetaDataService)
            .setImageProcessingStage(imageMetadataStub, PROCESSING_COMPLETE);
        verify(imageMetaDataService, times(2))
            .setImageProcessingStage(any(), any());
    }
}
