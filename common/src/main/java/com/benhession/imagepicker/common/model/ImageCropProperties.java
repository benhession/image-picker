package com.benhession.imagepicker.common.model;


import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@RegisterForReflection
@Builder
public record ImageCropProperties(ImageType imageType, Coordinate baseCoordinate, int width,
                                  ImageOrientation orientation) {

}
