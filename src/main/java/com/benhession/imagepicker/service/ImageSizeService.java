package com.benhession.imagepicker.service;

import com.benhession.imagepicker.config.ImageConfigProperties;
import com.benhession.imagepicker.model.ImageHeightWidth;
import com.benhession.imagepicker.model.ImageSize;
import com.benhession.imagepicker.model.ImageType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImageSizeService {

    private final ImageConfigProperties imageConfigProperties;

    public float findAspectRatio(ImageType imageType) {
        return switch (imageType) {
            case SQUARE -> imageConfigProperties.size().square().aspectRatio();
            case PANORAMIC -> imageConfigProperties.size().panoramic().aspectRatio();
            case RECTANGULAR -> imageConfigProperties.size().rectangular().aspectRatio();
            case LANDSCAPE -> imageConfigProperties.size().landscape().aspectRatio();
        };
    }

    public ImageHeightWidth findImageHeightWidth(ImageType imageType, ImageSize imageSize) {
        var squareConfig = imageConfigProperties.size().square();
        var panoConfig = imageConfigProperties.size().panoramic();
        var rectangularConfig = imageConfigProperties.size().rectangular();
        var landscapeConfig = imageConfigProperties.size().landscape();

        return switch (imageType) {
            case SQUARE -> heightWidthFromImageSize(imageSize, squareConfig);
            case PANORAMIC -> heightWidthFromImageSize(imageSize, panoConfig);
            case RECTANGULAR -> heightWidthFromImageSize(imageSize, rectangularConfig);
            case LANDSCAPE -> heightWidthFromImageSize(imageSize,landscapeConfig);
        };
    }

    private ImageHeightWidth heightWidthFromImageSize(ImageSize imageSize,
                                                      ImageConfigProperties.ImageType.ImageSize imageSizeConfig) {
        return switch (imageSize) {
            case THUMBNAIL -> calculateHeightWidth(
                    imageSizeConfig.minWidth(),
                    imageSizeConfig.thumbnail().scalingFactor(),
                    imageSizeConfig.aspectRatio()
            );
            case SMALL -> calculateHeightWidth(
                    imageSizeConfig.minWidth(),
                    imageSizeConfig.small().scalingFactor(),
                    imageSizeConfig.aspectRatio()
            );
            case MEDIUM -> calculateHeightWidth(
                    imageSizeConfig.minWidth(),
                    imageSizeConfig.medium().scalingFactor(),
                    imageSizeConfig.aspectRatio()
            );
            case LARGE -> calculateHeightWidth(
                    imageSizeConfig.minWidth(),
                    imageSizeConfig.large().scalingFactor(),
                    imageSizeConfig.aspectRatio()
            );
        };
    }

    private int calculateWidth(int minWidth, float scaleFactor) {
        return Math.round(minWidth * scaleFactor);
    }

    private int calculateHeight(int minWidth, float scaleFactor, float aspectRatio) {
        return Math.round(calculateWidth(minWidth, scaleFactor) / aspectRatio);
    }


    private ImageHeightWidth calculateHeightWidth(int minWidth, float scaleFactor, float aspectRatio) {
        return ImageHeightWidth.builder()
                .height(calculateHeight(minWidth, scaleFactor, aspectRatio))
                .width(calculateWidth(minWidth, scaleFactor))
                .build();
    }
}
