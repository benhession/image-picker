package com.benhession.imagepicker.api.mapper;

import com.benhession.imagepicker.api.dto.ImageResponseDto;
import com.benhession.imagepicker.common.model.ImageSize;
import com.benhession.imagepicker.common.util.FilenameUtil;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ImageResponseMapper {
    @Inject
    ObjectStorageService objectStorageService;
    @Inject
    FilenameUtil filenameUtil;

    @Mapping(target = "id", expression = "java(imageMetadata.getId().toString())")
    @Mapping(target = "images", ignore = true)
    @BeanMapping(qualifiedByName = "withImages")
    public abstract ImageResponseDto toDto(ImageMetadata imageMetadata);

    @Mapping(target = "id", expression = "java(imageMetadata.getId().toString())")
    @Mapping(target = "images", ignore = true)
    public abstract ImageResponseDto toDtoWithoutImages(ImageMetadata imageMetadata);

    @AfterMapping
    @Named("withImages")
    public void setImages(ImageMetadata imageMetadata, @MappingTarget ImageResponseDto imageResponseDto) {
        var imagesMap = Arrays.stream(ImageSize.values())
                .collect(Collectors.toMap(
                        Function.identity(),
                        imageSize -> String.format("%s/%s",
                                objectStorageService.getBaseResourcePath(imageMetadata.getParentKey()),
                                filenameUtil.getFilename(
                                  imageMetadata.getFilename(), imageMetadata.getType(), imageSize))));
        imageResponseDto.setImages(imagesMap);
    }
}
