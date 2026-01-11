package com.benhession.imagepicker.api.mapper;

import static com.benhession.imagepicker.common.model.ImageOrientation.LANDSCAPE;

import com.benhession.imagepicker.api.dto.ImageAttributesDto.ImageSizeAttributesDto;
import com.benhession.imagepicker.api.dto.ImageAttributesDto.ImageSizeDto;
import com.benhession.imagepicker.common.config.ImageConfigProperties.ImageType.ImageSize;
import com.benhession.imagepicker.common.config.ImageConfigProperties.ImageType.ImageSize.Scale;
import com.benhession.imagepicker.common.model.ImageType;
import com.benhession.imagepicker.common.service.ImageSizeService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ImageSizeMapper {

    @Inject
    ImageSizeService imageSizeService;

    @Mapping(target = "aspectRatio", ignore = true)
    @Mapping(target = "minWidth", expression = "java(Integer.valueOf(imageSize.minWidth()))")
    @Mapping(target = "thumbnail", expression = "java(mapImageAttributes(imageSize.thumbnail(), imageType, c"
        + "om.benhession.imagepicker.common.model.ImageSize.THUMBNAIL))")
    @Mapping(target = "small", expression = "java(mapImageAttributes(imageSize.small(), imageType, "
        + "com.benhession.imagepicker.common.model.ImageSize.SMALL))")
    @Mapping(target = "medium", expression = "java(mapImageAttributes(imageSize.medium(), imageType, "
        + "com.benhession.imagepicker.common.model.ImageSize.MEDIUM))")
    @Mapping(target = "large", expression = "java(mapImageAttributes(imageSize.large(), imageType, "
        + "com.benhession.imagepicker.common.model.ImageSize.LARGE))")
    public abstract ImageSizeDto toDto(ImageSize imageSize,
        com.benhession.imagepicker.common.model.ImageType imageType);

    @AfterMapping
    public void mapAspectRatio(ImageSize imageSize, @MappingTarget ImageSizeDto imageSizeDto) {
        BigDecimal aspectRation = imageSizeService.aspectRatioFromString(imageSize.aspectRatio(), false);
        imageSizeDto.setAspectRatio(aspectRation.doubleValue());
    }

    public ImageSizeAttributesDto mapImageAttributes(Scale scale, ImageType imageType,
        com.benhession.imagepicker.common.model.ImageSize imageSize) {
        var heightWidth = imageSizeService.findImageHeightWidth(imageType, imageSize, LANDSCAPE);
        return ImageSizeAttributesDto.builder()
            .scalingFactor(Double.parseDouble(scale.scalingFactor()))
            .width(heightWidth.getWidth())
            .height(heightWidth.getHeight())
            .build();
    }
}
