package com.benhession.imagepicker.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class ErrorResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorId;
    private List<ErrorMessage> errors;

    @Getter
    @Setter
    @Builder
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ErrorMessage {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String path;
        private String message;
    }

}
