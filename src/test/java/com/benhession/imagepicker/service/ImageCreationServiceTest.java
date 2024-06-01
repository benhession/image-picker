package com.benhession.imagepicker.service;

import static com.benhession.imagepicker.model.ImageType.LANDSCAPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benhession.imagepicker.dto.ImageUploadDto;
import com.benhession.imagepicker.dto.ObjectUploadForm;
import com.benhession.imagepicker.exception.BadRequestException;
import com.benhession.imagepicker.model.ImageMetadata;
import com.benhession.imagepicker.model.ImageSize;
import com.benhession.imagepicker.model.ImageType;
import com.benhession.imagepicker.repository.ImageMetadataRepository;
import com.benhession.imagepicker.testutil.TestFileLoader;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@QuarkusTest
public class ImageCreationServiceTest {
    private static final String PARENT_TEST_KEY = "parent-test-key";
    private static final String TEST_FILENAME = "test-filename.jpg";
    private static final String TEST_MIME_TYPE = "image/jpeg";
    private static final ImageType TEST_IMAGE_TYPE = LANDSCAPE;
    private static final ImageMetadata TEST_META_DATA = ImageMetadata.builder()
      .id(new ObjectId())
      .parentKey(PARENT_TEST_KEY)
      .filename(TEST_FILENAME)
      .type(TEST_IMAGE_TYPE)
      .build();

    @Inject
    TestFileLoader testFileLoader;

    @Inject
    ImageCreationService imageCreationService;

    @InjectMock
    ObjectStorageService objectStorageService;

    @InjectMock
    ImageMetadataRepository imageMetadataRepository;


    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ImageUploadDto>> imagesCaptor = ArgumentCaptor.forClass(List.class);

    @Test
    public void When_CreateNewImages_With_ValidJpg_Expect_ImagesUploadedAndMetaDataPersisted() {
        // arrange
        var objectUploadForm = ObjectUploadForm.builder()
          .data(testFileLoader.loadTestFile("test.jpeg"))
          .filename(TEST_FILENAME)
          .mimetype(TEST_MIME_TYPE)
          .imageType(TEST_IMAGE_TYPE.toString())
          .build();

        when(objectStorageService.uploadFiles(any(), any())).thenReturn(PARENT_TEST_KEY);
        when(imageMetadataRepository.findByParentKey(eq(PARENT_TEST_KEY))).thenReturn(Optional.of(TEST_META_DATA));

        // act
        ImageMetadata returnedMetadata = imageCreationService.createNewImages(objectUploadForm);

        // assert
        verify(objectStorageService, times(1)).uploadFiles(eq(TEST_FILENAME), imagesCaptor.capture());
        verify(imageMetadataRepository, times(1)).persist(eq(TEST_META_DATA));

        assertThat(returnedMetadata).isEqualTo(TEST_META_DATA);

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
    public void When_CreateNewImages_With_InvalidAspectRatio_Expect_BadRequestException() {
        var objectUploadForm = ObjectUploadForm.builder()
          .data(testFileLoader.loadTestFile("test.jpeg"))
          .filename(TEST_FILENAME)
          .mimetype(TEST_MIME_TYPE)
          .imageType("SQUARE")
          .build();

        assertThatThrownBy(() -> imageCreationService.createNewImages(objectUploadForm))
          .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Expected aspect ratio for image type: SQUARE to be 1.0, but was 1.78");
    }

    @Test
    public void When_CreateNewImages_With_InvalidWidth_Expect_BadRequestException() {

        var imageService = Mockito.mock(ImageSizeService.class);
        QuarkusMock.installMockForType(imageService, ImageSizeService.class);

        var objectUploadForm = ObjectUploadForm.builder()
          .data(testFileLoader.loadTestFile("test.jpeg"))
          .filename(TEST_FILENAME)
          .mimetype(TEST_MIME_TYPE)
          .imageType(TEST_IMAGE_TYPE.toString())
          .build();

        when(imageService.findAspectRatio(any())).thenReturn(new BigDecimal("1.78"));
        when(imageService.findMinWidth(any())).thenReturn(2000);


        assertThatThrownBy(() -> imageCreationService.createNewImages(objectUploadForm))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Expected width of LANDSCAPE image to be more that 2000");
    }

}
