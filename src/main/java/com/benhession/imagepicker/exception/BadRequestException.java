package com.benhession.imagepicker.exception;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class BadRequestException extends RuntimeException {
    private final List<ErrorMessage> errorMessages;
    public BadRequestException(List<ErrorMessage> errorMessages) {
        super(errorMessages.toString());
        this.errorMessages = errorMessages;
    }

    @Builder
    public record ErrorMessage(String path, String message) {}
}
