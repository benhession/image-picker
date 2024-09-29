package com.benhession.imagepicker.api.service;

import static com.benhession.imagepicker.data.model.ImageType.LANDSCAPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.benhession.imagepicker.api.dto.ObjectUploadForm;
import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException;
import com.benhession.imagepicker.common.exception.BadRequestException;
import com.benhession.imagepicker.data.model.ImageType;
import com.benhession.imagepicker.testutil.TestFileLoader;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@RequiredArgsConstructor
public class ImageValidationServiceTest {

    private static final String TEST_FILENAME = "test-filename.jpg";
    private static final String TEST_MIME_TYPE = "image/jpeg";
    private static final ImageType TEST_IMAGE_TYPE = LANDSCAPE;

    private final TestFileLoader testFileLoader;
    private final ImageValidationService imageValidationService;

    @Test
    public void When_CreateNewImages_With_InvalidAspectRatio_Expect_BadRequestException() {
        var objectUploadForm = ObjectUploadForm.builder()
            .data(testFileLoader.loadTestFile("test.jpeg"))
            .filename(TEST_FILENAME)
            .mimetype(TEST_MIME_TYPE)
            .imageType("SQUARE")
            .build();

        assertThatThrownBy(() -> imageValidationService.validateInputImage(objectUploadForm))
            .isInstanceOf(BadRequestException.class)
            .matches(e -> ((BadRequestException) e).getErrorMessages().stream()
                    .map(AbstractMultipleErrorApplicationException.ErrorMessage::message)
                    .anyMatch(message ->
                        message.equals("Expected aspect ratio for image type: SQUARE to be 1.0, but was 1.78")),
                "has expected message");
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

        assertThatThrownBy(() -> imageValidationService.validateInputImage(objectUploadForm))
            .isInstanceOf(BadRequestException.class)
            .matches(e -> ((BadRequestException) e).getErrorMessages().stream()
                    .map(AbstractMultipleErrorApplicationException.ErrorMessage::message)
                    .anyMatch(message ->
                        message.equals("Expected width of LANDSCAPE image to be more that 2000, but was 800")),
                "has expected message");
    }

    @Test
    public void When_ValidateImage_With_InvalidMimeType_Expect_BadRequestException() {
        var objectUploadForm = ObjectUploadForm.builder()
            .data(testFileLoader.loadTestFile("test.jpeg"))
            .filename(TEST_FILENAME)
            .mimetype("text/plain")
            .imageType("SQUARE")
            .build();

        assertThatThrownBy(() -> imageValidationService.validateInputImage(objectUploadForm))
            .isInstanceOf(BadRequestException.class)
            .extracting(BadRequestException.class::cast)
            .extracting(BadRequestException::getErrorMessages)
            .satisfies(errorMessages -> assertThat(errorMessages).hasSize(1))
            .extracting(List::getFirst)
            .satisfies(
                errorMessage -> assertThat(errorMessage.message()).contains("Mime type must be one of the following"));
    }
}
