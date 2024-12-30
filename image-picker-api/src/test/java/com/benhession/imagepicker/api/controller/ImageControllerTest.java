package com.benhession.imagepicker.api.controller;

import static com.benhession.imagepicker.common.model.ImageSize.LARGE;
import static com.benhession.imagepicker.common.model.ImageSize.MEDIUM;
import static com.benhession.imagepicker.common.model.ImageSize.SMALL;
import static com.benhession.imagepicker.common.model.ImageSize.THUMBNAIL;
import static com.benhession.imagepicker.common.model.ImageType.RECTANGULAR;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.INITIALISED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_TIMEOUT;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benhession.imagepicker.api.dto.ImageResponseDto;
import com.benhession.imagepicker.api.dto.UploadUrlResponseDto;
import com.benhession.imagepicker.api.exception.ErrorResponse;
import com.benhession.imagepicker.api.service.ImageProcessingService;
import com.benhession.imagepicker.api.service.ImageValidationService;
import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException.ErrorMessage;
import com.benhession.imagepicker.common.exception.BadRequestException;
import com.benhession.imagepicker.common.model.PageInfo;
import com.benhession.imagepicker.data.dto.PreSignedUploadDto;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import com.benhession.imagepicker.testutil.TestFileLoader;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.Header;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestHTTPEndpoint(ImageController.class)
public class ImageControllerTest {

    private static final String RECTANGULAR_TEST_IMAGE_NAME = "test-image.jpg";
    private static final String JPEG_MIME_TYPE = "image/jpeg";
    private static final List<String> TEST_TAGS = List.of("test", "another tag");

    private static final List<ImageMetadata> FIVE_METADATA_RESULTS = List.of(
        ImageMetadata.builder()
            .id(ObjectId.get())
            .build(),
        ImageMetadata.builder()
            .id(ObjectId.get())
            .build(),
        ImageMetadata.builder()
            .id(ObjectId.get())
            .build(),
        ImageMetadata.builder()
            .id(ObjectId.get())
            .build(),
        ImageMetadata.builder()
            .id(ObjectId.get())
            .build()
    );

