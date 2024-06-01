package com.benhession.imagepicker.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.benhession.imagepicker.exception.ErrorResponse;
import com.benhession.imagepicker.service.ImageCreationService;
import com.benhession.imagepicker.testutil.TestFileLoader;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.io.File;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(ImageController.class)
public class ImageControllerTest {

    private static final String SYSTEM_ERROR_MESSAGE = "An unexpected error has occurred.";

    @Inject
    TestFileLoader testFileLoader;
    @InjectMock
    ImageCreationService creationService;

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

        var error = errorResponse.getErrors().get(0);
        assertThat(error.getMessage()).isEqualTo(SYSTEM_ERROR_MESSAGE);
        assertThat(error.getPath()).isNull();
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"Everyone", "blog-admin"})
    public void When_AddImage_With_InvalidMimeType_Expect_BadRequestAndErrorMessage() {

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

        var error = errorResponse.getErrors().get(0);
        assertThat(error.getMessage()).contains("Mime type must be one of the following");
        assertThat(error.getPath()).isEqualTo("/image");
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
    public void When_AddImage_With_unauthorisedUser_Expect_Forbidden() {
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
}
