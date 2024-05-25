package com.benhession.imagepicker.controller;

import com.benhession.imagepicker.exception.ErrorResponse;
import com.benhession.imagepicker.service.ImageCreationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@QuarkusTest
@TestHTTPEndpoint(ImageController.class)
public class ImageControllerTest {

    private static final String SYSTEM_ERROR_MESSAGE = "An unexpected error has occurred.";

    @InjectMock
    ImageCreationService creationService;

    @Test
    public void When_AddImage_With_CreationServiceThrows_Expect_500AndError() {

        doThrow(new RuntimeException("test exception")).when(creationService).createNewImages(any());

        File file = loadTestFile("test-image.jpg");

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
    public void When_AddImage_With_InvalidMimeType_Expect_BadRequestAndErrorMessage() {

        File file = loadTestFile("test-text-file.txt");
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
    public void When_AddImage_With_MissingFilename_ExpectBadRequest() {
        File file = loadTestFile("test-image.jpg");

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
    public void When_AddImage_With_BlankFilename_ExpectBadRequest() {
        File file = loadTestFile("test-image.jpg");

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
    public void When_AddImage_With_MissingData_ExpectBadRequest() {

        given()
                .multiPart("mime-type", "image/jpeg")
                .multiPart("image-type", "SQUARE")
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void When_AddImage_With_InvalidImageType_ExpectBadRequest() {
        File file = loadTestFile("test-image.jpg");

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

    private File loadTestFile(String filename) {
        URL url = getClass().getClassLoader().getResource(filename);
        assert url != null;
        return new File(url.getFile());
    }
}
