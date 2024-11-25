package com.benhession.imagepicker.common.exception;

public class ImageProcessingException extends AbstractSingleErrorApplicationException {
    public ImageProcessingException(String errorMessage) {
        super(errorMessage);
    }

    public ImageProcessingException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}

