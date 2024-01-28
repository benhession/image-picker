package com.benhession.imagepicker.service;

import com.benhession.imagepicker.config.ImageConfigProperties;
import com.benhession.imagepicker.model.ImageHeightWidth;
import com.benhession.imagepicker.model.ImageSize;
import com.benhession.imagepicker.model.ImageType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperties;

@ApplicationScoped
public class ImageSizeService {

    @Inject
    @ConfigProperties
    ImageConfigProperties imageConfigProperties;

    public ImageHeightWidth findImageHeightWidth(ImageType imageType, ImageSize imageSize) {
        return switch (imageType) {
            case SQUARE -> switch (imageSize) {
                case THUMBNAIL -> buildImageHeightWidth(
                        imageConfigProperties.getSquareThumbnailHeight(),
                        imageConfigProperties.getSquareThumbnailWidth()
                );
                case SMALL -> buildImageHeightWidth(
                        imageConfigProperties.getSquareSmallHeight(),
                        imageConfigProperties.getSquareSmallWidth()
                );
                case MEDIUM -> buildImageHeightWidth(
                        imageConfigProperties.getSquareMediumHeight(),
                        imageConfigProperties.getSquareMediumWidth()
                );
                case LARGE -> buildImageHeightWidth(
                        imageConfigProperties.getSquareLargeHeight(),
                        imageConfigProperties.getSquareLargeWidth()
                );
            };

            case PANORAMIC -> switch (imageSize) {
                case THUMBNAIL -> buildImageHeightWidth(
                        imageConfigProperties.getPanoramicThumbnailHeight(),
                        imageConfigProperties.getPanoramicThumbnailWidth()
                );
                case SMALL -> buildImageHeightWidth(
                        imageConfigProperties.getPanoramicSmallHeight(),
                        imageConfigProperties.getPanoramicSmallWidth()
                );
                case MEDIUM -> buildImageHeightWidth(
                        imageConfigProperties.getPanoramicMediumHeight(),
                        imageConfigProperties.getPanoramicMediumWidth()
                );
                case LARGE -> buildImageHeightWidth(
                        imageConfigProperties.getPanoramicLargeHeight(),
                        imageConfigProperties.getPanoramicLargeWidth()
                );
            };

            case RECTANGULAR -> switch (imageSize) {
                case THUMBNAIL -> buildImageHeightWidth(
                        imageConfigProperties.getRectangularThumbnailHeight(),
                        imageConfigProperties.getRectangularThumbnailWidth()
                );
                case SMALL -> buildImageHeightWidth(
                        imageConfigProperties.getRectangularSmallHeight(),
                        imageConfigProperties.getRectangularSmallWidth()
                );
                case MEDIUM -> buildImageHeightWidth(
                        imageConfigProperties.getRectangularMediumHeight(),
                        imageConfigProperties.getRectangularMediumWidth()
                );
                case LARGE -> buildImageHeightWidth(
                        imageConfigProperties.getRectangularLargeHeight(),
                        imageConfigProperties.getRectangularLargeWidth()
                );
            };

            case LANDSCAPE -> switch (imageSize) {
                case THUMBNAIL -> buildImageHeightWidth(
                        imageConfigProperties.getLandscapeThumbnailHeight(),
                        imageConfigProperties.getLandscapeThumbnailWidth()
                );
                case SMALL -> buildImageHeightWidth(
                        imageConfigProperties.getLandscapeSmallHeight(),
                        imageConfigProperties.getLandscapeSmallWidth()
                );
                case MEDIUM -> buildImageHeightWidth(
                        imageConfigProperties.getLandscapeMediumHeight(),
                        imageConfigProperties.getLandscapeMediumWidth()
                );
                case LARGE -> buildImageHeightWidth(
                        imageConfigProperties.getLandscapeLargeHeight(),
                        imageConfigProperties.getLandscapeLargeWidth()
                );
            };
        };
    }

    private ImageHeightWidth buildImageHeightWidth(int height, int width) {
        return ImageHeightWidth.builder()
                .height(height)
                .width(width)
                .build();
    }
}
