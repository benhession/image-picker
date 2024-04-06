package com.benhession.imagepicker.controller;

import com.benhession.imagepicker.exception.ErrorResponse;
import com.benhession.imagepicker.service.ImageCreationService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
@TestHTTPEndpoint(ImageController.class)
public class ImageControllerTest {

    private static final String SYSTEM_ERROR_MESSAGE = "An unexpected error has occurred.";

    @Inject
    ImageCreationService imageCreationService;

    @Test
    public void When_AddImage_With_CreationServiceThrows_Expect_500AndError() {

        ImageCreationService mockCreationService = Mockito.mock(ImageCreationService.class);
        QuarkusMock.installMockForInstance(mockCreationService, imageCreationService);
        Mockito.doThrow(new RuntimeException("test exception"))
                .when(mockCreationService).createNewImagesFrom(any());

        URL url = getClass().getClassLoader().getResource("test-image.jpg");
        assert url != null;
        File file = Paths.get(url.getPath()).toFile();

        var errorResponse = given()
            .multiPart("data", file)
            .multiPart("filename", "test-image.jpg")
            .multiPart("mimetype", "image/jpeg")
        .when().post()
        .then()
            .statusCode(500)
            .extract()
            .as(ErrorResponse.class);

        assertThat(errorResponse.getErrors().size()).isEqualTo(1);

        var error = errorResponse.getErrors().get(0);
        assertThat(error.getMessage()).isEqualTo(SYSTEM_ERROR_MESSAGE);
        assertThat(error.getPath()).isNull();
    }
}
