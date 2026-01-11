package com.benhession.imagepicker.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ImageAttributesDto {

    private List<String> acceptedMimeTypes;
    private ImageSizeDto square;
    private ImageSizeDto panoramic;
    private ImageSizeDto rectangular;
    private ImageSizeDto wide;

    @Getter
    @Setter
    public static class ImageSizeDto {

        private Double aspectRatio;
        private int minWidth;
        private ImageSizeAttributesDto thumbnail;
        private ImageSizeAttributesDto small;
        private ImageSizeAttributesDto medium;
        private ImageSizeAttributesDto large;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageSizeAttributesDto {

        private double scalingFactor;
        private int width;
        private int height;
    }
}
