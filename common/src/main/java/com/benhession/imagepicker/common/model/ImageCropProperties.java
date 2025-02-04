package com.benhession.imagepicker.common.model;


import lombok.Builder;

@Builder
public record ImageCropProperties(ImageType imageType, Coordinate baseCoordinate, int width) {

}
