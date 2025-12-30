package com.benhession.imagepicker.api.dto;

import com.benhession.imagepicker.api.validation.EnumValidator;
import com.benhession.imagepicker.common.model.ImageOrientation;
import com.benhession.imagepicker.common.model.ImageType;
import io.quarkus.runtime.annotations.RegisterForReflection;
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
@RegisterForReflection
public class CropPropertiesDto {

    @EnumValidator(enumClass = ImageType.class)
    private String imageType;
    @EnumValidator(enumClass = ImageOrientation.class)
    private String orientation;
    @Valid
    private CoordinateDto baseCoordinate;
    @NotNull
    @SuppressWarnings("unused")
    Integer width;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    public static class CoordinateDto {

        @NotNull
        @SuppressWarnings("checkstyle:MemberNameCheck")
        private Integer x;
        @NotNull
        @SuppressWarnings("checkstyle:MemberNameCheck")
        private Integer y;
    }
}


