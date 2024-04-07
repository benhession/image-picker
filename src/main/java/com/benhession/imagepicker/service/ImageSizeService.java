package com.benhession.imagepicker.service;

import com.benhession.imagepicker.config.ImageConfigProperties;
import com.benhession.imagepicker.model.ImageHeightWidth;
import com.benhession.imagepicker.model.ImageSize;
import com.benhession.imagepicker.model.ImageType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ImageSizeService {

    @Inject
    ImageConfigProperties imageConfigProperties;

    public ImageHeightWidth findImageHeightWidth(ImageType imageType, ImageSize imageSize) {
        return switch (imageType) {
            case SQUARE -> switch (imageSize) {
                case THUMBNAIL -> buildImageHeightWidth(
                        imageConfigProperties.size().square().thumbnail().height(),
                        imageConfigProperties.size().square().thumbnail().width()
                );
                case SMALL -> buildImageHeightWidth(
                        imageConfigProperties.size().square().small().height(),
                        imageConfigProperties.size().square().small().width()
                );
                case MEDIUM -> buildImageHeightWidth(
                        imageConfigProperties.size().square().medium().height(),
                        imageConfigProperties.size().square().medium().width()
                );
                case LARGE -> buildImageHeightWidth(
                        imageConfigProperties.size().square().large().height(),
                        imageConfigProperties.size().square().large().width()
                );
            };

            case PANORAMIC -> switch (imageSize) {
                case THUMBNAIL -> buildImageHeightWidth(
                        imageConfigProperties.size().panoramic().thumbnail().height(),
                        imageConfigProperties.size().panoramic().thumbnail().width()
                );
                case SMALL -> buildImageHeightWidth(
                        imageConfigProperties.size().panoramic().small().height(),
                        imageConfigProperties.size().panoramic().small().width()
                );
                case MEDIUM -> buildImageHeightWidth(
                        imageConfigProperties.size().panoramic().medium().height(),
                        imageConfigProperties.size().panoramic().medium().width()
                );
                case LARGE -> buildImageHeightWidth(
                        imageConfigProperties.size().panoramic().large().height(),
                        imageConfigProperties.size().panoramic().large().width()
                );
            };

            case RECTANGULAR -> switch (imageSize) {
                case THUMBNAIL -> buildImageHeightWidth(
                        imageConfigProperties.size().rectangular().thumbnail().height(),
                        imageConfigProperties.size().rectangular().thumbnail().width()
                );
                case SMALL -> buildImageHeightWidth(
                        imageConfigProperties.size().rectangular().small().height(),
                        imageConfigProperties.size().rectangular().small().width()
                );
                case MEDIUM -> buildImageHeightWidth(
                        imageConfigProperties.size().rectangular().medium().height(),
                        imageConfigProperties.size().rectangular().medium().width()
                );
                case LARGE -> buildImageHeightWidth(
                        imageConfigProperties.size().rectangular().large().height(),
                        imageConfigProperties.size().rectangular().large().width()
                );
            };

            case LANDSCAPE -> switch (imageSize) {
                case THUMBNAIL -> buildImageHeightWidth(
                        imageConfigProperties.size().landscape().thumbnail().height(),
                        imageConfigProperties.size().landscape().thumbnail().width()
                );
                case SMALL -> buildImageHeightWidth(
                        imageConfigProperties.size().landscape().small().height(),
                        imageConfigProperties.size().landscape().small().width()
                );
                case MEDIUM -> buildImageHeightWidth(
                        imageConfigProperties.size().landscape().medium().height(),
                        imageConfigProperties.size().landscape().medium().width()
                );
                case LARGE -> buildImageHeightWidth(
                        imageConfigProperties.size().landscape().large().height(),
                        imageConfigProperties.size().landscape().large().width()
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
