package com.benhession.imagepicker.mapper;


import com.benhession.imagepicker.dto.ImageResponseDto;
import com.benhession.imagepicker.model.ImageMetadata;
import com.benhession.imagepicker.model.ImageSize;
import com.benhession.imagepicker.service.ObjectStorageService;
import com.benhession.imagepicker.util.FilenameUtil;
import jakarta.inject.Inject;
import org.mapstruct.*;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ImageResponseMapper {
    @Inject
    ObjectStorageService objectStorageService;
    @Inject
    FilenameUtil filenameUtil;

    @Mapping(target = "id", expression = "java(imageMetadata.getId().toString())")
    public abstract ImageResponseDto toDto(ImageMetadata imageMetadata);

    @AfterMapping
    public void setImages(ImageMetadata imageMetadata, @MappingTarget ImageResponseDto imageResponseDto) {
        var imagesMap = Arrays.stream(ImageSize.values())
                .collect(Collectors.toMap(
                        Function.identity(),
                        imageSize -> String.format("%s/%s",
                                objectStorageService.getBaseResourcePath(imageMetadata.getParentKey()),
                                filenameUtil.getFilename(imageMetadata.getFilename(), imageMetadata.getType(), imageSize))));
        imageResponseDto.setImages(imagesMap);
    }
}
