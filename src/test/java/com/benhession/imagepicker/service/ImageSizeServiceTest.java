package com.benhession.imagepicker.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static com.benhession.imagepicker.model.ImageSize.MEDIUM;
import static com.benhession.imagepicker.model.ImageSize.THUMBNAIL;
import static com.benhession.imagepicker.model.ImageType.PANORAMIC;
import static com.benhession.imagepicker.model.ImageType.SQUARE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ImageSizeServiceTest {

    @Inject
    ImageSizeService imageSizeService;

    @Test
    public void When_FindImageHeightWidth_With_SquareThumbnail_Expect_CorrectValues() {
        var result = imageSizeService.findImageHeightWidth(SQUARE, THUMBNAIL);

        assertEquals(100, result.getHeight());
        assertEquals(100, result.getWidth());
    }

    @Test
    public void When_FindImageHeightWidth_With_MediumPanoramic_Expect_CorrectValues() {
        var result = imageSizeService.findImageHeightWidth(PANORAMIC, MEDIUM);

        assertEquals(100, result.getHeight());
        assertEquals(500, result.getWidth());
    }
}
