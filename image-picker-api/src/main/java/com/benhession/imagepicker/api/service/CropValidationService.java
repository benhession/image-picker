package com.benhession.imagepicker.api.service;

import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException.ErrorMessage;
import com.benhession.imagepicker.common.exception.BadRequestException;
import com.benhession.imagepicker.common.model.ImageCropProperties;
import com.benhession.imagepicker.common.service.ImageSizeService;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class CropValidationService {

    private final ImageSizeService imageSizeService;

    public void validateCrop(ImageCropProperties imageCropProperties, BufferedImage originalImage) {
        List<ErrorMessage> errorMessages = new ArrayList<>();

        // check the cropped image meets the minimum size for the image type
        var minWidth =
            imageSizeService.findMinWidth(imageCropProperties.imageType(), imageCropProperties.orientation());
        if (imageCropProperties.width() < minWidth) {
            errorMessages.add(ErrorMessage.builder()
                .message(String.format(
                    "The cropped image does not meet the minimum size requirement. Minimum width %s but was %s",
                    minWidth, imageCropProperties.width()))
                .build());
        }

        int originalWidth = originalImage.getWidth();
        int rightmostExtremity = imageCropProperties.width() + imageCropProperties.baseCoordinate().x();

        if (rightmostExtremity > originalWidth) {
            errorMessages.add(ErrorMessage.builder()
                .message(String.format("The cropped image is not within the original's width dimension."
                    + " Expected rightmost extremity to be < %s, but was %s", originalWidth, rightmostExtremity))
                .build());
        }

        // check the crop dimensions are within the original image dimensions
        int originalHeight = originalImage.getHeight();
        int croppedHeight = imageSizeService
            .calculateImageHeight(imageCropProperties.width(), imageCropProperties.imageType(),
                imageCropProperties.orientation());
        int bottomExtremity = imageCropProperties.baseCoordinate().y() + croppedHeight;
        if (bottomExtremity > originalHeight) {
            errorMessages.add(ErrorMessage.builder()
                .message(String.format("The cropped image is not within the original's height dimension. "
                    + "Expected bottommost extremity to be < %s, but was %s", originalHeight, bottomExtremity))
                .build());
        }

        if (!errorMessages.isEmpty()) {
            throw new BadRequestException(errorMessages);
        }
    }
}
