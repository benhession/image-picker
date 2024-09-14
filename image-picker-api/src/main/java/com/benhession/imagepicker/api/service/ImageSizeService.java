package com.benhession.imagepicker.api.service;

import com.benhession.imagepicker.api.config.ImageConfigProperties;
import com.benhession.imagepicker.api.exception.InvalidConfigurationException;
import com.benhession.imagepicker.api.model.ImageHeightWidth;
import com.benhession.imagepicker.api.model.ImageSize;
import com.benhession.imagepicker.api.model.ImageType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImageSizeService {

    private final ImageConfigProperties imageConfigProperties;

    public BigDecimal findAspectRatio(ImageType imageType) {
        return switch (imageType) {
            case SQUARE -> aspectRatioFromString(imageConfigProperties.size().square().aspectRatio());
            case PANORAMIC -> aspectRatioFromString(imageConfigProperties.size().panoramic().aspectRatio());
            case RECTANGULAR -> aspectRatioFromString(imageConfigProperties.size().rectangular().aspectRatio());
            case LANDSCAPE -> aspectRatioFromString(imageConfigProperties.size().landscape().aspectRatio());
        };
    }

    public int findMinWidth(ImageType imageType) {
        return switch (imageType) {
            case SQUARE -> Integer.parseInt(imageConfigProperties.size().square().minWidth());
            case PANORAMIC -> Integer.parseInt(imageConfigProperties.size().panoramic().minWidth());
            case RECTANGULAR -> Integer.parseInt(imageConfigProperties.size().rectangular().minWidth());
            case LANDSCAPE -> Integer.parseInt(imageConfigProperties.size().landscape().minWidth());
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
            case LANDSCAPE -> heightWidthFromImageSize(imageSize, landscapeConfig);
        };
    }

    private ImageHeightWidth heightWidthFromImageSize(ImageSize imageSize,
                                                      ImageConfigProperties.ImageType.ImageSize imageSizeConfig) {
        return switch (imageSize) {
            case THUMBNAIL -> calculateHeightWidth(
              new BigDecimal(imageSizeConfig.minWidth()),
              new BigDecimal(imageSizeConfig.thumbnail().scalingFactor()),
              aspectRatioFromString(imageSizeConfig.aspectRatio())
            );
            case SMALL -> calculateHeightWidth(
              new BigDecimal(imageSizeConfig.minWidth()),
              new BigDecimal(imageSizeConfig.small().scalingFactor()),
              aspectRatioFromString(imageSizeConfig.aspectRatio())
            );
            case MEDIUM -> calculateHeightWidth(
              new BigDecimal(imageSizeConfig.minWidth()),
              new BigDecimal(imageSizeConfig.medium().scalingFactor()),
              aspectRatioFromString(imageSizeConfig.aspectRatio())
            );
            case LARGE -> calculateHeightWidth(
              new BigDecimal(imageSizeConfig.minWidth()),
              new BigDecimal(imageSizeConfig.large().scalingFactor()),
              aspectRatioFromString(imageSizeConfig.aspectRatio())
            );
        };
    }

    private int calculateWidth(BigDecimal minWidth, BigDecimal scaleFactor) {
        return minWidth.multiply(scaleFactor).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private int calculateHeight(BigDecimal minWidth, BigDecimal scaleFactor, BigDecimal aspectRatio) {
        return minWidth.multiply(scaleFactor).divide(aspectRatio, 0, RoundingMode.HALF_UP).intValue();
    }


    private ImageHeightWidth calculateHeightWidth(BigDecimal minWidth, BigDecimal scaleFactor, BigDecimal aspectRatio) {
        return ImageHeightWidth.builder()
          .height(calculateHeight(minWidth, scaleFactor, aspectRatio))
          .width(calculateWidth(minWidth, scaleFactor))
          .build();
    }

    private BigDecimal aspectRatioFromString(String ratio) {
        Pattern pattern = Pattern.compile("^(\\d+):(\\d+)$");
        Matcher matcher = pattern.matcher(ratio);

        if (!matcher.matches()) {
            throw new InvalidConfigurationException("Unable to match ratio when parsing: " + ratio);
        }

        var widthPart = new BigDecimal(matcher.group(1));
        var heightPart = new BigDecimal(matcher.group(2));

        return widthPart.divide(heightPart, 2, RoundingMode.HALF_UP);
    }
}
