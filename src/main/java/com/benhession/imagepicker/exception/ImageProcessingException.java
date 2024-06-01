package com.benhession.imagepicker.exception;

public class ImageProcessingException extends RuntimeException {
    public ImageProcessingException(String errorMessage) {
        super(errorMessage);
    }

    public ImageProcessingException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}

