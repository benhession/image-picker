package com.benhession.imagepicker.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
