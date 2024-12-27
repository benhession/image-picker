package com.benhession.imagepicker.data.model;

import lombok.Getter;

@Getter
public enum ImageProcessingStage {
    INITIALISED(true),
    ORIGINAL_UPLOADED(true),
    PROCESSING(true),
    PROCESSING_COMPLETE(false),
    PROCESSING_TIMEOUT(false),
    PROCESSING_FAILED(false);

    private final boolean isInProgress;

    ImageProcessingStage(boolean isInProgress) {
        this.isInProgress = isInProgress;
    }
}
