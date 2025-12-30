package com.benhession.imagepicker.common.service;

import static com.benhession.imagepicker.common.model.ImageOrientation.LANDSCAPE;
import static com.benhession.imagepicker.common.model.ImageOrientation.PORTRAIT;
import static com.benhession.imagepicker.common.model.ImageSize.MEDIUM;
import static com.benhession.imagepicker.common.model.ImageSize.THUMBNAIL;
import static com.benhession.imagepicker.common.model.ImageType.PANORAMIC;
import static com.benhession.imagepicker.common.model.ImageType.SQUARE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ImageSizeServiceTest {

    @Inject
    ImageSizeService imageSizeService;

    @Test
    public void When_FindImageHeightWidth_With_SquareThumbnail_Expect_CorrectValues() {
        var result = imageSizeService.findImageHeightWidth(SQUARE, THUMBNAIL, LANDSCAPE);

        assertEquals(120, result.getHeight());
        assertEquals(120, result.getWidth());
    }

    @Test
    public void When_FindImageHeightWidth_With_MediumPanoramic_Expect_CorrectValues() {
        var result = imageSizeService.findImageHeightWidth(PANORAMIC, MEDIUM, LANDSCAPE);

        assertEquals(700, result.getHeight());
        assertEquals(1400, result.getWidth());
    }

    @Test
    public void When_FindImageHeightWidth_With_Portrait_Expect_CorrectValues() {
        var result = imageSizeService.findImageHeightWidth(PANORAMIC, MEDIUM, PORTRAIT);
        assertEquals(1400, result.getHeight());
        assertEquals(700, result.getWidth());
    }

    @Test
    public void When_CalculateImageHeight_With_Landscape_Expect_CorrectValue() {
        var result = imageSizeService.calculateImageHeight(1400, PANORAMIC, LANDSCAPE);
        assertEquals(700, result);
    }

    @Test
    public void When_CalculateImageHeight_With_Portrait_Expect_CorrectValue() {
        var result = imageSizeService.calculateImageHeight(700, PANORAMIC, PORTRAIT);
        assertEquals(1400, result);
    }

    @Test
    public void When_FindMinWidth_With_Landscape_Expect_CorrectValue() {
        var result = imageSizeService.findMinWidth(PANORAMIC, LANDSCAPE);
        assertEquals(2000, result);
    }

    @Test
    public void When_FindMinWidth_With_Portrait_Expect_CorrectValue() {
        var result = imageSizeService.findMinWidth(PANORAMIC, PORTRAIT);
        assertEquals(1000, result);
    }

    @Test
    public void When_FindAspectRatio_With_Landscape_Expect_CorrectValue() {
        var result = imageSizeService.findAspectRatio(PANORAMIC, LANDSCAPE);
        assertEquals(new BigDecimal("2.00"), result);
    }

    @Test
    public void When_FindAspectRatio_With_Portrait_Expect_CorrectValue() {
        var result = imageSizeService.findAspectRatio(PANORAMIC, PORTRAIT);
        assertEquals(new BigDecimal("0.50"), result);
    }
}
