package com.benhession.imagepicker.imageprocessor.service;

import static com.benhession.imagepicker.common.model.ImageType.LANDSCAPE;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.model.ImageSize;
import com.benhession.imagepicker.common.model.ImageType;
import com.benhession.imagepicker.common.util.FilenameUtil;
import com.benhession.imagepicker.data.dto.ImageUploadDto;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import com.benhession.imagepicker.testutil.TestFileLoader;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
public class ImageCreationServiceTest {
    private static final String PARENT_TEST_KEY = "parent-test-key";
    private static final String TEST_FILENAME = "test-filename.jpg";
    private static final String TEST_MIME_TYPE = "image/jpeg";
    private static final ImageType TEST_IMAGE_TYPE = LANDSCAPE;

    @Inject
    TestFileLoader testFileLoader;
    @Inject
    ImageCreationService imageCreationService;

    @InjectMock
    ObjectStorageService objectStorageService;
    @InjectMock
    FilenameUtil filenameUtil;

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ImageUploadDto>> imagesCaptor = ArgumentCaptor.forClass(List.class);

    @Test
    public void When_CreateNewImages_With_ValidJpg_Expect_ImagesUploadedAndMetaDataPersisted() throws IOException {
        // arrange
        ImageMetadata imageMetadata = ImageMetadata.builder()
            .id(ObjectId.get())
            .type(TEST_IMAGE_TYPE)
            .filename(TEST_FILENAME)
            .parentKey(PARENT_TEST_KEY)
            .status(ImageProcessingStatus.of(ORIGINAL_UPLOADED))
            .build();

        FileData fileData = new FileData(testFileLoader.loadTestFileBytes("test.jpeg"),
            TEST_FILENAME,
            TEST_MIME_TYPE,
            TEST_IMAGE_TYPE.toString());

        when(filenameUtil.getFilename(any(), any(), any())).thenCallRealMethod();
        when(filenameUtil.generateParentKey(TEST_FILENAME)).thenReturn(PARENT_TEST_KEY);

        // act
        imageCreationService.createNewImages(fileData, imageMetadata);

        // assert
        verify(objectStorageService, times(1)).uploadFiles(imagesCaptor.capture(), any());

        List<ImageUploadDto> capturedImages = imagesCaptor.getValue();
        assertThat(capturedImages.size()).isEqualTo(ImageSize.values().length);

        List<String> expectedFilenames = Arrays.stream(ImageSize.values())
            .map(imageSize -> String.format("%s-%s-%s", imageSize, TEST_IMAGE_TYPE, TEST_FILENAME))
            .toList();
        List<String> filenames = capturedImages.stream()
            .map(ImageUploadDto::filename)
            .toList();
        assertThat(filenames).isEqualTo(expectedFilenames);

        boolean allHaveExpectedMimeType = capturedImages.stream()
            .map(ImageUploadDto::mimetype)
            .allMatch(mimetype -> mimetype.equals(TEST_MIME_TYPE));
        assertThat(allHaveExpectedMimeType).isTrue();

        boolean allHaveData = capturedImages.stream()
            .map(ImageUploadDto::image)
            .allMatch(image -> image.length > 0);
        assertThat(allHaveData).isTrue();
    }

    @Test
    public void When_CreateImages_With_InvalidProcessingStage_Expect_ImageProcessingExceptionThrown()
        throws IOException {
        // arrange
        ImageMetadata imageMetadata = ImageMetadata.builder()
            .id(ObjectId.get())
            .type(TEST_IMAGE_TYPE)
            .filename(TEST_FILENAME)
            .parentKey(PARENT_TEST_KEY)
            .status(ImageProcessingStatus.of(PROCESSING))
            .build();

        FileData fileData = new FileData(testFileLoader.loadTestFileBytes("test.jpeg"),
            TEST_FILENAME,
            TEST_MIME_TYPE,
            TEST_IMAGE_TYPE.toString());

        // act + assert
        assertThatThrownBy(() -> imageCreationService.createNewImages(fileData, imageMetadata))
            .isInstanceOf(ImageProcessingException.class)
            .hasMessage("Image metadata is not in the processing stage for imageId: " + imageMetadata.getId());
    }
}