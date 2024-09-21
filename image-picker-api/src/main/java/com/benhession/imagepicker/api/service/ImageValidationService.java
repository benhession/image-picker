package com.benhession.imagepicker.api.service;

import com.benhession.imagepicker.api.dto.ObjectUploadForm;
import com.benhession.imagepicker.common.exception.BadRequestException;
import com.benhession.imagepicker.data.model.ImageType;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageValidationService {

    private final ImageSizeService imageSizeService;

    public void validateInputImage(ObjectUploadForm objectUploadForm) {
        final File file = objectUploadForm.getData();
        final ImageType imageType = ImageType.valueOf(objectUploadForm.getImageType());

        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            List<BadRequestException.ErrorMessage> errorMessages = new ArrayList<>();
            BigDecimal expectedAspectRatio = imageSizeService.findAspectRatio(imageType);
            BigDecimal actualAspectRatio = calculateAspectRatio(bufferedImage.getWidth(),
              bufferedImage.getHeight());
            int minWidth = imageSizeService.findMinWidth(imageType);

            if (!actualAspectRatio.equals(expectedAspectRatio)) {
                errorMessages.add(BadRequestException.ErrorMessage.builder()
                  .message(String.format("Expected aspect ratio for image type: %s to be %s, but was %s",
                    imageType, expectedAspectRatio.floatValue(), actualAspectRatio.floatValue()))
                  .build());
            }

            if (bufferedImage.getWidth() < minWidth) {
                errorMessages.add(BadRequestException.ErrorMessage.builder()
                  .message(String.format("Expected width of %s image to be more that %s, but was %s",
                    imageType, minWidth, bufferedImage.getWidth()))
                  .build());
            }

            if (!errorMessages.isEmpty()) {
                throw new BadRequestException(errorMessages);
            }

        } catch (IOException e) {
            throw new BadRequestException(List.of(BadRequestException.ErrorMessage.builder()
              .message("Unable to read file data")
              .build()));
        }
    }

    private BigDecimal calculateAspectRatio(int width, int height) {
        var widthBd = new BigDecimal(Integer.toString(width));
        var heightBd = new BigDecimal(Integer.toString(height));

        return widthBd.divide(heightBd, 2, RoundingMode.HALF_UP);
    }
}
