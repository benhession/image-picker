package com.benhession.imagepicker.api.mapper;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.benhession.imagepicker.api.dto.CropPropertiesDto;
import com.benhession.imagepicker.common.model.ImageCropProperties;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = IGNORE)
public abstract class CropPropertiesMapper {

    public abstract ImageCropProperties toModel(CropPropertiesDto cropPropertiesDto);
}
