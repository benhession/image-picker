package com.benhession.imagepicker.data.model;

import java.time.Instant;

public record ImageProcessingStatus(Instant statusChangedAt, ImageProcessingStage stage) {
    public static ImageProcessingStatus of(ImageProcessingStage stage) {
        return new ImageProcessingStatus(Instant.now(), stage);
    }
}