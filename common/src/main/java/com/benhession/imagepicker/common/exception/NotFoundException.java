package com.benhession.imagepicker.common.exception;

import java.util.List;

public class NotFoundException extends AbstractMultipleErrorApplicationException {

    public NotFoundException(List<ErrorMessage> errorMessages) {
        super(errorMessages);
    }
}
