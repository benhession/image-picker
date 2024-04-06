package com.benhession.imagepicker.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@EqualsAndHashCode
public class ErrorResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String errorId;
    private final List<ErrorMessage> errors;

    public ErrorResponse(String errorId, ErrorMessage errorMessage) {
        this.errorId = errorId;
        this.errors = List.of(errorMessage);
    }

    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class ErrorMessage {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String path;
        private String message;

        public ErrorMessage(String path, String message) {
            this.path = path;
            this.message = message;
        }

        public ErrorMessage(String message) {
            this.path = null;
            this.message = message;
        }
    }

}
