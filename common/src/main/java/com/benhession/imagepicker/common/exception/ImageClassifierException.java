package com.benhession.imagepicker.common.exception;

public class ImageClassifierException extends RuntimeException {

    public ImageClassifierException(String message) {
        super(message);
    }

    public ImageClassifierException(String message, Throwable cause) {
        super(message, cause);
    }
}
