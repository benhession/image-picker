package com.benhession.imagepicker.api.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.benhession.imagepicker.api.dto.ImageResponseDto;
import com.benhession.imagepicker.api.exception.ErrorResponse;
import com.benhession.imagepicker.api.service.ImageCreationService;
import com.benhession.imagepicker.api.service.ImageValidationService;
import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException;
import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException.ErrorMessage;
import com.benhession.imagepicker.common.exception.BadRequestException;
import com.benhession.imagepicker.common.model.PageInfo;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import com.benhession.imagepicker.testutil.TestFileLoader;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.Header;
import jakarta.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(ImageController.class)
public class ImageControllerTest {

    private static final String SYSTEM_ERROR_MESSAGE = "An unexpected error has occurred.";

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
    ImageCreationService creationService;
    @InjectMock
    ImageMetaDataService imageMetaDataService;
    @InjectMock
    ObjectStorageService objectStorageService;
    @InjectMock
    ImageValidationService imageValidationService;

    @Test
    @TestSecurity(user = "testUser", roles = {"Everyone", "blog-admin"})
    public void When_AddImage_With_CreationServiceThrows_Expect_500AndError() {

        doThrow(new RuntimeException("test exception")).when(creationService).createNewImages(any());

        File file = testFileLoader.loadTestFile("test-image.jpg");

        var errorResponse = given()
          .multiPart("data", file)
          .multiPart("filename", "test-image.jpg")
          .multiPart("mime-type", "image/jpeg")
          .multiPart("image-type", "SQUARE")
          .when()
          .post()
          .then()
          .statusCode(500)
          .extract()
          .as(ErrorResponse.class);

        assertThat(errorResponse.getErrors().size()).isEqualTo(1);

        var error = errorResponse.getErrors().getFirst();
        assertThat(error.getMessage()).isEqualTo(SYSTEM_ERROR_MESSAGE);
        assertThat(error.getPath()).isNull();
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"Everyone", "blog-admin"})
    public void When_AddImage_with_ImageValidationServiceThrowsBadRequest_Expect_400AndError() {
        var errorMessage = AbstractMultipleErrorApplicationException
          .ErrorMessage.builder().message("test exception").build();

        doThrow(new BadRequestException(List.of(errorMessage)))
          .when(imageValidationService).validateInputImage(any());

        File file = testFileLoader.loadTestFile("test-image.jpg");

        given()
          .multiPart("data", file)
          .multiPart("filename", "test-image.jpg")
          .multiPart("mime-type", "image/jpeg")
          .multiPart("image-type", "SQUARE")
          .when()
          .post()
          .then()
          .statusCode(400)
          .extract()
          .as(ErrorResponse.class);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"Everyone", "blog-admin"})
    public void When_AddImage_With_InvalidMimeType_Expect_BadRequestAndErrorMessage() {

        ErrorMessage thrownErrorMessage = ErrorMessage.builder()
          .message("test message")
          .path("test path")
          .build();

        doThrow(new BadRequestException(List.of(thrownErrorMessage)))
          .when(imageValidationService).validateInputImage(any());

        File file = testFileLoader.loadTestFile("test-text-file.txt");
        var errorResponse = given()
          .multiPart("data", file)
          .multiPart("filename", "test-text-file.txt")
          .multiPart("mime-type", "text.plain")
          .multiPart("image-type", "SQUARE")
          .when()
          .post()
          .then()
          .statusCode(400)
          .extract()
          .as(ErrorResponse.class);

        assertThat(errorResponse.getErrors().size()).isEqualTo(1);

        var error = errorResponse.getErrors().getFirst();
        assertThat(error.getMessage()).isEqualTo("test message");
        assertThat(error.getPath()).isEqualTo("test path");
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"Everyone", "blog-admin"})
    public void When_AddImage_With_MissingFilename_Expect_BadRequest() {
        File file = testFileLoader.loadTestFile("test-image.jpg");

        given()
          .multiPart("data", file)
          .multiPart("mime-type", "image/jpeg")
          .multiPart("image-type", "SQUARE")
          .when()
          .post()
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"Everyone", "blog-admin"})
    public void When_AddImage_With_BlankFilename_Expect_BadRequest() {
        File file = testFileLoader.loadTestFile("test-image.jpg");

        given()
          .multiPart("filename", " ")
          .multiPart("data", file)
          .multiPart("mime-type", "image/jpeg")
          .multiPart("image-type", "SQUARE")
          .when()
          .post()
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"Everyone", "blog-admin"})
    public void When_AddImage_With_MissingData_Expect_BadRequest() {

        given()
          .multiPart("mime-type", "image/jpeg")
          .multiPart("image-type", "SQUARE")
          .when()
          .post()
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"Everyone", "blog-admin"})
    public void When_AddImage_With_InvalidImageType_Expect_BadRequest() {
        File file = testFileLoader.loadTestFile("test-image.jpg");

        given()
          .multiPart("data", file)
          .multiPart("filename", "test-image.jpg")
          .multiPart("mime-type", "image/jpeg")
          .multiPart("image-type", "CIRCLE")
          .when()
          .post()
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "unauthorisedTestUser", roles = {"Everyone"})
    public void When_AddImage_With_UnauthorisedUser_Expect_Forbidden() {
        File file = testFileLoader.loadTestFile("test-image.jpg");

        given()
          .multiPart("data", file)
          .multiPart("filename", "test-image.jpg")
          .multiPart("mime-type", "image/jpeg")
          .multiPart("image-type", "RECTANGULAR")
          .when()
          .post()
          .then()
          .statusCode(403);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"blog-admin"})
    public void When_GetImages_With_ResultsOnOnePage_Expect_ResultsAndCorrectHeaders() {
        when(imageMetaDataService.getPageInfo(eq(0), eq(5)))
          .thenReturn(PageInfo.builder()
            .page(0)
            .size(5)
            .numberItems(5)
            .lastPage(0)
            .build());
        when(imageMetaDataService.getImageMetaDataList(0, 5))
          .thenReturn(FIVE_METADATA_RESULTS);
        when(imageMetaDataService.getPageInfo(eq(0), eq(5)))
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
        when(imageMetaDataService.getPageInfo(eq(0), eq(3)))
          .thenReturn(PageInfo.builder()
            .page(1)
            .size(3)
            .numberItems(5)
            .lastPage(1)
            .build());
        when(imageMetaDataService.getImageMetaDataList(0, 3))
          .thenReturn(FIVE_METADATA_RESULTS.subList(0, 3));
        when(imageMetaDataService.getPageInfo(eq(0), eq(3)))
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
        when(imageMetaDataService.getPageInfo(eq(1), eq(3)))
          .thenReturn(PageInfo.builder()
            .page(1)
            .size(3)
            .numberItems(5)
            .lastPage(1)
            .build());
        when(imageMetaDataService.getImageMetaDataList(1, 3))
          .thenReturn(FIVE_METADATA_RESULTS.subList(3, 5));
        when(imageMetaDataService.getPageInfo(eq(1), eq(3)))
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

    private Optional<String> getHeaderWithRel(String rel, List<Header> headers) {
        return headers.stream()
          .map(Header::getValue)
          .filter(value -> value.contains(String.format("rel=\"%s\"", rel)))
          .findFirst();
    }
}
