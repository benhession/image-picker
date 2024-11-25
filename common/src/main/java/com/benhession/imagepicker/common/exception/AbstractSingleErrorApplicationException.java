package com.benhession.imagepicker.common.exception;

public abstract class AbstractSingleErrorApplicationException extends RuntimeException {

    public AbstractSingleErrorApplicationException(String message) {
        super(message);
    }

    public AbstractSingleErrorApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
