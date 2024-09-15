package com.benhession.imagepicker.imageprocessor.exception;

import java.util.List;

public class BadRequestException extends AbstractMultipleErrorApplicationException {

    public BadRequestException(List<ErrorMessage> errorMessages) {
        super(errorMessages);
    }
}