package com.benhession.imagepicker.config;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Getter
@ConfigProperties(prefix = "image.size")
public class ImageConfigProperties {

    // square images

    @ConfigProperty(name = "square.thumbnail.width")
    int squareThumbnailWidth;
    @ConfigProperty(name = "square.thumbnail.height")
    int squareThumbnailHeight;
    @ConfigProperty(name = "square.small.width")
    int squareSmallWidth;
    @ConfigProperty(name = "square.small.height")
    int squareSmallHeight;
    @ConfigProperty(name = "square.medium.width")
    int squareMediumWidth;
    @ConfigProperty(name = "square.medium.height")
    int squareMediumHeight;
    @ConfigProperty(name = "square.large.width")
    int squareLargeWidth;
    @ConfigProperty(name = "square.large.height")
    int squareLargeHeight;

    // panoramic images

    @ConfigProperty(name = "panoramic.thumbnail.width")
    int panoramicThumbnailWidth;
    @ConfigProperty(name = "panoramic.thumbnail.height")
    int panoramicThumbnailHeight;
    @ConfigProperty(name = "panoramic.small.width")
    int panoramicSmallWidth;
    @ConfigProperty(name = "panoramic.small.height")
    int panoramicSmallHeight;
    @ConfigProperty(name = "panoramic.medium.width")
    int panoramicMediumWidth;
    @ConfigProperty(name = "panoramic.medium.height")
    int panoramicMediumHeight;
    @ConfigProperty(name = "panoramic.large.width")
    int panoramicLargeWidth;
    @ConfigProperty(name = "panoramic.large.height")
    int panoramicLargeHeight;
    @ConfigProperty(name = "rectangular.thumbnail.width")

    // rectangular images

    int rectangularThumbnailWidth;
    @ConfigProperty(name = "rectangular.thumbnail.height")
    int rectangularThumbnailHeight;
    @ConfigProperty(name = "rectangular.small.width")
    int rectangularSmallWidth;
    @ConfigProperty(name = "rectangular.small.height")
    int rectangularSmallHeight;
    @ConfigProperty(name = "rectangular.medium.width")
    int rectangularMediumWidth;
    @ConfigProperty(name = "rectangular.medium.height")
    int rectangularMediumHeight;
    @ConfigProperty(name = "rectangular.large.width")
    int rectangularLargeWidth;
    @ConfigProperty(name = "rectangular.large.height")
    int rectangularLargeHeight;

    // landscape images

    @ConfigProperty(name = "landscape.thumbnail.width")
    int landscapeThumbnailWidth;
    @ConfigProperty(name = "landscape.thumbnail.height")
    int landscapeThumbnailHeight;
    @ConfigProperty(name = "landscape.medium.height")
    int landscapeMediumHeight;
    @ConfigProperty(name = "landscape.small.width")
    int landscapeSmallWidth;
    @ConfigProperty(name = "landscape.small.height")
    int landscapeSmallHeight;
    @ConfigProperty(name = "landscape.medium.width")
    int landscapeMediumWidth;
    @ConfigProperty(name = "landscape.large.width")
    int landscapeLargeWidth;
    @ConfigProperty(name = "landscape.large.height")
    int landscapeLargeHeight;
}