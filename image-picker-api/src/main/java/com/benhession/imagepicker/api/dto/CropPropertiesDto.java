package com.benhession.imagepicker.api.dto;

import com.benhession.imagepicker.api.validation.EnumValidator;
import com.benhession.imagepicker.common.model.ImageType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CropPropertiesDto {

    @EnumValidator(enumClass = ImageType.class)
    private String imageType;
    @Valid
    private CoordinateDto baseCoordinate;
    @NotNull
    Integer width;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinateDto {

        @NotNull
        private Integer x;
        @NotNull
        private Integer y;
    }
}


