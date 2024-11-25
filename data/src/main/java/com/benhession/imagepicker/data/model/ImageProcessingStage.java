package com.benhession.imagepicker.data.model;

public enum ImageProcessingStage {
    ORIGINAL_UPLOADED,
    PROCESSING,
    PROCESSING_COMPLETE,
    PROCESSING_TIMEOUT,
    PROCESSING_FAILED;

    public static boolean isInProgress(ImageProcessingStage imageProcessingStage) {
        return imageProcessingStage.equals(ORIGINAL_UPLOADED) || imageProcessingStage.equals(PROCESSING);
    }
}