    @Inject
    TestFileLoader testFileLoader;
    @InjectMock
    ImageProcessingService imageProcessingService;
    @InjectMock
    ImageMetaDataService imageMetaDataService;
    @InjectMock
    ObjectStorageService objectStorageService;
    @InjectMock
    ImageValidationService imageValidationService;

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin"})
    public void When_ProcessImage_With_MetaDataNotFound_Expect_NotFoundException() {
        // arrange
        ObjectId testImageId = ObjectId.get();
        when(imageMetaDataService.getImageMetaData(testImageId)).thenReturn(Optional.empty());

        // act
        ErrorResponse errorResponse = given()
            .multiPart("image-type", RECTANGULAR)
            .when()
            .post(String.format("/%s/process", testImageId))
            .then()
            .statusCode(404)
            .extract()
            .body()
            .as(ErrorResponse.class);

        // assert
        assertThat(errorResponse.getErrors()).hasSize(1);
        assertThat(errorResponse.getErrors().getFirst().getMessage())
            .isEqualTo("Unable to find metadata for image id " + testImageId);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin"})
    public void When_ProcessImage_With_ValidStage_Expect_SentToBeProcessed() {
        // arrange
        ObjectId testImageId = ObjectId.get();
        var inputMetadata = ImageMetadata.builder()
            .parentKey("test")
            .id(testImageId)
            .status(ImageProcessingStatus.of(INITIALISED))
            .filename(RECTANGULAR_TEST_IMAGE_NAME)
            .build();

        var outputMetadata = ImageMetadata.builder()
            .parentKey("test")
            .id(testImageId)
            .status(ImageProcessingStatus.of(PROCESSING))
            .filename(RECTANGULAR_TEST_IMAGE_NAME)
            .build();

        when(imageMetaDataService.getImageMetaData(testImageId)).thenReturn(Optional.of(inputMetadata));
        when(imageProcessingService.validateAndProcessUploadedImage(any(), any()))
            .thenReturn(outputMetadata);

        // act
        ImageResponseDto responseDto = given()
            .multiPart("image-type", RECTANGULAR)
            .when()
            .post(String.format("/%s/process", testImageId))
            .then()
            .statusCode(202)
            .extract()
            .body()
            .as(ImageResponseDto.class);

        // assert
        verify(imageProcessingService, times(1))
            .validateAndProcessUploadedImage(eq(RECTANGULAR), eq(inputMetadata));

        assertThat(responseDto.getId()).isEqualTo(testImageId.toString());
        assertThat(responseDto.getStatus().stage()).isEqualTo(PROCESSING);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin"})
    public void When_ProcessImage_With_InvalidStage_Expect_BadRequestResponseAndMessage() {
        // arrange
        ObjectId testImageId = ObjectId.get();
        var inputMetadata = ImageMetadata.builder()
            .parentKey("test")
            .id(testImageId)
            .status(ImageProcessingStatus.of(PROCESSING))
            .filename(RECTANGULAR_TEST_IMAGE_NAME)
            .build();

        when(imageMetaDataService.getImageMetaData(testImageId)).thenReturn(Optional.of(inputMetadata));

        // act
        var errorResponse = given()
            .multiPart("image-type", RECTANGULAR)
            .when()
            .post(String.format("/%s/process", testImageId))
            .then()
            .statusCode(400)
            .extract()
            .body()
            .as(ErrorResponse.class);

        // assert
        assertThat(errorResponse.getErrors()).hasSize(1);
        assertThat(errorResponse.getErrors().getFirst().getMessage())
            .isEqualTo("Expected image processing stage to be one of [INITIALISED] but was PROCESSING");
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"Everyone"})
    public void When_ProcessImage_With_UnauthorisedUser_Expect_ForbiddenResponse() {
        // act
        given()
            .multiPart("image-type", RECTANGULAR)
            .when()
            .post(String.format("/%s/process", ObjectId.get()))
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin"})
    public void When_GetImages_With_ResultsOnOnePage_Expect_ResultsAndCorrectHeaders() {
        when(imageMetaDataService.findProcessedImagesPageInfo(eq(0), eq(5)))
            .thenReturn(PageInfo.builder()
                .page(0)
                .size(5)
                .numberItems(5)
                .lastPage(0)
                .build());
        when(imageMetaDataService.findProcessedImages(0, 5))
            .thenReturn(FIVE_METADATA_RESULTS);
        when(imageMetaDataService.findProcessedImagesPageInfo(eq(0), eq(5)))
            .thenReturn(new PageInfo(5, 0, 0, 5));
        when(objectStorageService.getBaseResourcePath(any())).thenReturn("test-path");

        var response = given()
            .queryParam("page", 0)
            .queryParam("size", 5)
            .when()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .response();

        var resultsList = Arrays.asList(response.body()
            .as(ImageResponseDto[].class));
        assertThat(resultsList.size()).isEqualTo(5);

        var linkHeaders = response.headers().getList("link");
        Optional<String> currentHeader = getHeaderWithRel("current", linkHeaders);
        Optional<String> firstHeader = getHeaderWithRel("first", linkHeaders);
        Optional<String> lastHeader = getHeaderWithRel("last", linkHeaders);

        assertThat(currentHeader.isPresent()).isTrue();
        assertThat(currentHeader.get()).contains("page=0&size=5");
        assertThat(firstHeader.isPresent()).isTrue();
        assertThat(firstHeader.get()).contains("page=0&size=5");
        assertThat(lastHeader.isPresent()).isTrue();
        assertThat(lastHeader.get()).contains("page=0&size=5");
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin"})
    public void When_GetImages_With_MultiplePages_Expect_ResultsAndCorrectHeaders() {
        when(imageMetaDataService.findProcessedImagesPageInfo(eq(0), eq(3)))
            .thenReturn(PageInfo.builder()
                .page(1)
                .size(3)
                .numberItems(5)
                .lastPage(1)
                .build());
        when(imageMetaDataService.findProcessedImages(0, 3))
            .thenReturn(FIVE_METADATA_RESULTS.subList(0, 3));
        when(imageMetaDataService.findProcessedImagesPageInfo(eq(0), eq(3)))
            .thenReturn(new PageInfo(5, 0, 1, 3));
        when(objectStorageService.getBaseResourcePath(any())).thenReturn("test-path");

        var response = given()
            .queryParam("page", 0)
            .queryParam("size", 3)
            .when()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .response();

        var resultsList = Arrays.asList(response.body()
            .as(ImageResponseDto[].class));
        assertThat(resultsList.size()).isEqualTo(3);

        var linkHeaders = response.headers().getList("link");
        Optional<String> currentHeader = getHeaderWithRel("current", linkHeaders);
        Optional<String> firstHeader = getHeaderWithRel("first", linkHeaders);
        Optional<String> lastHeader = getHeaderWithRel("last", linkHeaders);
        Optional<String> next = getHeaderWithRel("next", linkHeaders);

        assertThat(currentHeader.isPresent()).isTrue();
        assertThat(currentHeader.get()).contains("page=0&size=3");
        assertThat(firstHeader.isPresent()).isTrue();
        assertThat(firstHeader.get()).contains("page=0&size=3");
        assertThat(lastHeader.isPresent()).isTrue();
        assertThat(lastHeader.get()).contains("page=1&size=3");
        assertThat(next.isPresent()).isTrue();
        assertThat(next.get()).contains("page=1&size=3");
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin"})
    public void When_GetImages_With_PageIsLastPage_Expect_ResultsAndCorrectHeaders() {
        when(imageMetaDataService.findProcessedImagesPageInfo(eq(1), eq(3)))
            .thenReturn(PageInfo.builder()
                .page(1)
                .size(3)
                .numberItems(5)
                .lastPage(1)
                .build());
        when(imageMetaDataService.findProcessedImages(1, 3))
            .thenReturn(FIVE_METADATA_RESULTS.subList(3, 5));
        when(imageMetaDataService.findProcessedImagesPageInfo(eq(1), eq(3)))
            .thenReturn(new PageInfo(5, 1, 1, 3));
        when(objectStorageService.getBaseResourcePath(any())).thenReturn("test-path");

        var response = given()
            .queryParam("page", 1)
            .queryParam("size", 3)
            .when()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .response();

        var resultsList = Arrays.asList(response.body()
            .as(ImageResponseDto[].class));
        assertThat(resultsList.size()).isEqualTo(2);

        var linkHeaders = response.headers().getList("link");
        Optional<String> currentHeader = getHeaderWithRel("current", linkHeaders);
        Optional<String> firstHeader = getHeaderWithRel("first", linkHeaders);
        Optional<String> lastHeader = getHeaderWithRel("last", linkHeaders);
        Optional<String> previous = getHeaderWithRel("previous", linkHeaders);

        assertThat(currentHeader.isPresent()).isTrue();
        assertThat(currentHeader.get()).contains("page=1&size=3");
        assertThat(firstHeader.isPresent()).isTrue();
        assertThat(firstHeader.get()).contains("page=0&size=3");
        assertThat(lastHeader.isPresent()).isTrue();
        assertThat(lastHeader.get()).contains("page=1&size=3");
        assertThat(previous.isPresent()).isTrue();
        assertThat(previous.get()).contains("page=0&size=3");
    }

    @Test
    @TestSecurity(user = "unauthorisedTestUser", roles = {"Everyone"})
    public void When_GetImages_With_UnknownUser_Expect_Forbidden() {
        given()
            .queryParam("page", 1)
            .queryParam("size", 3)
            .when()
            .get()
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin", "Everyone"})
    public void When_GetImage_With_ProcessingComplete_Expect_OkAndImageUrls() {
        // arrange
        var testId = ObjectId.get();
        var testParentKey = String.format("%s-%s", RECTANGULAR_TEST_IMAGE_NAME, UUID.randomUUID());
        var testStatus = ImageProcessingStatus.of(PROCESSING_COMPLETE);
        var testBasePath = "test-path";
        when(imageMetaDataService.getImageMetaData(testId))
            .thenReturn(Optional.of(ImageMetadata.builder()
                .id(testId)
                .type(RECTANGULAR)
                .filename(RECTANGULAR_TEST_IMAGE_NAME)
                .parentKey(testParentKey)
                .status(testStatus)
                .build()));

        when(objectStorageService.getBaseResourcePath(eq(testParentKey)))
            .thenReturn(testBasePath);

        // act
        var response = given()
            .get("/" + testId)
            .then()
            .statusCode(200)
            .extract()
            .as(ImageResponseDto.class);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testId.toString());
        assertThat(response.getType()).isEqualTo(RECTANGULAR);
        assertThat(response.getFilename()).isEqualTo(RECTANGULAR_TEST_IMAGE_NAME);
        assertThat(response.getStatus()).isEqualTo(testStatus);
        assertThat(response.getImages()).hasSize(4);

        var responseImages = response.getImages();
        assertThat(responseImages.get(THUMBNAIL)).isEqualTo(
            String.format("%s/%s-%s-%s", testBasePath, THUMBNAIL, RECTANGULAR, RECTANGULAR_TEST_IMAGE_NAME));
        assertThat(responseImages.get(SMALL)).isEqualTo(
            String.format("%s/%s-%s-%s", testBasePath, SMALL, RECTANGULAR, RECTANGULAR_TEST_IMAGE_NAME));
        assertThat(responseImages.get(MEDIUM)).isEqualTo(
            String.format("%s/%s-%s-%s", testBasePath, MEDIUM, RECTANGULAR, RECTANGULAR_TEST_IMAGE_NAME));
        assertThat(responseImages.get(LARGE)).isEqualTo(
            String.format("%s/%s-%s-%s", testBasePath, LARGE, RECTANGULAR, RECTANGULAR_TEST_IMAGE_NAME));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin", "Everyone"})
    public void When_GetImage_With_ProcessingFailed_Expect_BadGateway() {
        // arrange
        var testId = ObjectId.get();
        var testStatus = ImageProcessingStatus.of(PROCESSING_FAILED);
        when(imageMetaDataService.getImageMetaData(testId))
            .thenReturn(Optional.of(ImageMetadata.builder()
                .id(testId)
                .status(testStatus)
                .build()));

        // act
        var errorResponse = given()
            .get("/" + testId)
            .then()
            .statusCode(502)
            .extract()
            .response()
            .as(ErrorResponse.class);

        // assert
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getErrors()).hasSize(1);
        assertThat(errorResponse.getErrors().getFirst().getMessage()).isEqualTo(
            String.format("The image could not be processed successfully. id: %s status: %s", testId, testStatus));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin", "Everyone"})
    public void When_GetImage_With_ProcessingTimeout_Expect_GatewayTimeout() {
        // arrange
        var testId = ObjectId.get();
        var testStatus = ImageProcessingStatus.of(PROCESSING_TIMEOUT);
        when(imageMetaDataService.getImageMetaData(testId))
            .thenReturn(Optional.of(ImageMetadata.builder()
                .id(testId)
                .status(testStatus)
                .build()));

        // act
        var errorResponse = given()
            .get("/" + testId)
            .then()
            .statusCode(504)
            .extract()
            .response()
            .as(ErrorResponse.class);

        // assert
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getErrors()).hasSize(1);
        assertThat(errorResponse.getErrors().getFirst().getMessage()).isEqualTo(
            String.format("The image processing timed out. id: %s status: %s", testId, testStatus));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin", "Everyone"})
    public void When_GetImage_With_OriginalUploaded_Expect_MetaDataWithoutImageUrls() {
        checkForNoImagesResponse(ORIGINAL_UPLOADED);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin", "Everyone"})
    public void When_GetImage_With_Processing_Expect_MetaDataWithoutImageUrls() {
        checkForNoImagesResponse(PROCESSING);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"blog-admin", "Everyone"})
    public void When_GetUploadUrl_With_ValidRequest_Expect_MetadataAndCorrectResponse() {
        var stubMetadata = ImageMetadata.builder()
            .id(ObjectId.get())
            .parentKey(UUID.randomUUID().toString())
            .filename(RECTANGULAR_TEST_IMAGE_NAME)
            .status(ImageProcessingStatus.of(INITIALISED))
            .build();

        when(imageMetaDataService.newImageMetaData(any(), any()))
            .thenReturn(stubMetadata);
        when(imageMetaDataService.getImageMetaData(any()))
            .thenCallRealMethod();
        when(objectStorageService.getPreSignedUrl(any(), any()))
            .thenReturn(PreSignedUploadDto.builder()
                .url("http://test-url")
                .headers(Map.of("x-amz-tagging", "test-tag"))
                .build());

        var response = given()
            .multiPart("filename", RECTANGULAR_TEST_IMAGE_NAME)
            .multiPart("mime-type", JPEG_MIME_TYPE)
            .multiPart("tag", TEST_TAGS.getFirst())
            .multiPart("tag", TEST_TAGS.getLast())
            .post("/pre-signed")
            .then()
            .statusCode(200)
            .extract()
            .as(UploadUrlResponseDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getUploadUrl())
            .isNotBlank()
            .isEqualTo("http://test-url");
        assertThat(response.getHeaders())
            .isNotEmpty()
            .hasSize(1);
        assertThat(response.getHeaders().get("x-amz-tagging")).isEqualTo("test-tag");
        assertThat(response.getImageId())
            .isNotBlank()
            .isEqualTo(stubMetadata.getId().toString());

        verify(imageMetaDataService, times(1))
            .newImageMetaData(eq(RECTANGULAR_TEST_IMAGE_NAME), eq(TEST_TAGS));
    }

    @Test
    @TestSecurity(user = "unauthorisedUser", roles = {"Everyone"})
    public void When_GetUploadUrl_With_UnauthorisedUser_Expect_Forbidden() {
        given()
            .multiPart("filename", RECTANGULAR_TEST_IMAGE_NAME)
            .multiPart("mime-type", JPEG_MIME_TYPE)
            .post("/pre-signed")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin"})
    public void When_GetUploadUrl_With_InvalidMimeType_Expect_BadRequestException() {
        // arrange
        String invalidMimeType = "application/json";
        String testErrorMessage = "test error message";
        Mockito.doThrow(new BadRequestException(List.of(ErrorMessage.builder()
                .message(testErrorMessage)
                .build())))
            .when(imageValidationService).validateMimeType(eq(invalidMimeType), any());

        //act
        ErrorResponse errorResponse = given()
            .multiPart("filename", RECTANGULAR_TEST_IMAGE_NAME)
            .multiPart("mime-type", invalidMimeType)
            .when()
            .post("/pre-signed")
            .then()
            .statusCode(400)
            .extract()
            .body()
            .as(ErrorResponse.class);

        assertThat(errorResponse.getErrors().size()).isEqualTo(1);
        assertThat(errorResponse.getErrors().getFirst().getMessage()).isEqualTo(testErrorMessage);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"blog-admin", "Everyone"})
    public void When_GetUploadUrl_With_MissingFilename_Expect_BadRequest() {
        given()
            .multiPart("mime-type", JPEG_MIME_TYPE)
            .post("/pre-signed")
            .then()
            .statusCode(400);
    }

    private void checkForNoImagesResponse(ImageProcessingStage stage) {
        // arrange
        var testId = ObjectId.get();
        var testParentKey = String.format("%s-%s", RECTANGULAR_TEST_IMAGE_NAME, UUID.randomUUID());
        var testStatus = ImageProcessingStatus.of(stage);
        when(imageMetaDataService.getImageMetaData(testId))
            .thenReturn(Optional.of(ImageMetadata.builder()
                .id(testId)
                .type(RECTANGULAR)
                .filename(RECTANGULAR_TEST_IMAGE_NAME)
                .parentKey(testParentKey)
                .status(testStatus)
                .build()));

        // act
        var response = given()
            .get("/" + testId)
            .then()
            .statusCode(200)
            .extract()
            .as(ImageResponseDto.class);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testId.toString());
        assertThat(response.getType()).isEqualTo(RECTANGULAR);
        assertThat(response.getFilename()).isEqualTo(RECTANGULAR_TEST_IMAGE_NAME);
        assertThat(response.getStatus()).isEqualTo(testStatus);
        assertThat(response.getImages()).isEmpty();
    }

    private Optional<String> getHeaderWithRel(String rel, List<Header> headers) {
        return headers.stream()
            .map(Header::getValue)
            .filter(value -> value.contains(String.format("rel=\"%s\"", rel)))
            .findFirst();
    }
}
