package com.benhession.imagepicker.common.service;

import static com.benhession.imagepicker.common.model.ImageOrientation.PORTRAIT;
import static java.math.RoundingMode.HALF_UP;

import com.benhession.imagepicker.common.config.ImageConfigProperties;
import com.benhession.imagepicker.common.exception.InvalidConfigurationException;
import com.benhession.imagepicker.common.model.ImageHeightWidth;
import com.benhession.imagepicker.common.model.ImageOrientation;
import com.benhession.imagepicker.common.model.ImageSize;
import com.benhession.imagepicker.common.model.ImageType;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageSizeService {

    private final ImageConfigProperties imageConfigProperties;

    public BigDecimal findAspectRatio(ImageType imageType, ImageOrientation orientation) {
        boolean reverse = orientation == PORTRAIT;

        return switch (imageType) {
            case SQUARE -> aspectRatioFromString(imageConfigProperties.size().square().aspectRatio(), reverse);
            case PANORAMIC -> aspectRatioFromString(imageConfigProperties.size().panoramic().aspectRatio(), reverse);
            case RECTANGULAR ->
                aspectRatioFromString(imageConfigProperties.size().rectangular().aspectRatio(), reverse);
            case WIDE -> aspectRatioFromString(imageConfigProperties.size().wide().aspectRatio(), reverse);
        };
    }

    public int findMinWidth(ImageType imageType, ImageOrientation orientation) {
        return switch (imageType) {
            case SQUARE -> calculateMinWidthForOrientation(
                Integer.parseInt(imageConfigProperties.size().square().minWidth()),
                imageType, orientation);
            case PANORAMIC -> calculateMinWidthForOrientation(
                Integer.parseInt(imageConfigProperties.size().panoramic().minWidth()),
                imageType, orientation);
            case RECTANGULAR -> calculateMinWidthForOrientation(
                Integer.parseInt(imageConfigProperties.size().rectangular().minWidth()),
                imageType, orientation);
            case WIDE -> calculateMinWidthForOrientation(
                Integer.parseInt(imageConfigProperties.size().wide().minWidth()),
                imageType, orientation);
        };
    }

    public ImageHeightWidth findImageHeightWidth(ImageType imageType, ImageSize imageSize,
        ImageOrientation orientation) {
        var squareConfig = imageConfigProperties.size().square();
        var panoConfig = imageConfigProperties.size().panoramic();
        var rectangularConfig = imageConfigProperties.size().rectangular();
        var landscapeConfig = imageConfigProperties.size().wide();

        return switch (imageType) {
            case SQUARE -> heightWidthFromImageSize(imageSize, squareConfig, orientation);
            case PANORAMIC -> heightWidthFromImageSize(imageSize, panoConfig, orientation);
            case RECTANGULAR -> heightWidthFromImageSize(imageSize, rectangularConfig, orientation);
            case WIDE -> heightWidthFromImageSize(imageSize, landscapeConfig, orientation);
        };
    }

    public int calculateImageHeight(int width, ImageType imageType, ImageOrientation orientation) {
        BigDecimal height = new BigDecimal(width).divide(findAspectRatio(imageType, orientation), 2, HALF_UP);
        return height.intValue();
    }

    public BigDecimal aspectRatioFromString(String ratio, boolean reverse) {
        Pattern pattern = Pattern.compile("^(\\d+):(\\d+)$");
        Matcher matcher = pattern.matcher(ratio);

        if (!matcher.matches()) {
            throw new InvalidConfigurationException("Unable to match ratio when parsing: " + ratio);
        }

        var widthPart = new BigDecimal(matcher.group(1));
        var heightPart = new BigDecimal(matcher.group(2));

        if (reverse) {
            return heightPart.divide(widthPart, 2, RoundingMode.HALF_UP);
        }

        return widthPart.divide(heightPart, 2, RoundingMode.HALF_UP);
    }

    private int calculateMinWidthForOrientation(int minWidth, ImageType imageType, ImageOrientation orientation) {
        if (orientation == PORTRAIT) {
            BigDecimal aspectRatio = findAspectRatio(imageType, orientation);
            return new BigDecimal(minWidth).multiply(aspectRatio).intValue();
        }

        return minWidth;
    }

    private BigDecimal calculateMinWidthForOrientation(ImageConfigProperties.ImageType.ImageSize imageSizeConfig,
        ImageOrientation orientation) {
        if (orientation == PORTRAIT) {
            BigDecimal aspectRatio = aspectRatioFromString(imageSizeConfig.aspectRatio(), true);
            return new BigDecimal(imageSizeConfig.minWidth()).multiply(aspectRatio);
        }

        return new BigDecimal(imageSizeConfig.minWidth());
    }

    private ImageHeightWidth heightWidthFromImageSize(ImageSize imageSize,
        ImageConfigProperties.ImageType.ImageSize imageSizeConfig, ImageOrientation orientation) {
        var reverse = orientation == PORTRAIT;
        return switch (imageSize) {
            case THUMBNAIL -> calculateHeightWidth(
                calculateMinWidthForOrientation(imageSizeConfig, orientation),
                new BigDecimal(imageSizeConfig.thumbnail().scalingFactor()),
                aspectRatioFromString(imageSizeConfig.aspectRatio(), reverse)
            );
            case SMALL -> calculateHeightWidth(
                calculateMinWidthForOrientation(imageSizeConfig, orientation),
                new BigDecimal(imageSizeConfig.small().scalingFactor()),
                aspectRatioFromString(imageSizeConfig.aspectRatio(), reverse)
            );
            case MEDIUM -> calculateHeightWidth(
                calculateMinWidthForOrientation(imageSizeConfig, orientation),
                new BigDecimal(imageSizeConfig.medium().scalingFactor()),
                aspectRatioFromString(imageSizeConfig.aspectRatio(), reverse)
            );
            case LARGE -> calculateHeightWidth(
                calculateMinWidthForOrientation(imageSizeConfig, orientation),
                new BigDecimal(imageSizeConfig.large().scalingFactor()),
                aspectRatioFromString(imageSizeConfig.aspectRatio(), reverse)
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
}
