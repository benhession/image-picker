package com.benhession.imagepicker.exception;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public abstract class AbstractMultipleErrorApplicationException extends RuntimeException {
    private final List<ErrorMessage> errorMessages;

    public AbstractMultipleErrorApplicationException(List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }

    @Builder
    public record ErrorMessage(String path, String message) {}
}
