package com.benhession.imagepicker.api.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static com.benhession.imagepicker.api.model.ImageSize.MEDIUM;
import static com.benhession.imagepicker.api.model.ImageSize.THUMBNAIL;
import static com.benhession.imagepicker.api.model.ImageType.PANORAMIC;
import static com.benhession.imagepicker.api.model.ImageType.SQUARE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ImageSizeServiceTest {

    @Inject
    ImageSizeService imageSizeService;

    @Test
    public void When_FindImageHeightWidth_With_SquareThumbnail_Expect_CorrectValues() {
        var result = imageSizeService.findImageHeightWidth(SQUARE, THUMBNAIL);

        assertEquals(40, result.getHeight());
        assertEquals(40, result.getWidth());
    }

    @Test
    public void When_FindImageHeightWidth_With_MediumPanoramic_Expect_CorrectValues() {
        var result = imageSizeService.findImageHeightWidth(PANORAMIC, MEDIUM);

        assertEquals(140, result.getHeight());
        assertEquals(280, result.getWidth());
    }
}
