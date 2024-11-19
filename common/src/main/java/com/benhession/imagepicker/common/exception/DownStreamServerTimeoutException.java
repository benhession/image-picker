package com.benhession.imagepicker.common.exception;

public class DownStreamServerTimeoutException extends AbstractSingleErrorApplicationException {
    public DownStreamServerTimeoutException(String message) {
        super(message);
    }
}
