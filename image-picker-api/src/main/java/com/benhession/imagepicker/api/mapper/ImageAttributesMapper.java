package com.benhession.imagepicker.api.mapper;

import com.benhession.imagepicker.api.dto.ImageAttributesDto;
import com.benhession.imagepicker.common.config.ImageConfigProperties;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ImageAttributesMapper {

    @Inject
    ImageSizeMapper imageSizeMapper;

    @Mapping(target = "square", expression = "java(imageSizeMapper.toDto(configProperties.size().square(), "
        + "com.benhession.imagepicker.common.model.ImageType.SQUARE))")
    @Mapping(target = "rectangular", expression = "java(imageSizeMapper.toDto(configProperties.size().rectangular(), "
        + "com.benhession.imagepicker.common.model.ImageType.RECTANGULAR))")
    @Mapping(target = "wide", expression = "java(imageSizeMapper.toDto(configProperties.size().wide(), "
        + "com.benhession.imagepicker.common.model.ImageType.WIDE))")
    @Mapping(target = "panoramic", expression = "java(imageSizeMapper.toDto(configProperties.size().panoramic(), "
        + "com.benhession.imagepicker.common.model.ImageType.PANORAMIC))")
    @Mapping(target = "acceptedMimeTypes", expression = "java(configProperties.acceptedMimeTypes())")
    public abstract ImageAttributesDto toDto(ImageConfigProperties configProperties);
}
